package mealrecommender.neva.com.neva_android_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.facebook.login.LoginManager;

public class SplashScreen extends AppCompatActivity {

  public final String TAG = this.getClass().getSimpleName();
  AccountManager accountManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    accountManager = AccountManager.get(getBaseContext());
    checkAccounts();
  }

  private void checkAccounts() {
    // If we already have a NeVA account, try to login automatically with the saved password.
    // If we don't have an account on the phone, launch LoginActivity and wait for its result.
    Account accounts[] = accountManager.getAccountsByType(LoginActivity.ACCOUNT_TYPE);
    if (accounts == null || accounts.length < 1) {
      Log.i(TAG, "No accounts found, directing to LoginActivity");
      Intent intent = new Intent(this, LoginActivity.class);
      intent.putExtra(LoginActivity.IS_ADDING_NEW_ACCOUNT, true);
      startActivityForResult(intent, 1);
      LoginManager.getInstance().logOut();
      return;
    }

    Account userAccount = accounts[0]; //TODO:Implement Account picker for multiple account support.
    Log.i(TAG, "Account found Logging in...");
    executeLogin(userAccount);
  }

  @SuppressLint("StaticFieldLeak")
  private void executeLogin(Account userAccount) {
    final Account loginAccount = userAccount;
    Toast.makeText(getBaseContext(), "Logging in with account: " + loginAccount.name,
        Toast.LENGTH_LONG).show();
    Log.i(TAG, "Getting authtoken from AccountManager");
    try {
      //BE CAREFUL THERE MAY BE A MEMORY LEAK HERE DUE TO ASYNCTASK LIVING LONGER THAN SPLASHSCREEN
      //When task gets the authtoken OnTokenAcquired.run() is called.
      new AsyncTask<Void, Void, Void>() {
        protected Void doInBackground(Void... voids) {
          accountManager.getAuthToken(loginAccount, LoginActivity.NEVA_TOKEN_TYPE, null,
              SplashScreen.this, new OnTokenAcquired(), null);
          return  null;
        }
      }.execute();
    } catch (Exception e) {
      Log.e(TAG, "Error while getting authtoken.");
      e.printStackTrace();
    }
  }

  private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
    @Override
    public void run(AccountManagerFuture<Bundle> result) {
      Bundle bundle = null;
      Intent authIntent = null;
      try {
        bundle = result.getResult();
        authIntent = (Intent) bundle.get(AccountManager.KEY_INTENT);
        if (authIntent != null) {
          Log.i(TAG, "Response bundle contains Intent, directing to LoginActivity");
          Toast.makeText(getBaseContext(), getResources().getString(R.string.error_authtoken), Toast.LENGTH_LONG).show();
          startActivityForResult(authIntent, 0);
          return;
        }
        String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
        String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
        accountManager.invalidateAuthToken(LoginActivity.ACCOUNT_TYPE, token);
        NevaLoginManager.getInstance().setAuthToken(accountName, token); //Initiate NevaLoginManager
        processToken(token);
      } catch (Exception e) {
        Log.e(TAG, "Cannot get data from response bundle!");
        e.printStackTrace();
      }
    }
  }

  private void processToken(String token) {
    Log.i(TAG, "Checking auth token");
    Log.i(TAG, "Token from Account Manager: " + token);
    Log.i(TAG, "Token from NevaLoginManager: " + NevaLoginManager.getInstance().getStringToken());

    if(NevaLoginManager.getInstance().validateToken()){
      Log.i(TAG, "TOKEN VALID");
      Log.i(TAG, "Launching MainActivity");
      Intent intent = new Intent(getBaseContext(), MainActivity.class);
      startActivity(intent);
      finish();
    } else {
      Log.e(TAG, "TOKEN AUTH INVALID");
      Toast.makeText(getBaseContext(), getResources().getString(R.string.error_authtoken), Toast.LENGTH_LONG).show();
      finishAndRemoveTask();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.i(TAG, "Login activity returned");
    Log.i(TAG,
        "Request Code: " + Integer.toString(requestCode) + " Return Code: " + Integer
            .toString(resultCode));
    if (resultCode == RESULT_OK && (requestCode == 0 || requestCode == 1)) {
      Log.i(TAG, "Checking acocunts again");
      checkAccounts();
    } else {
      Toast.makeText(getBaseContext(), getResources().getString(R.string.error_login), Toast.LENGTH_LONG).show();
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
