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
  Button recommendButton;
  Button likeButton;
  Button dislikeButton;

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

    recommendButton = view.findViewById(R.id.fragment_recommend_button);
    likeButton = view.findViewById(R.id.like_button);
    dislikeButton = view.findViewById(R.id.dislike_button);
    recommendedView = view.findViewById(R.id.fragment_recommendation_field);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    recommendButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        GetSuggestionsTask getSuggestionsTask = new GetSuggestionsTask();
        getSuggestionsTask.execute();
      }
    });

    likeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        SendFeedbackTask feedbackTask = new SendFeedbackTask();
        feedbackTask.execute(true);
      }
    });

    dislikeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        SendFeedbackTask feedbackTask = new SendFeedbackTask();
        feedbackTask.execute(false);
      }
    });
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
        Log.e(TAG, "Suggestee Id: "+ Integer.toString(suggesteeId));
        Log.e(TAG, "Last Meal Choice Id: "+ Integer.toString(lastChoiceId));
        Log.e(TAG, "Timestamp: "+ Integer.toString(timestamp));
        Log.e(TAG, "lat: "+ Long.toString(latitude));
        Log.e(TAG, "lon: "+ Long.toString(longitude));
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
    String suggestionName;
    ArrayList<String> tagNames;

    @Override
    protected void onPreExecute() {
      flexboxLayout.removeAllViews();
      recommendButton.setEnabled(false);
      recommendedView.setText("");
    }

    @Override
    protected Void doInBackground(Void... voids) {
      try {
        List<Suggestion> suggestionList = connectionManager.getMultipleSuggestions();
        Random r = new Random();
        Suggestion suggestion = suggestionList.get(r.nextInt(suggestionList.size()));
        List<SuggestionOuterClass.Tag> tagList = suggestion.getTagsList();
        int[] tagIdList = new int[tagList.size()];
        for(int i=0; i<tagIdList.length; i++) {
          tagIdList[i] = tagList.get(i).getId();
        }
        List<String> tagNames = db.nevaDao().getTagNames(tagIdList);
        Log.i(TAG, Integer.toString(tagNames.size()));
        suggestionName = suggestion.getName();
        this.tagNames = (ArrayList<String>) tagNames;
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      recommendedView.setText(suggestionName);
      for (int i = 0; i < tagNames.size(); i++) {
        TextView textView = new TextView(getContext());
        textView.setText(tagNames.get(i));
        textView.setTextSize(16);
        textView.setTextColor(getResources().getColor(R.color.textPrimaryColor));
        textView.setPadding(16, 0, 16, 0);
        textView.setBackground(getResources().getDrawable(R.drawable.rounded_tagview_background, getContext().getTheme()));
        flexboxLayout.addView(textView);
      }
      recommendButton.setEnabled(true);
    }
  }

}
