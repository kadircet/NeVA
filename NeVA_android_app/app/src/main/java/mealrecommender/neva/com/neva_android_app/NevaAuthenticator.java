package mealrecommender.neva.com.neva_android_app;

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
import android.util.Base64;
import android.util.Log;

import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;

/**
 * Created by hakan on 12/21/17.
 */

public class NevaAuthenticator extends AbstractAccountAuthenticator {

    private static final String TAG = "NevaAcc.Authenticator";
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
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(LoginActivity.ACCOUNT_TYPE, accountType);
        intent.putExtra(LoginActivity.AUTH_TOKEN_TYPE, authTokenType);
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
        // If there is no token, get one using the saved password for the user.
        if(TextUtils.isEmpty(authToken))
        {
            final String password = am.getPassword(account);
            if (password != null){
                BackendOuterClass.LoginRequest request = BackendOuterClass.LoginRequest.newBuilder()
                    .setAuthenticationType(BackendOuterClass.LoginRequest.AuthenticationType.DEFAULT)
                    .setEmail(account.name)
                    .setPassword(password)
                    .build();

                ManagedChannel mChannel = ManagedChannelBuilder.forAddress("neva.0xdeffbeef.com", 50051)
                    .build();
                BackendGrpc.BackendBlockingStub blockingStub = BackendGrpc.newBlockingStub(mChannel);
                byte[] byteArrayToken = null;
                try {
                    BackendOuterClass.LoginReply reply = blockingStub.login(request);
                    ByteString tokenByteString = reply.getToken();
                    byteArrayToken = tokenByteString.toByteArray();
                    authToken = Base64.encodeToString(byteArrayToken, Base64.DEFAULT);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
        // If authtoken isn't empty. It means we have an auth token already, we need to check it
        // and if it is valid we are done.
        if (!TextUtils.isEmpty(authToken)) {
            //TODO: Validate the authToken with the server.
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
        intent.putExtra(LoginActivity.AUTH_TOKEN_TYPE, authTokenType);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        return super.getAccountRemovalAllowed(response, account);
    }
}
