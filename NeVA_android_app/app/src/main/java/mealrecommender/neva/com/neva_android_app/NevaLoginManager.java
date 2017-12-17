package mealrecommender.neva.com.neva_android_app;

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
    private ByteString loginToken;
    private boolean loggedIn;

    BackendGrpc.BackendBlockingStub blockingStub;
    ManagedChannel mChannel;

    protected NevaLoginManager() {
        mChannel = ManagedChannelBuilder.forAddress("www.0xdeffbeef.com", 50051).usePlaintext(true).build();
        blockingStub = BackendGrpc.newBlockingStub(mChannel);
    }

    public static NevaLoginManager getInstance(){
        if(instance == null){
            instance = new NevaLoginManager();
        }
        return instance;
    }

    public String getUsername() {
        if(loggedIn)
            return username;
        return null;
    }
    public ByteString getLoginToken() {
        if(loggedIn)
            return loginToken;
        return null;
    }

    public void logOut(){
        username = null;
        loginToken = null;
        loggedIn = false;
    }

    public ByteString logIn(String username, String password, BackendOuterClass.LoginRequest.AuthenticationType auth)
    {
        try {
            BackendOuterClass.LoginRequest loginRequest = BackendOuterClass.LoginRequest.newBuilder()
                    .setEmail(username)
                    .setPassword(password)
                    .setAuthenticationType(auth)
                    .build();

            BackendOuterClass.LoginReply loginReply = blockingStub.login(loginRequest);
            this.username = username;
            this.loginToken = loginReply.getToken();
            this.loggedIn = true;
            return loginToken;
        }
        catch (Exception e)
        {
            Log.i(TAG, e.getMessage());
            return  null;
        }
    }
}
