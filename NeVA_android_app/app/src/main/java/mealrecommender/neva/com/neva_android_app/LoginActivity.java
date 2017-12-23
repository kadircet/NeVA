package mealrecommender.neva.com.neva_android_app;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.login.Login;
import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;

@RequiresApi(api = Build.VERSION_CODES.ECLAIR)
public class LoginActivity extends AppCompatActivity {

  private static final String TAG = "LoginActivity_legacy";
  public static final String ARG_ACCOUNT_TYPE = "accountType";
  public static final String ARG_AUTH_TYPE = "authType";
  public static final String ARG_IS_ADDING_NEW_ACCOUNT = "isAddingNewAccount";

  private EditText email_field;
  private EditText password_field;
  private Button login_button;
  private TextView register_text;
  private AccountManager mAccountManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    email_field = findViewById(R.id.user_email);
    password_field = findViewById(R.id.user_password);
    login_button = findViewById(R.id.submit_button);
    register_text = findViewById(R.id.register_text);
    mAccountManager = AccountManager.get(getBaseContext());

  }

  public void onLoginButton(View view) {
    String email = email_field.getText().toString();
    String password = password_field.getText().toString();
    submit(email, password);
  }

  public void submit(final String email, final String password) {

    BackendOuterClass.LoginRequest request = BackendOuterClass.LoginRequest.newBuilder()
        .setAuthenticationType(BackendOuterClass.LoginRequest.AuthenticationType.DEFAULT)
        .setEmail(email)
        .setPassword(password)
        .build();

    ManagedChannel mChannel = ManagedChannelBuilder.forAddress("www.0xdeffbeef.com", 50051)
        .usePlaintext(true).build();
    BackendGrpc.BackendBlockingStub blockingStub = BackendGrpc.newBlockingStub(mChannel);
    byte[] authToken = null;

    try {
      BackendOuterClass.LoginReply reply = blockingStub.login(request);
      ByteString tokenByteString = reply.getToken();
      authToken = tokenByteString.toByteArray();

      createAccount(email, password, Base64.encodeToString(authToken, Base64.DEFAULT));

      Intent intent = new Intent(getBaseContext(), MainActivity.class);
      startActivity(intent);
    } catch (Exception e) {
      Log.d(TAG, e.getMessage());
    }
  }

  public void createAccount(String email, String password, String authToken) {
    Account account = new Account(email, LoginActivity.ARG_ACCOUNT_TYPE);
    AccountManager am = AccountManager.get(this);
    am.addAccountExplicitly(account, password, null);
    am.setAuthToken(account, LoginActivity.ARG_AUTH_TYPE, authToken);
  }


}
