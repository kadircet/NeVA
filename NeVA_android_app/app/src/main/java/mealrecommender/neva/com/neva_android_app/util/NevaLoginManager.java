package mealrecommender.neva.com.neva_android_app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import com.google.protobuf.ByteString;
import mealrecommender.neva.com.neva_android_app.activities.SplashScreen;
import neva.backend.BackendOuterClass;
import neva.backend.BackendOuterClass.LoginReply;
import neva.backend.BackendOuterClass.LoginRequest.AuthenticationType;

/**
 * Created by hakan on 12/12/17.
 */

public class NevaLoginManager {

  private final String TAG = this.getClass().getSimpleName();
  private static NevaLoginManager instance = null;
  private String email;
  private ByteString byteStringToken;
  private String stringToken;
  private boolean loggedIn;
  private NevaConnectionManager connectionManager;

  protected NevaLoginManager() {
    connectionManager = NevaConnectionManager.getInstance();
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

  public String getEmail() {
    if (loggedIn) {
      return email;
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
    email = null;
    byteStringToken = null;
    stringToken = null;
    loggedIn = false;
  }

  public void setAuthToken(String email, String token) {
    stringToken = token;
    byteStringToken = ByteString.copyFrom(Base64.decode(token, Base64.DEFAULT));
    this.email = email;
    loggedIn = true;
  }
  
  public boolean logIn(String email, String password,
      BackendOuterClass.LoginRequest.AuthenticationType auth) {
    try {
      LoginReply loginReply = connectionManager.loginRequest(email, password, auth);
      Log.i(TAG, loginReply.getToken().toString());
      this.email = email;
      this.byteStringToken = loginReply.getToken();
      this.stringToken = Base64.encodeToString(byteStringToken.toByteArray(), Base64.DEFAULT);
      this.loggedIn = true;
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      Log.i(TAG, e.getMessage());
      this.loggedIn = false;
      return false;
    }

  }

  public boolean validateToken() {
      return connectionManager.checkToken(getByteStringToken()); //blockingStub.checkToken(checkTokenRequest);
  }
}