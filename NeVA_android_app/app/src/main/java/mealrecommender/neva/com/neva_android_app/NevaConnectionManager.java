package mealrecommender.neva.com.neva_android_app;

import static neva.backend.BackendGrpc.newBlockingStub;

import android.util.Log;
import android.widget.ProgressBar;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.List;
import neva.backend.BackendGrpc.BackendBlockingStub;
import neva.backend.BackendOuterClass;
import neva.backend.BackendOuterClass.CheckTokenRequest;
import neva.backend.BackendOuterClass.FetchUserHistoryReply;
import neva.backend.BackendOuterClass.FetchUserHistoryRequest;
import neva.backend.BackendOuterClass.GenericReply;
import neva.backend.BackendOuterClass.GetMultipleSuggestionsReply;
import neva.backend.BackendOuterClass.GetMultipleSuggestionsRequest;
import neva.backend.BackendOuterClass.GetSuggestionItemListReply;
import neva.backend.BackendOuterClass.GetSuggestionItemListRequest;
import neva.backend.BackendOuterClass.GetTagsReply;
import neva.backend.BackendOuterClass.GetTagsRequest;
import neva.backend.BackendOuterClass.GetUserReply;
import neva.backend.BackendOuterClass.GetUserRequest;
import neva.backend.BackendOuterClass.InformUserChoiceReply;
import neva.backend.BackendOuterClass.InformUserChoiceRequest;
import neva.backend.BackendOuterClass.LoginReply;
import neva.backend.BackendOuterClass.LoginRequest;
import neva.backend.BackendOuterClass.LoginRequest.AuthenticationType;
import neva.backend.BackendOuterClass.RecordFeedbackRequest;
import neva.backend.BackendOuterClass.RegisterRequest;
import neva.backend.BackendOuterClass.SuggestionItemPropositionRequest;
import neva.backend.BackendOuterClass.TagPropositionRequest;
import neva.backend.BackendOuterClass.TagValuePropositionRequest;
import neva.backend.BackendOuterClass.UpdateUserRequest;
import neva.backend.SuggestionOuterClass;
import neva.backend.SuggestionOuterClass.Suggestion;
import neva.backend.SuggestionOuterClass.Suggestion.SuggestionCategory;
import neva.backend.SuggestionOuterClass.Tag;
import neva.backend.UserHistoryOuterClass.Choice;
import neva.backend.UserHistoryOuterClass.UserFeedback;
import neva.backend.UserHistoryOuterClass.UserFeedback.Feedback;
import neva.backend.UserOuterClass;
import neva.backend.UserOuterClass.User;
import neva.backend.UserOuterClass.User.Gender;
import neva.backend.util.Util.Timestamp;

/**
 * Created by hakan on 1/8/18.
 */

public class NevaConnectionManager {

  private final String TAG = this.getClass().getSimpleName();
  private final String serverAddress = "neva.0xdeffbeef.com";
  private final int serverPort = 50051;
  private static NevaConnectionManager instance = null;

  private ManagedChannel managedChannel;
  private BackendBlockingStub blockingStub;

  protected NevaConnectionManager() {
    managedChannel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).build();
    blockingStub = newBlockingStub(managedChannel);
  }

  public static NevaConnectionManager getInstance() {
    if (instance == null) {
      instance = new NevaConnectionManager();
    }
    return instance;
  }

  public LoginReply loginRequest(String email, String password, AuthenticationType auth) {
    LoginRequest request = LoginRequest.newBuilder()
        .setEmail(email)
        .setPassword(password)
        .setAuthenticationType(auth)
        .build();

    try {
      return blockingStub.login(request);
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return null;
    }
  }

  public boolean registerRequest(String username, String email, String password, Gender gender,
      Timestamp bdate) {
    UserOuterClass.User user = UserOuterClass.User.newBuilder().setName(username).setEmail(email)
        .setGender(gender).setDateOfBirth(bdate).setPassword(password).build();

    RegisterRequest registerRequest = RegisterRequest.newBuilder().setUser(user).build();
    try {
      blockingStub.register(registerRequest);
      return true;
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return false;
    }
  }

  public boolean checkToken(ByteString token) {
    CheckTokenRequest request = CheckTokenRequest.newBuilder().setToken(token).build();
    try {
      GenericReply reply = blockingStub.checkToken(request);
      return true;
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return false;
    }
  }

  public List<Tag> getTags(int tagTableVersion) {
    GetTagsRequest request = GetTagsRequest.newBuilder()
        .setToken(NevaLoginManager.getInstance().getByteStringToken())
        .setStartIndex(tagTableVersion)
        .build();
    try {
      GetTagsReply reply = blockingStub.getTags(request);
      return reply.getTagListList();
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return null;
    }
  }

  public GetSuggestionItemListReply getSuggestions(SuggestionCategory category,
      int mealTableVersion) {
    GetSuggestionItemListRequest request = GetSuggestionItemListRequest.newBuilder()
        .setToken(NevaLoginManager.getInstance().getByteStringToken())
        .setSuggestionCategory(category)
        .setStartIndex(mealTableVersion)
        .build();

    try {
      return blockingStub.getSuggestionItemList(request);
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return null;
    }
  }

  public boolean tagValueProposition(int mealId, int tagId) {
    TagValuePropositionRequest request = TagValuePropositionRequest.newBuilder()
        .setToken(NevaLoginManager.getInstance().getByteStringToken())
        .setSuggesteeId(mealId).setTagId(tagId).build();
    try {
      GenericReply reply = blockingStub.tagValueProposition(request);
      return true;
    } catch (Exception e) {
      Log.d(TAG, "MealTag Exception: " + e.getMessage());
      return false;
    }
  }

  public boolean suggestionItemProposition(String suggestionName) {
    Suggestion suggestion = Suggestion.newBuilder()
        .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
        .setName(suggestionName)
        .build();

    SuggestionItemPropositionRequest request = SuggestionItemPropositionRequest.newBuilder()
        .setToken(NevaLoginManager.getInstance().getByteStringToken())
        .setSuggestion(suggestion)
        .build();

    try {
      GenericReply genRep = blockingStub.suggestionItemProposition(request);
      return true;
    } catch (Exception e) {
      Log.d("SUGG_Click", "Exception: " + e.getMessage());
      return false;
    }
  }

  public boolean tagProposition(String tagName) {
    TagPropositionRequest request = TagPropositionRequest.newBuilder()
        .setToken(NevaLoginManager.getInstance().getByteStringToken())
        .setTag(tagName).build();
    try {
      BackendOuterClass.GenericReply genRep = blockingStub.tagProposition(request);
      return true;
    } catch (Exception e) {
      Log.d(TAG, "Tag Propose Exception: " + e.getMessage());
      return false;
    }
  }

  public List<Choice> fetchUserHistory(int startIndex) {
    FetchUserHistoryRequest fetchUserHistoryRequest = FetchUserHistoryRequest.newBuilder()
        .setToken(NevaLoginManager.getInstance().getByteStringToken())
        .setStartIndex(startIndex)
        .build();
    try {
      FetchUserHistoryReply reply = blockingStub.fetchUserHistory(fetchUserHistoryRequest);
      return reply.getUserHistory().getHistoryList();

    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return null;
    }
  }

  public InformUserChoiceReply informUserChoice(int mealId, long epochTime, double latitude,
      double longitude) {
    Choice choice = Choice.newBuilder()
        .setSuggesteeId(mealId)
        .setTimestamp(Timestamp.newBuilder()
            .setSeconds((int) (epochTime)))
        .setLatitude(latitude)
        .setLongitude(longitude)
        .build();

    Log.d(TAG, "Created choice with: ");
    Log.d(TAG, "\tMeal Id: " + Integer.toString(mealId));
    Log.d(TAG, "\tTimestamp(ms): " + Long.toString(epochTime));
    Log.d(TAG,
        "\tLatitude: " + Double.toString(latitude) + " Longitude: " + Double.toString(longitude));

    InformUserChoiceRequest request = InformUserChoiceRequest.newBuilder()
        .setChoice(choice)
        .setToken(NevaLoginManager.getInstance().getByteStringToken())
        .build();
    try {
      Log.d(TAG, "Sending \"Choice\" to server");
      return blockingStub.informUserChoice(request);
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return null;
    }
  }

  public List<Suggestion> getMultipleSuggestions() {
    GetMultipleSuggestionsRequest request = GetMultipleSuggestionsRequest.newBuilder()
        .setToken(NevaLoginManager.getInstance().getByteStringToken())
        .setSuggestionCategory(SuggestionCategory.MEAL)
        .build();
    try {
      GetMultipleSuggestionsReply reply = blockingStub.getMultipleSuggestions(request);
      return reply.getSuggestion().getSuggestionListList();
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return null;
    }
  }

  public User getUser() {
    GetUserRequest request = GetUserRequest.newBuilder()
        .setToken(NevaLoginManager.getInstance().getByteStringToken()).build();

    try {
      GetUserReply reply = blockingStub.getUser(request);
      return reply.getUser();
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return null;
    }

  }

  public boolean updateUser(User updatedUserData) {
    try{
      UpdateUserRequest request = UpdateUserRequest.newBuilder().setToken(NevaLoginManager.getInstance().getByteStringToken())
          .setUser(updatedUserData).build();
      GenericReply reply = blockingStub.updateUser(request);
      return true;
    } catch (Exception e){
      Log.e(TAG, e.getMessage());
      return false;
    }
  }

  public boolean sendFeedback(boolean like, int lastChoiceId, int suggesteeId, int timestamp, long latitude, long longitude) {
    UserFeedback feedback;
    Choice meal = Choice.newBuilder().setChoiceId(lastChoiceId)
        .setSuggesteeId(suggesteeId)
        .setTimestamp(Timestamp.newBuilder().setSeconds(timestamp).build())
        .setLatitude(latitude)
        .setLongitude(longitude)
        .build();
    if(like) {
      feedback = UserFeedback.newBuilder().setFeedback(Feedback.LIKE).setChoice(meal).build();
    } else {
      feedback = UserFeedback.newBuilder().setFeedback((Feedback.DISLIKE)).setChoice(meal).build();
    }
    try {
      RecordFeedbackRequest request = RecordFeedbackRequest.newBuilder()
          .setToken(NevaLoginManager.getInstance().getByteStringToken())
          .setUserFeedback(feedback).build();
      
      Log.d(TAG, "Suggestee Id: "+ Integer.toString(suggesteeId));
      Log.d(TAG, "Last Meal Choice Id: "+ Integer.toString(lastChoiceId));
      Log.d(TAG, "Timestamp: "+ Integer.toString(timestamp));
      Log.d(TAG, "Latitude: "+ Long.toString(latitude));
      Log.d(TAG, "Longiture: "+ Long.toString(longitude));
      Log.d(TAG, "Like:" + Boolean.toString(like));
      GenericReply reply = blockingStub.recordFeedback(request);
      return true;
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      return false;
    }
  }

}
