package mealrecommender.neva.com.neva_android_app;


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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import java.util.ArrayList;
import mealrecommender.neva.com.neva_android_app.database.Meal;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.BackendOuterClass.GenericReply;
import neva.backend.BackendOuterClass.TagPropositionRequest;
import neva.backend.BackendOuterClass.TagValuePropositionRequest;
import neva.backend.SuggestionOuterClass;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProposeFragment extends Fragment {

  private final String TAG = this.getClass().getSimpleName();

  ByteString loginToken;
  ManagedChannel mChannel;
  BackendGrpc.BackendBlockingStub blockingStub;

  EditText fragment_proposal_field;
  EditText fragment_tag_proposal_field;
  Button fragment_proposal_button;
  Button fragment_tag_proposal_button;

  AutoCompleteTextView meal_for_tag_field;
  AutoCompleteTextView tag_of_meal_field;
  Button propose_tag_for_meal;
  NevaDatabase db;
  NevaConnectionManager connectionManager;

  ArrayAdapter<String> mealAutocompleteAdapter;
  ArrayAdapter<String> tagAutocompleteAdapter;
  String[] mealNames;
  String[] tagNames;

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
    db = mainActivity.db;
    connectionManager = NevaConnectionManager.getInstance();

    fragment_proposal_field = view.findViewById(R.id.fragment_proposal_field);
    fragment_tag_proposal_field = view.findViewById(R.id.fragment_tag_proposal_field);
    meal_for_tag_field = view.findViewById(R.id.fragment_proposal_meal_for_tag);
    tag_of_meal_field = view.findViewById(R.id.fragment_proposal_tag_of_meal);

    fragment_proposal_button = view.findViewById(R.id.fragment_proposal_button);
    fragment_tag_proposal_button = view.findViewById(R.id.fragment_tag_proposal_button);
    propose_tag_for_meal = view.findViewById(R.id.fragment_proposal_tag_for_meal_button);

    mealNames = getMealNames();
    tagNames = db.nevaDao().getTagNames();

    mealAutocompleteAdapter = new ArrayAdapter<>(getContext(), R.layout.textview_autocomplete_item,
        mealNames);
    meal_for_tag_field.setAdapter(mealAutocompleteAdapter);

    tagAutocompleteAdapter = new ArrayAdapter<>(getContext(), R.layout.textview_autocomplete_item, tagNames);
    tag_of_meal_field.setAdapter(tagAutocompleteAdapter);

    return view;
  }

  class MealProposalTask extends AsyncTask<Void, Void, Boolean> {

    @Override
    protected void onPreExecute() {
      fragment_proposal_button.setEnabled(false);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
      return mealProposal();
    }

    @Override
    protected void onPostExecute(Boolean proposeSuccess) {
      if(proposeSuccess) {
        fragment_proposal_field.getText().clear();
        Toast.makeText(getContext(), getResources().getString(R.string.success_meal_propose), Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(getContext(), "Error while proposing", Toast.LENGTH_LONG).show();
      }
      fragment_proposal_button.setEnabled(true);
    }

    public boolean mealProposal() {

      String suggestionText = fragment_proposal_field.getText().toString();
      return connectionManager.suggestionItemProposition(suggestionText);
    }
  }

  class TagProposalTask extends AsyncTask<Void, Void, Boolean> {
    @Override
    protected void onPreExecute() {
      fragment_tag_proposal_button.setEnabled(false);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
      return tagPropose();
    }

    public boolean tagPropose() {
      String tagString = fragment_tag_proposal_field.getText().toString();
      return connectionManager.tagProposition(tagString);
    }

    @Override
    protected void onPostExecute(Boolean proposeSuccess) {
      if (proposeSuccess) {
        Toast.makeText(getContext(), getResources().getString(R.string.success_tag_propose), Toast.LENGTH_SHORT).show();
        fragment_tag_proposal_field.getText().clear();
      } else {
        Toast.makeText(getContext(), "Error while proposing tag", Toast.LENGTH_LONG).show();
      }
      fragment_tag_proposal_button.setEnabled(true);
    }
  }

  class MealTagProposalTask extends AsyncTask<Void, Void, Boolean> {
    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Boolean doInBackground(Void... voids) {
      return tagMealPropose();
    }

    @Override
    protected void onPostExecute(Boolean proposeSuccess) {
      if(proposeSuccess) {
        Toast.makeText(getContext(), getResources().getString(R.string.success_tag_propose), Toast.LENGTH_SHORT).show();
        tag_of_meal_field.getText().clear();
        meal_for_tag_field.getText().clear();
      } else {
        Toast.makeText(getContext(), "Error while proposing tag for meal", Toast.LENGTH_LONG).show();
      }
      propose_tag_for_meal.setEnabled(true);
    }

    public boolean tagMealPropose() {

      int sugId = db.nevaDao().getMealId(meal_for_tag_field.getText().toString());  // TODO:GET MEAL ID DIRECTLY FROM TEXT BOX?
      int tagId = db.nevaDao().getTagId(tag_of_meal_field.getText().toString());

      return connectionManager.tagValueProposition(sugId, tagId);
    }
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    fragment_proposal_button.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        MealProposalTask mealProposalTask = new MealProposalTask();
        mealProposalTask.execute();
      }
    });

    fragment_tag_proposal_button.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        TagProposalTask tagProposalTask = new TagProposalTask();
        tagProposalTask.execute();
      }
    });

    propose_tag_for_meal.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        MealTagProposalTask mealTagProposalTask = new MealTagProposalTask();
        mealTagProposalTask.execute();
      }
    });

  }

  public String[] getMealNames() {
    ArrayList<Meal> meals = (ArrayList<Meal>) db.nevaDao().getAllMeals();
    String[] values;
    values = new String[meals.size()];
    for (int i = 0; i < meals.size(); i++) {
      values[i] = meals.get(i).mealName;
    }
    Log.d(TAG, Integer.toString(values.length));
    return values;
  }
}
