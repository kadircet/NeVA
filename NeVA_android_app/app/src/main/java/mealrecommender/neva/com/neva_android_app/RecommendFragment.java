package mealrecommender.neva.com.neva_android_app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.flexbox.FlexboxLayout;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import neva.backend.SuggestionOuterClass;
import neva.backend.SuggestionOuterClass.Suggestion;


public class RecommendFragment extends Fragment {

  private final String TAG = this.getClass().getSimpleName();

  FlexboxLayout flexboxLayout;
  ByteString loginToken;

  NevaDatabase db;
  NevaConnectionManager connectionManager;

  TextView recommendedView;
  Button likeButton;
  Button dislikeButton;

  ConcurrentLinkedQueue<Suggestion> suggestionList;

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
    suggestionList = new ConcurrentLinkedQueue<Suggestion>();
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
        if (recommendedView.getText().length() > 0) {
          SendFeedbackTask feedbackTask = new SendFeedbackTask();
          feedbackTask.execute(true);
          displayNextSuggestion();
        }
      }
    });

    dislikeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (recommendedView.getText().length() > 0) {
          SendFeedbackTask feedbackTask = new SendFeedbackTask();
          feedbackTask.execute(false);
          displayNextSuggestion();
        }
      }
    });
  }

  public void displayNextSuggestion() {
    if (suggestionList.isEmpty()) {
      suggestionList = getSuggestionsBlocking();
    }
    flexboxLayout.removeAllViews();
    Suggestion suggestion = suggestionList.poll();
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

  public ConcurrentLinkedQueue<Suggestion> getSuggestionsBlocking() {
    try {
      ConcurrentLinkedQueue<Suggestion> res = new ConcurrentLinkedQueue<>();
      for (Suggestion sug : connectionManager.getMultipleSuggestions()) {
        res.offer(sug);
      }
      return res;
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
    return null;
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
      if (!success) {
        Snackbar.make(getView(), "Problem sending Feedback", Snackbar.LENGTH_LONG).show();
      }
      likeButton.setEnabled(true);
      dislikeButton.setEnabled(true);
    }
  }

  class GetSuggestionsTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
      try {
        for (Suggestion sug : connectionManager.getMultipleSuggestions()) {
          suggestionList.offer(sug);
        }
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
      }
      return null;
    }
  }
}
