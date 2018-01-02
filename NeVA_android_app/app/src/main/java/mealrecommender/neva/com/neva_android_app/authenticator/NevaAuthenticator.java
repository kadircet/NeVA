package mealrecommender.neva.com.neva_android_app.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.AccessToken;
import mealrecommender.neva.com.neva_android_app.LoginActivity;
import mealrecommender.neva.com.neva_android_app.NevaLoginManager;
import mealrecommender.neva.com.neva_android_app.RegisterActivity;
import neva.backend.BackendOuterClass.LoginRequest.AuthenticationType;

/**
 * Created by hakan on 12/21/17.
 */

public class NevaAuthenticator extends AbstractAccountAuthenticator {

  private final String TAG = this.getClass().getSimpleName();
  private final Context mContext;

  public NevaAuthenticator(Context context) {
    super(context);
    this.mContext = context;
  }

  //Add new account to the device (to AccountManager).
  @Override
  public Bundle addAccount(AccountAuthenticatorResponse response,
      String accountType,
      String authTokenType,
      String[] requiredFeatures,
      Bundle options) throws NetworkErrorException {

    //Check for permissions.
    if (ActivityCompat.checkSelfPermission(mContext,
        android.Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
      Log.e(TAG, "GET_ACCOUNTS permission not present.");
    }

    //Fill the account data of the account to be created.
    final Intent intent = new Intent(mContext, RegisterActivity.class);
    intent.putExtra(LoginActivity.ACCOUNT_TYPE, accountType);
    intent.putExtra(LoginActivity.NEVA_TOKEN_TYPE, authTokenType);
    intent.putExtra(LoginActivity.IS_ADDING_NEW_ACCOUNT, true);
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
    final Bundle bundle = new Bundle();
    bundle.putParcelable(AccountManager.KEY_INTENT, intent);
    return bundle;
  }

  @Override
  public Bundle getAuthToken(AccountAuthenticatorResponse response,
      Account account,
      String authTokenType,
      Bundle options) throws NetworkErrorException {

    final AccountManager am = AccountManager.get(mContext);
    // Get the authToken for the account.
    String authToken = am.peekAuthToken(account, authTokenType);
    String password = am.getPassword(account);
    String isFacebookLogin = am.getUserData(account, LoginActivity.IS_FACEBOOK_LOGIN);
    Log.i(TAG, "Facebook Login: " + isFacebookLogin);
    if(isFacebookLogin.equalsIgnoreCase("true")) {
      Log.i(TAG, "Token Before Refresh: "+ AccessToken.getCurrentAccessToken());
      AccessToken.refreshCurrentAccessTokenAsync();
      String refreshed =AccessToken.getCurrentAccessToken().getToken();
      Log.i(TAG, "RefreshedToken: "+refreshed);
      password =refreshed;
      authToken = null;
      if (password != null) {
        Log.i(TAG, "Tryin to login again with saved password");
        NevaLoginManager.getInstance().logIn(account.name, password, AuthenticationType.FACEBOOK);
        authToken = NevaLoginManager.getInstance().getStringToken();
      }
    } else {
      // If there is no token, get one using the saved password for the user.
      if (TextUtils.isEmpty(authToken)) {
        Log.i(TAG, "AuthToken is empty");
        if (password != null) {
          Log.i(TAG, "Tryin to login again with saved password");
          NevaLoginManager.getInstance().logIn(account.name, password, AuthenticationType.DEFAULT);
          authToken = NevaLoginManager.getInstance().getStringToken();
        }
      }
    }
    // If authtoken isn't empty. It means we have an auth token already, we need to check it
    // and if it is valid we are done.
    if (!TextUtils.isEmpty(authToken)) {
      Log.i(TAG, "Auth Token not empty");
      Log.i(TAG, "Getting auth token from NevaLoginManager");
      NevaLoginManager nevaLoginManager = NevaLoginManager.getInstance();
      nevaLoginManager.setAuthToken(account.name, authToken);
      if(!nevaLoginManager.validateToken()) {
        Log.e(TAG, "AUTH TOKEN INVALID");
      }

      final Bundle result = new Bundle();
      result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
      result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
      result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
      return result;
    }
    // If we come to this point that means we have no authToken and we failed to get a new one
    // using the password. So we prompt the user to login again.
    final Intent intent = new Intent(mContext, LoginActivity.class);
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
    intent.putExtra(LoginActivity.ACCOUNT_TYPE, account.type);
    intent.putExtra(LoginActivity.NEVA_TOKEN_TYPE, authTokenType);
    final Bundle bundle = new Bundle();
    bundle.putParcelable(AccountManager.KEY_INTENT, intent);
    return bundle;
  }

  @Override
  public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse,
      String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
      Account account, Bundle bundle) throws NetworkErrorException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getAuthTokenLabel(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
      Account account, String s, Bundle bundle) throws NetworkErrorException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse,
      Account account, String[] strings) throws NetworkErrorException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account)
      throws NetworkErrorException {
    return super.getAccountRemovalAllowed(response, account);
  }
}
