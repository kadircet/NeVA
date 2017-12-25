package mealrecommender.neva.com.neva_android_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import java.util.Arrays;
import neva.backend.BackendOuterClass.LoginRequest.AuthenticationType;
import org.json.JSONObject;

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
  private LoginButton facebook_login_button;
  private CallbackManager callbackManager;
  private AccountManager accountManager;
  private NevaLoginManager nevaLoginManager;
  private boolean addAccount;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setBackgroundDrawableResource(R.drawable.background);
    setContentView(R.layout.activity_login);

    emailField = findViewById(R.id.user_email);
    passwordField = findViewById(R.id.user_password);
    loginButton = findViewById(R.id.submit_button);
    registerText = findViewById(R.id.register_text);
    facebook_login_button = findViewById(R.id.facebook_login_button);
    callbackManager = CallbackManager.Factory.create();
    accountManager = AccountManager.get(getBaseContext());
    nevaLoginManager = NevaLoginManager.getInstance();
    Intent intent = getIntent();
    addAccount = intent.getBooleanExtra(LoginActivity.IS_ADDING_NEW_ACCOUNT, false);

    facebook_login_button.setReadPermissions(Arrays.asList("public_profile","email"));
    facebook_login_button.registerCallback(callbackManager, new FacebookButtonCallback());
  }

  public void onLoginButton(View view) {
    String email = emailField.getText().toString();
    String password = passwordField.getText().toString();
    if (validateEmail(email) && validatePassword(password)) {
      loginButton.setEnabled(false);
      submit(email, password, AuthenticationType.DEFAULT);
      loginButton.setEnabled(true);
    }
  }

  public void onRegisterButton(View view) {
    Log.i(TAG, "Launching RegisterActivity");
    Intent intent = new Intent(this, RegisterActivity.class);
    startActivityForResult(intent, 1);
  }

  public void submit(final String email, final String password, AuthenticationType authenticationType) {

    try {
      boolean loginSuccess = nevaLoginManager.logIn(email, password, authenticationType);
      if(loginSuccess) {
        String authToken = nevaLoginManager.getStringToken();
        if (addAccount) {
          Account account = new Account(email, ACCOUNT_TYPE);
          boolean accountAddSuccess = accountManager.addAccountExplicitly(account, password, null);
          accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, authToken);
          if (accountAddSuccess) {
            Log.i(TAG, "Account Added Successfully!");
            setResult(RESULT_OK);
          } else {
            setResult(RESULT_CANCELED);
            Log.e(TAG, "Couldn't Add Account!");
          }
          finish();
        } else {
          Account account = accountManager.getAccountsByType(ACCOUNT_TYPE)[0];
          accountManager.setPassword(account, password);
          accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, authToken);
          setResult(RESULT_OK);
          finish();
        }
      }
    } catch (Exception e) {
      Log.d(TAG, e.getMessage());
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK && requestCode == 1) {
      Log.i(TAG, "Returned from RegisterActivity");
      emailField.getText().clear();
      passwordField.getText().clear();
    }
    super.onActivityResult(requestCode, resultCode, data);
    callbackManager.onActivityResult(requestCode, resultCode, data);
  }

  //Do nothing when back is pressed to be sure that the return code from this activity is true.
  @Override
  public void onBackPressed() {
    return;
  }

  public boolean validateEmail(String email) {
    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      emailField.setError("Enter a valid email.");
      return false;
    }
    emailField.setError(null);
    return true;

  }

  public boolean validatePassword(String password) {
    if (password.isEmpty()) {
      passwordField.setError("Password cannot be empty");
      return false;
    }
    passwordField.setError(null);
    return true;
  }

  private class FacebookButtonCallback implements FacebookCallback<LoginResult> {

    @Override
    public void onSuccess(LoginResult loginResult) {
      final String accessToken = loginResult.getAccessToken().getToken();
      Log.i(TAG,"Facebook Access Token: " +accessToken);
      GraphRequest graphReq =GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
        @Override
        public void onCompleted(JSONObject object, GraphResponse response) {
          Log.i(TAG, "Response: "+response.toString());
          if(response.getError() != null) {
            //Handle Error
          }
          else {
            String email = object.optString("email");
            String name = object.optString("name");

            Log.i(TAG, "Email: "+ email);
            Log.i(TAG,"Name: "+ name);
            try {
              submit(email, accessToken, AuthenticationType.FACEBOOK);
            }
            catch (Exception e)
            {
              Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
              LoginManager.getInstance().logOut();
            }
          }
        }
      });
      Bundle parameters = new Bundle();
      parameters.putString("fields","id,name,email");
      graphReq.setParameters(parameters);
      graphReq.executeAsync();

    }

    @Override
    public void onCancel() {
      Toast.makeText(getBaseContext(), "Facebook Login Cancelled.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(FacebookException error) {
      Toast.makeText(getBaseContext(), "Facebook Login Error.", Toast.LENGTH_LONG).show();
    }
  }

}
