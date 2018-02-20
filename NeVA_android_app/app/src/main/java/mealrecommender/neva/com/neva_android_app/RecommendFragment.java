package mealrecommender.neva.com.neva_android_app;

import android.content.res.Resources.Theme;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.flexbox.FlexboxLayout;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import mealrecommender.neva.com.neva_android_app.database.Tag;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass.GetMultipleSuggestionsReply;
import neva.backend.BackendOuterClass.GetMultipleSuggestionsRequest;
import neva.backend.SuggestionOuterClass;
import neva.backend.SuggestionOuterClass.Suggestion;
import neva.backend.SuggestionOuterClass.Suggestion.Builder;
import neva.backend.SuggestionOuterClass.Suggestion.SuggestionCategory;


public class RecommendFragment extends Fragment {

  private final String TAG = this.getClass().getSimpleName();

  FlexboxLayout flexboxLayout;
  ByteString loginToken;

  NevaDatabase db;
  NevaConnectionManager connectionManager;

  TextView recommendedView;
  Button likeButton;
  Button dislikeButton;

  LinkedList<Suggestion> suggestionList;
  private int lastDisplayIndex;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_recommend, null);

    MainActivity mainActivity = (MainActivity) getActivity();
    loginToken = mainActivity.loginToken;
    db = mainActivity.db;
    connectionManager = NevaConnectionManager.getInstance();
    flexboxLayout = view.findViewById(R.id.flexbox_layout);
    suggestionList = new LinkedList<Suggestion>();
    lastDisplayIndex = 0;

    likeButton = view.findViewById(R.id.like_button);
    dislikeButton = view.findViewById(R.id.dislike_button);
    recommendedView = view.findViewById(R.id.fragment_recommendation_field);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    displayNextSuggestion();

    likeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        likeButton.setEnabled(false);
        if(recommendedView.getText().length() > 0) {
          SendFeedbackTask feedbackTask = new SendFeedbackTask();
          feedbackTask.execute(true);
          displayNextSuggestion();
        }
        likeButton.setEnabled(true);
      }
    });

    dislikeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        dislikeButton.setEnabled(false);
        if(recommendedView.getText().length() > 0) {
          SendFeedbackTask feedbackTask = new SendFeedbackTask();
          feedbackTask.execute(false);
          displayNextSuggestion();
        }
        dislikeButton.setEnabled(true);
      }
    });
  }

  public void displayNextSuggestion() {
    if(suggestionList.isEmpty()) {
      suggestionList = getSuggestionsBlocking();
    }
    flexboxLayout.removeAllViews();
    Suggestion suggestion = suggestionList.pop();//get(lastDisplayIndex);
    if(suggestionList.size() == 1) {
      GetSuggestionsTask getSuggestionsTask = new GetSuggestionsTask();
      getSuggestionsTask.execute();
    }
    List<SuggestionOuterClass.Tag> tagList = suggestion.getTagsList();
    int[] tagIds = new int[tagList.size()];
    for (int i = 0; i < tagIds.length; i++) {
      tagIds[i] = tagList.get(i).getId();
    }
    ArrayList<String> tagNames = (ArrayList<String>) db.nevaDao().getTagNames(tagIds);
    recommendedView.setText(suggestion.getName());
    for (int i = 0; i < tagNames.size(); i++) {
      TextView tagHolder = new TextView(getContext());
      tagHolder.setText(tagNames.get(i));
      tagHolder.setTextSize(16);
      tagHolder.setTextColor(getResources().getColor(R.color.textPrimaryColor));
      tagHolder.setPadding(16, 0, 16, 0);
      tagHolder.setBackground(getResources()
          .getDrawable(R.drawable.rounded_tagview_background, getContext().getTheme()));
      flexboxLayout.addView(tagHolder);
    }
  }


  class SendFeedbackTask extends AsyncTask<Boolean, Void, Boolean> {

    int lastChoiceId;
    int suggesteeId;
    long latitude;
    long longitude;
    int timestamp;

    @Override
    protected void onPreExecute() {
      likeButton.setEnabled(false);
      dislikeButton.setEnabled(false);
    }

    @Override
    protected Boolean doInBackground(Boolean... booleans) {
      try {
        suggesteeId = db.nevaDao().getMealId(recommendedView.getText().toString());
        lastChoiceId = db.nevaDao()
            .getLastChoiceIdOfUser(NevaLoginManager.getInstance().getEmail());
        timestamp = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
        latitude = 0;
        longitude = 0;
        boolean like = booleans[0];

        return connectionManager
            .sendFeedback(like, lastChoiceId, suggesteeId, timestamp, latitude, longitude);
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
        return false;
      }
    }

    @Override
    protected void onPostExecute(Boolean success) {
      if(success) {
        Toast.makeText(getContext(), "Feedback Success", Toast.LENGTH_SHORT).show();
      }
      else {
        Toast.makeText(getContext(), "Problem sending Feedback", Toast.LENGTH_SHORT).show();
      }
      likeButton.setEnabled(true);
      dislikeButton.setEnabled(true);
    }
  }

  class GetSuggestionsTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
      try {
        for(Suggestion sug : connectionManager.getMultipleSuggestions()) {
          suggestionList.add(sug);
        };
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
      }
      return null;
    }

  }

  public LinkedList<Suggestion> getSuggestionsBlocking() {
    try {
      LinkedList<Suggestion> res = new LinkedList<>();
      for (Suggestion sug: connectionManager.getMultipleSuggestions()) {
        Log.e(TAG, sug.getName());
        res.add(sug);
      }

      return res;
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
    return null;
  }

}
