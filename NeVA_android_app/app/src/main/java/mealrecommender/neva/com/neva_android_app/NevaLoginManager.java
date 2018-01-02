package mealrecommender.neva.com.neva_android_app;

import android.util.Base64;
import android.util.Log;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.BackendOuterClass.CheckTokenRequest;
import neva.backend.BackendOuterClass.GenericReply;

/**
 * Created by hakan on 12/12/17.
 */

public class NevaLoginManager {

  private final String TAG = this.getClass().getSimpleName();
  private final String serverAddress = "neva.0xdeffbeef.com";
  private final int serverPort = 50051;
  private static NevaLoginManager instance = null;
  private String username;
  private ByteString byteStringToken;
  private String stringToken;
  private boolean loggedIn;

  ManagedChannel mChannel;
  public BackendGrpc.BackendBlockingStub blockingStub;

  protected NevaLoginManager() {
    mChannel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).build();
    blockingStub = BackendGrpc.newBlockingStub(mChannel);
  }

  public static NevaLoginManager getInstance() {
    if (instance == null) {
      instance = new NevaLoginManager();
    }
    return instance;
  }

  public boolean isLoggedIn() {
    return loggedIn;
  }

  public String getUsername() {
    if (loggedIn) {
      return username;
    }
    return null;
  }

  public ByteString getByteStringToken() {
    if (loggedIn) {
      return byteStringToken;
    }
    return null;
  }

  public String getStringToken() {
    if (loggedIn) {
      return stringToken;
    }
    return null;
  }

  public void logOut() {
    username = null;
    byteStringToken = null;
    stringToken = null;
    loggedIn = false;
  }

  public void setAuthToken(String username, String token) {
    stringToken = token;
    byteStringToken = ByteString.copyFrom(Base64.decode(token, Base64.DEFAULT));
    this.username = username;
    loggedIn = true;
  }
  
  public boolean logIn(String username, String password,
      BackendOuterClass.LoginRequest.AuthenticationType auth) {
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
    } catch (Exception e) {
      e.printStackTrace();
      Log.i(TAG, e.getMessage());
      return false;
    }
  }

  public boolean validateToken() {
    CheckTokenRequest checkTokenRequest = CheckTokenRequest.newBuilder()
        .setToken(getByteStringToken()).build();
    try{
      GenericReply genericReply = blockingStub.checkToken(checkTokenRequest);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
