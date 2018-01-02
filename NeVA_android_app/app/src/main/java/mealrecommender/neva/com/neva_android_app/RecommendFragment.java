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

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import java.util.List;
import java.util.Random;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.BackendOuterClass.GetMultipleSuggestionsReply;
import neva.backend.BackendOuterClass.GetMultipleSuggestionsRequest;
import neva.backend.SuggestionOuterClass;
import neva.backend.SuggestionOuterClass.Suggestion;


public class RecommendFragment extends Fragment {

  private final String TAG = this.getClass().getSimpleName();

  ByteString loginToken;
  ManagedChannel mChannel;
  BackendGrpc.BackendBlockingStub blockingStub;

  TextView recommendedView;
  Button recommendButton;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_recommend, null);

    MainActivity mainActivity = (MainActivity) getActivity();
    loginToken = mainActivity.loginToken;
    mChannel = mainActivity.mChannel;
    blockingStub = mainActivity.blockingStub;

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
          recommendedView.setText(suggestion.getName());

        } catch (Exception e) {
          Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
      }
    });
  }
}
