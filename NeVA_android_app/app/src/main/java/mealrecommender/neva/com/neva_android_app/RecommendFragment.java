package mealrecommender.neva.com.neva_android_app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.flexbox.FlexboxLayout;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import java.util.List;
import java.util.Random;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass.GetMultipleSuggestionsReply;
import neva.backend.BackendOuterClass.GetMultipleSuggestionsRequest;
import neva.backend.SuggestionOuterClass;
import neva.backend.SuggestionOuterClass.Suggestion;


public class RecommendFragment extends Fragment {

  private final String TAG = this.getClass().getSimpleName();

  FlexboxLayout flexboxLayout;
  ByteString loginToken;
  ManagedChannel mChannel;
  BackendGrpc.BackendBlockingStub blockingStub;
  NevaDatabase db;

  TextView recommendedView;
  Button recommendButton;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_recommend, null);

    MainActivity mainActivity = (MainActivity) getActivity();
    loginToken = mainActivity.loginToken;
    mChannel = mainActivity.mChannel;
    blockingStub = mainActivity.blockingStub;
    db = mainActivity.db;
    flexboxLayout = view.findViewById(R.id.flexbox_layout);

    recommendButton = view.findViewById(R.id.fragment_recommend_button);
    recommendedView = view.findViewById(R.id.fragment_recommendation_field);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    recommendButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        flexboxLayout.removeAllViews();
        GetMultipleSuggestionsRequest recommendationReq;
        recommendationReq = GetMultipleSuggestionsRequest.newBuilder()
            .setToken(loginToken)
            .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
            .build();

        GetMultipleSuggestionsReply recommendationRep;
        try {
          recommendationRep = blockingStub.getMultipleSuggestions(recommendationReq);
          List<Suggestion> suggestionList = recommendationRep.getSuggestion().getSuggestionListList();
          Random r = new Random();
          Suggestion suggestion = suggestionList.get(r.nextInt(suggestionList.size()));
          List<SuggestionOuterClass.Tag> tagList = suggestion.getTagsList();
          int tagIdList[] = new int[tagList.size()];
          for(int i =0; i<tagIdList.length; i++) {
            tagIdList[i] = tagList.get(i).getId();
          }
          String[] tagNames = db.nevaDao().getTagNames(tagIdList);
          recommendedView.setText(suggestion.getName());
          for(int i=0; i < tagNames.length; i++) {
            TextView textView = new TextView(getContext());
            textView.setText(tagNames[i]);
            textView.setTextSize(16);
            textView.setTextColor(getResources().getColor(R.color.textPrimaryColor));
            textView.setPadding(16,0,16,0);
            textView.setBackground(getResources().getDrawable(R.drawable.rounded_tagview_background));
            flexboxLayout.addView(textView);
          }

        } catch (Exception e) {
          Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
      }
    });
  }
}
