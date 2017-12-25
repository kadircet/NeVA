package mealrecommender.neva.com.neva_android_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.lang.ref.WeakReference;
import neva.backend.BackendOuterClass.LoginRequest.AuthenticationType;

public class SplashScreen extends AppCompatActivity {

  public static final String TAG = "SplashScreen";
  AccountManager accountManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    accountManager = AccountManager.get(getBaseContext());
    checkAccounts();
  }

  private void checkAccounts() {
    // If we already have a NeVA account, try to login automatically with the saved password.
    // TODO: Don't login with the password, but use getAuthToken for the user, when backend supports
    // checking validity of authTokens.
    Account accounts[] = accountManager.getAccountsByType(LoginActivity.ACCOUNT_TYPE);
    if (accounts == null || accounts.length < 1) {
      Intent intent = new Intent(this, LoginActivity.class);
      startActivityForResult(intent, 1);
      Log.i(TAG, "No accounts found, directing to LoginActivity");
      return;
    }

    Account userAccount = accounts[0]; //TODO:Implement Account picker for multiple account support.
    Log.i(TAG, "Account found Logging in...");
    executeLogin(userAccount);
  }

  @SuppressLint("StaticFieldLeak")
  private void executeLogin(Account userAccount) {
    if (userAccount == null) {
      userAccount = accountManager.getAccountsByType(LoginActivity.ACCOUNT_TYPE)[0];
    }
    final Account loginAccount = userAccount;
    Log.i(TAG, "Getting auth token");
    try {
      //BE CAREFUL THERE MAY BE A MEMORY LEAK HERE DUE TO ASYNCTASK LIVING LONGER THAN SPLASHSCREEN
      new AsyncTask<Void, Void, Void>() {
        protected Void doInBackground(Void... voids) {
          AccountManagerFuture accountManagerFuture = accountManager
              .getAuthToken(loginAccount, LoginActivity.AUTH_TOKEN_TYPE, null, SplashScreen.this,
                  new OnTokenAcquired(), null);
          return null;
        }
      }.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void processToken(String token) {
    //TODO: Check Auth Token With Server
    Intent intent = new Intent(getBaseContext(), MainActivity.class);
    startActivity(intent);
    return;
  }

  private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

    @Override
    public void run(AccountManagerFuture<Bundle> result) {
      Bundle bundle = null;
      Intent authIntent = null;
      try {
        bundle = result.getResult();
        authIntent = (Intent) bundle.get(AccountManager.KEY_INTENT);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (authIntent != null) {
        Log.i(TAG, "Couldnt get token with saved values, directing to login");
        startActivityForResult(authIntent, 0);
        return;
      }
      String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
      Log.i(TAG, "TokenString: " + token);
      processToken(token);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.i(TAG, "Login activity returned");
    Log.i(TAG,
        "Request - Result: " + Integer.toString(requestCode) + " " + Integer.toString(resultCode));
    if (resultCode == RESULT_OK && (requestCode == 0 || requestCode == 1)) {
      executeLogin(null);
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
