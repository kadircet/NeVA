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
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.SuggestionOuterClass;


public class RecommendFragment extends Fragment {

    private static final String TAG = "RecommendFragment";

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
                BackendOuterClass.GetSuggestionRequest recommendationReq;
                recommendationReq = BackendOuterClass.GetSuggestionRequest.newBuilder()
                        .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
                        .setToken(loginToken).build();

                BackendOuterClass.GetSuggestionReply recommendationRep;
                try {
                    recommendationRep = blockingStub.getSuggestion(recommendationReq);
                    recommendedView.setText(recommendationRep.getName());

                }
                catch (Exception e)
                {
                    Toast.makeText(getContext(),  e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
