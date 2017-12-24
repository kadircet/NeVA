package mealrecommender.neva.com.neva_android_app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NevaAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        NevaAuthenticator authenticator = new NevaAuthenticator(this);
        return authenticator.getIBinder();
    }
}
