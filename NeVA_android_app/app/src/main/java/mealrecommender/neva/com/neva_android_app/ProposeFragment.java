package mealrecommender.neva.com.neva_android_app;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.sql.SQLException;

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

  AutoCompleteTextView meal_for_tag_field;
  EditText tag_of_meal_field;
  Button propose_tag_for_meal;

  ArrayAdapter<String> autocompleteAdapter;
  String[] mealNames;

  public ProposeFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_propose, container, false);

    MainActivity mainActivity = (MainActivity) getActivity();
    loginToken = NevaLoginManager.getInstance().getByteStringToken();
    mChannel = mainActivity.mChannel;
    blockingStub = mainActivity.blockingStub;

    fragment_proposal_field = view.findViewById(R.id.fragment_proposal_field);
    fragment_tag_proposal_field = view.findViewById(R.id.fragment_tag_proposal_field);
    meal_for_tag_field = view.findViewById(R.id.fragment_proposal_meal_for_tag);
    tag_of_meal_field = view.findViewById(R.id.fragment_proposal_tag_of_meal);

    fragment_proposal_button = view.findViewById(R.id.fragment_proposal_button);
    fragment_tag_proposal_button = view.findViewById(R.id.fragment_tag_proposal_button);
    propose_tag_for_meal = view.findViewById(R.id.fragment_proposal_tag_for_meal_button);

    mealNames = getMealNames(); //TODO: Store mealnames and not send request each time.

    autocompleteAdapter = new ArrayAdapter<>(getContext(), R.layout.textview_autocomplete_item,
        mealNames);
    meal_for_tag_field.setAdapter(autocompleteAdapter);

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
        try {
          BackendOuterClass.GenericReply genRep = blockingStub
              .suggestionItemProposition(suggestionRequest);
          Toast.makeText(getContext(), "Meal Suggested", Toast.LENGTH_SHORT).show();
          fragment_proposal_field.getText().clear();
          fragment_proposal_button.setEnabled(true);
        } catch (Exception e) {
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

        try {
          BackendOuterClass.GenericReply genRep = blockingStub.tagProposition(tagProp);
          Toast.makeText(getContext(), "Tag Suggested", Toast.LENGTH_SHORT).show();
          fragment_tag_proposal_field.getText().clear();
          fragment_tag_proposal_button.setEnabled(true);
        } catch (Exception e) {
          Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
          fragment_tag_proposal_button.setEnabled(true);
        }

      }
    });

    /*propose_tag_for_meal.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        DatabaseManager dbman = new DatabaseManager(getContext());
        try {
          dbman.open();
        } catch (SQLException e) {
          e.printStackTrace();
        }

        int mealID = dbman.getMealId(
            meal_for_tag_field.getText().toString()); // TODO:GET MEAL ID DIRECTLY FROM TEXT BOX?
        String tagName = tag_of_meal_field.getText().toString();

        BackendOuterClass.TagPropositionRequest tagPropositionReq;
        tagPropositionReq = BackendOuterClass.TagPropositionRequest.newBuilder()
            .setToken(loginToken)
            .setTag(tagName)
            .build();

        //TODO: CHANGE TAG PROPOSAL PART WHEN BACKEND SUPPORTS IT.

        try {

          BackendOuterClass.GenericReply genRep = blockingStub.tagProposition(tagPropositionReq);
          Toast.makeText(getContext(), "Tag Suggested", Toast.LENGTH_SHORT).show();
          tag_of_meal_field.getText().clear();
          meal_for_tag_field.getText().clear();
          propose_tag_for_meal.setEnabled(true);
        } catch (Exception e) {

          Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
          propose_tag_for_meal.setEnabled(true);
        }
      }
    });*/

  }

  public String[] getMealNames() {
    BackendOuterClass.GetSuggestionItemListRequest request;
    request = BackendOuterClass.GetSuggestionItemListRequest.newBuilder()
        .setToken(loginToken).setStartIndex(0)
        .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
        .build();

    BackendOuterClass.GetSuggestionItemListReply reply = blockingStub
        .getSuggestionItemList(request);
    String[] values;
    values = new String[reply.getItemsCount()];
    for (int i = 0; i < reply.getItemsCount(); i++) {
      values[i] = reply.getItems(i).getName();
    }

    return values;
  }


}
