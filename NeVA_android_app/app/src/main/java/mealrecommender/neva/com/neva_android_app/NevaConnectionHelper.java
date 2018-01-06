package mealrecommender.neva.com.neva_android_app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import neva.backend.BackendGrpc;
import neva.backend.BackendGrpc.BackendBlockingStub;
import neva.backend.BackendGrpc.BackendStub;
import neva.backend.BackendOuterClass.LoginReply;
import neva.backend.BackendOuterClass.LoginRequest;
import neva.backend.BackendOuterClass.LoginRequest.AuthenticationType;
import neva.backend.SuggestionOuterClass.Suggestion;
import neva.backend.SuggestionOuterClass.Suggestion.SuggestionCategory;
import neva.backend.SuggestionOuterClass.Tag;
import neva.backend.UserHistoryOuterClass.Choice;

/**
 * Created by Hakan on 1/6/2018.
 */

public class NevaConnectionHelper {
  private static final String TAG = "NevaConnecitonManager";
  private static NevaConnectionHelper instance = null;
  private String serverName;
  private int serverPort;
  private Context context;
  private ManagedChannel managedChannel;
  private BackendBlockingStub blockingStub;
  private BackendStub asyncStub;
  private ByteString loginToken;

  protected NevaConnectionHelper(Context context) {
    this.context = context;
    serverName = context.getResources().getString(R.string.server_name);
    serverPort = context.getResources().getInteger(R.integer.server_port);
    managedChannel = ManagedChannelBuilder.forAddress(serverName,serverPort).build();
    blockingStub = BackendGrpc.newBlockingStub(managedChannel);
    asyncStub = BackendGrpc.newStub(managedChannel);
  }

  public static NevaConnectionHelper getInstance(Context context) {
    if(context == null) {
      Log.e(TAG, "Context is null while creating NevaConnectionManager.");
      throw new IllegalArgumentException("Null Context");
    }
    if(instance == null) {
      instance = new NevaConnectionHelper(context);
    }
    return instance;
  }

  public static NevaConnectionHelper getInstance() throws IllegalStateException {
    if(instance == null) {
      Log.e(TAG, "NevaConnectionManager isn't initiliazed correctly, instance is null");
      Log.e(TAG, "Cannot create instance without Context");
      throw new IllegalStateException("NevaConnectionHelper instance is null");
    }
    return instance;
  }

  public ByteString login(String email, String password, AuthenticationType auth) {
    LoginRequest request = LoginRequest.newBuilder().setEmail(email).setPassword(password)
        .setAuthenticationType(auth).build();

    StreamObserver<LoginReply> responseObserver = new StreamObserver<LoginReply>() {
      @Override
      public void onNext(LoginReply value) {
        loginToken = value.getToken();
        Log.i(TAG, "Got login token");
      }

      @Override
      public void onError(Throwable t) {
        Log.e(TAG, "Login ResponseObserver" + t.getMessage());
      }

      @Override
      public void onCompleted() {
        Log.i(TAG, "Login ResponseObserver completed");
      }
    };

    asyncStub.login(request, responseObserver);
    if(loginToken != null) {
      return loginToken;
    }
    return null;
    /*try {
      LoginReply reply = blockingStub.login(request);
      return reply.getToken();
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return null;
    }*/
  }

  public boolean validateToken(ByteString token) {
    return true;
  }

  public ArrayList<Tag> getMultipleSuggestions(SuggestionCategory category, ByteString token) {
    return null;
  }

  public boolean proposeMealName(String mealName, SuggestionCategory category, ByteString token) {
    return true;
  }

  public boolean proposeTagName(String tagName, SuggestionCategory category, ByteString token) {
    return true;
  }

  public boolean proposeMealTagValue(String mealName, String tagName, ByteString token) {
    return true;
  }

  public int informUserChoice() { return 0;}
  public ArrayList<Choice> fetchUserHistory() { return null;}
  public ArrayList<Tag> getTags() { return null; }
  public ArrayList<Suggestion> getMeals() {return null;}

}
