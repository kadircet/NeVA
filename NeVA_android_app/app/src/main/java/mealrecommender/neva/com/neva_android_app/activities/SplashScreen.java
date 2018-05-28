package mealrecommender.neva.com.neva_android_app.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.facebook.login.LoginManager;
import mealrecommender.neva.com.neva_android_app.util.NevaConnectionManager;
import mealrecommender.neva.com.neva_android_app.util.NevaLoginManager;
import mealrecommender.neva.com.neva_android_app.R;
import neva.backend.BackendOuterClass.LoginRequest.AuthenticationType;

public class SplashScreen extends AppCompatActivity {

  public final String TAG = this.getClass().getSimpleName();
  private SharedPreferences sharedPref;
  private boolean isLoggedIn;
  private AuthenticationType authType;
  String password;
  String email ;
  String token;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sharedPref = getSharedPreferences("mealrecommender.neva.com.loginstat",
                                      Context.MODE_PRIVATE);
    isLoggedIn = sharedPref.getBoolean("LOGGED_IN", false);
    authType = sharedPref.getBoolean("IS_FB", false) ?
                                      AuthenticationType.FACEBOOK :
                                      AuthenticationType.DEFAULT;
    token = sharedPref.getString("AUTH_TOKEN", null);
    email = sharedPref.getString("USERNAME", null);
    password = sharedPref.getString("PASSWORD", null);
    Log.d(TAG, ""+isLoggedIn);
    checkAccount();
  }

  public void checkAccount() {
    if(!isLoggedIn) {
      Log.d(TAG, "Forwarding to LoginPage");
      forwardToLoginPage();
    }
    if(isLoggedIn && token != null && email != null) {
      Log.d(TAG, ""+isLoggedIn);
      Log.d(TAG, token);
      Log.d(TAG, email);
      Log.d(TAG, password);
      Log.d(TAG, "Data complete setting up Login Manager");
      NevaLoginManager.getInstance().setAuthToken(email, token);
      Log.d(TAG, "Checking Token Validity");
      if(NevaLoginManager.getInstance().validateToken() || NevaLoginManager.getInstance().logIn(email, password, authType)) {
        Log.d(TAG, "Saved values all good");
        successfulLogin();
      } else {
        Log.d(TAG, "Token validation & login attempt failed.");
        Log.d(TAG, "Forwarding to LoginPage");
        forwardToLoginPage();
      }
    }
  }

  public void successfulLogin() {
    Intent intent;
    Log.d(TAG, "Checking cold start status");
    if(NevaConnectionManager.getInstance().getColdStartStatus()) {
      Log.d(TAG,"Cold Start complete");
      intent = new Intent(getBaseContext(), MainActivity.class);
    } else {
      Log.d(TAG,"Cold Start is not complete");
      intent = new Intent(getBaseContext(), ColdStartActivity.class);
    }
    startActivity(intent);
  }

  public void forwardToLoginPage() {
    startActivityForResult(new Intent(getBaseContext(), LoginActivity.class), 1);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, "Returned from login with code: "+ resultCode+", request code: "+ requestCode);
    if(resultCode == RESULT_OK && requestCode == 1) {
      successfulLogin();
    }
    Log.d(TAG, ""+resultCode);
  }
}
