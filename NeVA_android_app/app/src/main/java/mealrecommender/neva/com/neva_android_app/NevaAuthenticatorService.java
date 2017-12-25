package mealrecommender.neva.com.neva_android_app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NevaAuthenticatorService extends Service {

  // Instance field that stores the authenticator object
  // Notice, this is the same Authenticator class we defined earlier
  private static final String TAG = "NevaAuthenticatorSvc";
  private NevaAuthenticator mAuthenticator;

  @Override
  public void onCreate() {
    // Create a new authenticator object
    Log.i(TAG, "Service created");
    mAuthenticator = new NevaAuthenticator(this);
  }

  /*
   * When the system binds to this Service to make the RPC call
   * return the authenticator's IBinder.
   */
  @Override
  public IBinder onBind(Intent intent) {
    return mAuthenticator.getIBinder();
  }
}
