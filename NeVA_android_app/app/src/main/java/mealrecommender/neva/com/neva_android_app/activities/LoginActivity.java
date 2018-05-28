package mealrecommender.neva.com.neva_android_app.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import java.util.Arrays;
import mealrecommender.neva.com.neva_android_app.util.NevaLoginManager;
import mealrecommender.neva.com.neva_android_app.R;
import neva.backend.BackendOuterClass.LoginRequest.AuthenticationType;

@RequiresApi(api = Build.VERSION_CODES.ECLAIR)
public class LoginActivity extends AppCompatActivity {

  private final String TAG = this.getClass().getSimpleName();
  public static final String ACCOUNT_TYPE = "com.neva.mealrecommender";
  public static final String FACEBOOK_APP_ID = "FACEBOOK_APP_ID";
  public static final String FACEBOOK_USER_ID = "FACEBOOK_USER_ID";
  public static final String FACEBOOK_TOKEN = "FACEBOOK_TOKEN";
  public static final String IS_FACEBOOK_LOGIN = "IS_FACEBOOK_LOGIN";
  public static final String NEVA_TOKEN_TYPE = "NEVA_TOKEN";
  public static final String IS_ADDING_NEW_ACCOUNT = "IS_ADDING_NEW_ACCOUNT";

  private EditText emailField;
  private EditText passwordField;
  private Button loginButton;
  private TextView registerText;
  private LoginButton facebook_login_button;
  private ProgressBar progressBar;
  private CallbackManager callbackManager;
  private NevaLoginManager nevaLoginManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //getWindow().setBackgroundDrawableResource(R.drawable.background);
    setContentView(R.layout.activity_login);

    emailField = findViewById(R.id.user_email);
    passwordField = findViewById(R.id.user_password);
    loginButton = findViewById(R.id.submit_button);
    registerText = findViewById(R.id.register_text);
    facebook_login_button = findViewById(R.id.facebook_login_button);
    progressBar = findViewById(R.id.progress_bar);
    callbackManager = CallbackManager.Factory.create();
    nevaLoginManager = NevaLoginManager.getInstance();
    Intent intent = getIntent();

    facebook_login_button.setReadPermissions(Arrays.asList("public_profile", "email"));
    facebook_login_button.registerCallback(callbackManager, new FacebookButtonCallback());
  }

  public void onLoginButton(View view) {
    Log.i(TAG, "Login Button Pressed");
    String email = emailField.getText().toString().trim();
    String password = passwordField.getText().toString();
    if (validateEmail(email) && validatePassword(password)) {
      Toast.makeText(getBaseContext(), "Logging In", Toast.LENGTH_SHORT).show();
      LoginTask loginTask = new LoginTask(email, password, AuthenticationType.DEFAULT, null);
      loginTask.execute();
    }
  }

  public void onRegisterButton(View view) {
    Log.i(TAG, "Launching RegisterActivity");
    Intent intent = new Intent(this, RegisterActivity.class);
    startActivityForResult(intent, 1);
  }

  public void submit(int result) {
    Log.i(TAG, "Login Result: " + Integer.toString(result));
    if (result == RESULT_OK) {
      Log.e(TAG,"HERE");
      Toast.makeText(getBaseContext(), getResources().getString(R.string.success_add_account),
          Toast.LENGTH_SHORT).show();
      setResult(RESULT_OK);
    } else {
      Log.e(TAG,"THERE");
      setResult(RESULT_CANCELED);
    }
    Log.e(TAG,"HERE");
    finish();
    Log.e(TAG,"HERE");
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

  //TODO: Add Back Button functionality. Currently it is not allowed because we need a valid return status.
  @Override
  public void onBackPressed() {
    return;
  }

  public boolean validateEmail(String email) {
    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      emailField.setError(getResources().getString(R.string.error_invalid_email));
      return false;
    }
    emailField.setError(null);
    return true;

  }

  public boolean validatePassword(String password) {
    if (password.isEmpty()) {
      passwordField.setError(getResources().getString(R.string.error_invalid_password));
      return false;
    }
    passwordField.setError(null);
    return true;
  }

  private class FacebookButtonCallback implements FacebookCallback<LoginResult> {

    @Override
    public void onSuccess(final LoginResult loginResult) {
      final String accessToken = loginResult.getAccessToken().getToken();
      Log.i(TAG, loginResult.getAccessToken().getApplicationId());
      Log.i(TAG, loginResult.getAccessToken().getUserId());
      Log.i(TAG, "Facebook Access Token: " + accessToken);
      Toast.makeText(getBaseContext(), getResources().getString(R.string.success_facebook_login),
          Toast.LENGTH_SHORT).show();
      String userId = loginResult.getAccessToken().getUserId();
      try {
        progressBar.setVisibility(View.VISIBLE);
        LoginTask loginTask = new LoginTask(userId, accessToken, AuthenticationType.FACEBOOK, loginResult.getAccessToken());
        loginTask.execute();
        progressBar.setVisibility(View.GONE);
      } catch (Exception e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        LoginManager.getInstance().logOut();
        NevaLoginManager.getInstance().logOut();
      }
    }

    @Override
    public void onCancel() {
      Toast.makeText(getBaseContext(), getResources().getString(R.string.error_facebook_cancel),
          Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(FacebookException error) {
      Toast.makeText(getBaseContext(), getResources().getString(R.string.error_facebook_error),
          Toast.LENGTH_LONG).show();
    }
  }

  class LoginTask extends AsyncTask<Void, Void, Integer> {

    String email;
    String password;
    AuthenticationType authType;
    AccessToken facebookToken;

    public LoginTask(String email, String password, AuthenticationType authType,
        AccessToken facebookToken) {
      this.email = email;
      this.password = password;
      this.authType = authType;
      this.facebookToken = facebookToken;
    }

    @Override
    protected void onPreExecute() {
      loginButton.setEnabled(false);
      progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Integer doInBackground(Void... voids) {
      boolean res = nevaLoginManager.logIn(email, password, authType);
      Log.i(TAG, res+"");
      if(res) {
        return RESULT_OK;
      }
      return RESULT_CANCELED;
    }
    @Override
    protected void onPostExecute(Integer result) {
      loginButton.setEnabled(true);
      progressBar.setVisibility(View.GONE);
      String authToken = nevaLoginManager.getStringToken();
      boolean isFacebook = facebookToken != null;
      Log.i(TAG, "Login Token: " + authToken);
      SharedPreferences sp = getSharedPreferences("mealrecommender.neva.com.loginstat",
          Context.MODE_PRIVATE);
      Editor editor = sp.edit();
      editor.putBoolean("LOGGED_IN", true);
      editor.putBoolean("IS_FB", isFacebook);
      editor.putString("AUTH_TOKEN", authToken);
      editor.putString("USERNAME", email);
      editor.putString("PASSWORD", password);
      editor.commit();
      Log.i(TAG, "Login Task Return Value: " + result);
      submit(result);
    }
  }

}
