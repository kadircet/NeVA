package mealrecommender.neva.com.neva_android_app;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.SuggestionOuterClass;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProposeFragment extends Fragment {

    private static final String TAG = "ProposeFragment";

    ByteString loginToken;
    ManagedChannel mChannel;
    BackendGrpc.BackendBlockingStub blockingStub;

    EditText fragment_proposal_field;
    EditText fragment_tag_proposal_field;
    Button fragment_proposal_button;
    Button fragment_tag_proposal_button;

    public ProposeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_propose, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        loginToken = mainActivity.loginToken;
        mChannel = mainActivity.mChannel;
        blockingStub = mainActivity.blockingStub;

        fragment_proposal_field = view.findViewById(R.id.fragment_proposal_field);
        fragment_tag_proposal_field = view.findViewById(R.id.fragment_tag_proposal_field);

        fragment_proposal_button = view.findViewById(R.id.fragment_proposal_button);
        fragment_tag_proposal_button = view.findViewById(R.id.fragment_tag_proposal_button);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragment_proposal_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String suggestionText = fragment_proposal_field.getText().toString();
                fragment_proposal_button.setEnabled(false);

                SuggestionOuterClass.Suggestion suggestion;
                suggestion = SuggestionOuterClass.Suggestion.newBuilder()
                        .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
                        .setName(suggestionText)
                        .build();

                BackendOuterClass.SuggestionItemPropositionRequest suggestionRequest;
                suggestionRequest = BackendOuterClass.SuggestionItemPropositionRequest.newBuilder()
                        .setToken(loginToken)
                        .setSuggestion(suggestion)
                        .build();
                try
                {
                    BackendOuterClass.GenericReply genRep = blockingStub.suggestionItemProposition(suggestionRequest);
                    Toast.makeText(getContext(), "Meal Suggested", Toast.LENGTH_SHORT).show();
                    fragment_proposal_field.getText().clear();
                    fragment_proposal_button.setEnabled(true);
                }
                catch (Exception e)
                {
                    Log.d("SUGG_Click", "Exception");
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    fragment_proposal_button.setEnabled(true);
                }

            }
        });

        fragment_tag_proposal_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String tagString = fragment_tag_proposal_field.getText().toString();
                fragment_tag_proposal_button.setEnabled(false);

                BackendOuterClass.TagPropositionRequest tagProp;
                tagProp = BackendOuterClass.TagPropositionRequest.newBuilder()
                        .setTag(tagString).setToken(loginToken).build();

                try
                {
                    BackendOuterClass.GenericReply genRep = blockingStub.tagProposition(tagProp);
                    Toast.makeText(getContext(), "Tag Suggested", Toast.LENGTH_SHORT).show();
                    fragment_tag_proposal_field.getText().clear();
                    fragment_tag_proposal_button.setEnabled(true);
                }
                catch (Exception e)
                {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    fragment_tag_proposal_button.setEnabled(true);
                }


            }
        });


    }


}
