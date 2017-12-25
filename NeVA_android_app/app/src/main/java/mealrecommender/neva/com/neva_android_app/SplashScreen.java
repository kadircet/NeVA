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
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.lang.ref.WeakReference;
import neva.backend.BackendGrpc;
import neva.backend.BackendGrpc.BackendBlockingStub;
import neva.backend.BackendOuterClass.CheckTokenRequest;
import neva.backend.BackendOuterClass.GenericReply;
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
    // If we don't have an account on the phone, launch LoginActivity and wait for its result.
    Account accounts[] = accountManager.getAccountsByType(LoginActivity.ACCOUNT_TYPE);
    if (accounts == null || accounts.length < 1) {
      Log.i(TAG, "No accounts found, directing to LoginActivity");
      Intent intent = new Intent(this, LoginActivity.class);
      intent.putExtra(LoginActivity.IS_ADDING_NEW_ACCOUNT, true);
      startActivityForResult(intent, 1);
      return;
    }

    Account userAccount = accounts[0]; //TODO:Implement Account picker for multiple account support.
    Log.i(TAG, "Account found Logging in...");
    executeLogin(userAccount);
  }

  @SuppressLint("StaticFieldLeak")
  private void executeLogin(Account userAccount) {
    final Account loginAccount = userAccount;
    Log.i(TAG, "Getting authtoken from AccountManager");
    try {
      //BE CAREFUL THERE MAY BE A MEMORY LEAK HERE DUE TO ASYNCTASK LIVING LONGER THAN SPLASHSCREEN
      //When task gets the authtoken OnTokenAcquired.run() is called.
      new AsyncTask<Void, Void, Void>() {
        protected Void doInBackground(Void... voids) {
          accountManager.getAuthToken(loginAccount, LoginActivity.AUTH_TOKEN_TYPE, null,
              SplashScreen.this, new OnTokenAcquired(), null);
          return null;
        }
      }.execute();
    } catch (Exception e) {
      Log.e(TAG, "Error while getting authtoken.");
      e.printStackTrace();
    }
  }

  private void processToken(String token) {
    //TODO: Check Auth Token With Server
    Log.i(TAG, "Checking auth token");
    Log.i(TAG, "Token from Account Manager: " + token);
    Log.i(TAG, "Token from NevaLoginManager: " + NevaLoginManager.getInstance().getStringToken());
    //TODO: Move this check to NevaLoginManager
    CheckTokenRequest checkTokenRequest = CheckTokenRequest.newBuilder()
        .setToken(NevaLoginManager.getInstance().getByteStringToken()).build();
    ManagedChannel mChannel = ManagedChannelBuilder.forAddress("neva.0xdeffbeef.com", 50051)
        .build();
    BackendBlockingStub blockingStub = BackendGrpc.newBlockingStub(mChannel);

    try {
      GenericReply genericReply = blockingStub.checkToken(checkTokenRequest);
      Log.i(TAG, "TOKEN VALID");
      Log.i(TAG, "Launching MainActivity");
      Intent intent = new Intent(getBaseContext(), MainActivity.class);
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "TOKEN AUTH FAIL" + e.getMessage());
      e.printStackTrace();
    }
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
        Log.e(TAG, "Cannot get data from response bundle!");
        e.printStackTrace();
      }
      if (authIntent != null) {
        Log.i(TAG, "Response bundle contains Intent, directing to LoginActivity");
        startActivityForResult(authIntent, 0);
        return;
      }
      String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
      String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
      accountManager.invalidateAuthToken(LoginActivity.ACCOUNT_TYPE, token);
      NevaLoginManager.getInstance().setAuthToken(accountName, token); //Initiate NevaLoginManager
      processToken(token);
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
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
