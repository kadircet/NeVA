package mealrecommender.neva.com.neva_android_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import neva.backend.BackendOuterClass.LoginRequest.AuthenticationType;

@RequiresApi(api = Build.VERSION_CODES.ECLAIR)
public class LoginActivity extends AppCompatActivity {

  private static final String TAG = "LoginActivity";
  public static final String ACCOUNT_TYPE = "com.neva.mealrecommender";
  public static final String AUTH_TOKEN_TYPE = "FULL_ACCESS";
  public static final String IS_ADDING_NEW_ACCOUNT = "IS_ADDING_NEW_ACCOUNT";

  private EditText emailField;
  private EditText passwordField;
  private Button loginButton;
  private TextView registerText;
  private AccountManager accountManager;
  private NevaLoginManager nevaLoginManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    emailField = findViewById(R.id.user_email);
    passwordField = findViewById(R.id.user_password);
    loginButton = findViewById(R.id.submit_button);
    registerText = findViewById(R.id.register_text);
    accountManager = AccountManager.get(getBaseContext());
    nevaLoginManager = NevaLoginManager.getInstance();
  }

  public void onLoginButton(View view) {
    String email = emailField.getText().toString();
    String password = passwordField.getText().toString();
    loginButton.setEnabled(false);
    submit(email, password);
    loginButton.setEnabled(true);
  }

  public void submit(final String email, final String password) {

    try {
      nevaLoginManager.logIn(email, password, AuthenticationType.DEFAULT);
      String authToken = nevaLoginManager.getStringToken();

      Account account = new Account(email, ACCOUNT_TYPE);
      boolean accountAddSuccess = accountManager.addAccountExplicitly(account, password, null);
      accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, authToken);
      if(accountAddSuccess) {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
      } else {
        Log.e(TAG, "Couldn't Add Account!");
      }
    } catch (Exception e) {
      Log.d(TAG, e.getMessage());
    }
  }

}
