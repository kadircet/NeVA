package mealrecommender.neva.com.neva_android_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import com.google.protobuf.ByteString;
import neva.backend.BackendOuterClass.LoginRequest.AuthenticationType;

public class SplashScreen extends AppCompatActivity {

  public static final String TAG = "SplashScreen";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AccountManager accountManager = AccountManager.get(getBaseContext());

    // If we already have a NeVA account, try to login automatically with the saved password.
    // TODO: Don't login with the password, but use getAuthToken for the user, when backend supports
    // checking validity of authTokens.
    Account accounts[] = accountManager.getAccountsByType(LoginActivity.ACCOUNT_TYPE);
    Log.i(TAG, Integer.toString(accounts.length));
    if(accounts.length > 0){
      Account userAccount = accounts[0]; //TODO:Implement Account picker for multiple account support.
      try {
        String email = userAccount.name;
        String password = accountManager.getPassword(userAccount);
        NevaLoginManager.getInstance().logIn(email, password, AuthenticationType.DEFAULT);
        String authToken = NevaLoginManager.getInstance().getStringToken();
        //TODO: Check Auth Token With Server
        Log.i(TAG, authToken);
        accountManager.setAuthToken(userAccount, LoginActivity.AUTH_TOKEN_TYPE, authToken);
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      Log.i(TAG, "LoginActivity");
      Intent intent = new Intent(getBaseContext(), LoginActivity.class);
      startActivity(intent);
    }
  }
}
