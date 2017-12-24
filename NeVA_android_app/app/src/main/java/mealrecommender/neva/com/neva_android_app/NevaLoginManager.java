package mealrecommender.neva.com.neva_android_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;

/**
 * Created by hakan on 12/12/17.
 */

public class NevaLoginManager {
    private final static String TAG="NevaLoginManager";
    private static NevaLoginManager instance = null;

    private String username;
    private ByteString byteStringToken;
    private String stringToken;
    private boolean loggedIn;

    BackendGrpc.BackendBlockingStub blockingStub;
    ManagedChannel mChannel;

    protected NevaLoginManager() {
        mChannel = ManagedChannelBuilder.forAddress("neva.0xdeffbeef.com", 50051).build();
        blockingStub = BackendGrpc.newBlockingStub(mChannel);
    }

    public static NevaLoginManager getInstance(){
        if(instance == null){
            instance = new NevaLoginManager();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUsername() {
        if(loggedIn)
            return username;
        return null;
    }

    public ByteString getByteStringToken() {
        if(loggedIn)
            return byteStringToken;
        return null;
    }

    public String getStringToken() {
        if(loggedIn)
            return stringToken;
        return null;
    }

    public void logOut(){
        username = null;
        byteStringToken = null;
        stringToken = null;
        loggedIn = false;
    }


    public boolean logIn(String username, String password, BackendOuterClass.LoginRequest.AuthenticationType auth)
    {
        try {
            BackendOuterClass.LoginRequest loginRequest = BackendOuterClass.LoginRequest.newBuilder()
                    .setEmail(username)
                    .setPassword(password)
                    .setAuthenticationType(auth)
                    .build();

            BackendOuterClass.LoginReply loginReply = blockingStub.login(loginRequest);
            this.username = username;
            this.byteStringToken = loginReply.getToken();
            this.stringToken = Base64.encodeToString(byteStringToken.toByteArray(), Base64.DEFAULT);
            this.loggedIn = true;
            return true;
        }
        catch (Exception e)
        {
            Log.i(TAG, e.getMessage());
            return  false;
        }
    }
}
