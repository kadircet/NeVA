package mealrecommender.neva.com.neva_android_app;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import java.util.ArrayList;
import mealrecommender.neva.com.neva_android_app.customviews.NiceAutoCompleteTextView;
import mealrecommender.neva.com.neva_android_app.database.Meal;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import mealrecommender.neva.com.neva_android_app.util.IgnoreAccentsArrayAdapter;
import neva.backend.BackendGrpc;


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


  TabLayout tabLayout;
  LinearLayout mealNameLayout;
  LinearLayout tagNameLayout;
  LinearLayout tagForFoodLayout;
  TextView descriptionText;


  NiceAutoCompleteTextView meal_for_tag_field;
  NiceAutoCompleteTextView tag_of_meal_field;
  Button propose_tag_for_meal;
  NevaDatabase db;
  NevaConnectionManager connectionManager;

  IgnoreAccentsArrayAdapter<String> mealAutoCompleteAdapter;
  IgnoreAccentsArrayAdapter<String> tagAutoCompleteAdapter;

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

    tabLayout = view.findViewById(R.id.tabLayout);
    mealNameLayout = view.findViewById(R.id.mealNameLayout);
    tagNameLayout = view.findViewById(R.id.tagNameLayout);
    tagForFoodLayout = view.findViewById(R.id.tagForFoodLayout);
    descriptionText = view.findViewById(R.id.descriptionText);

    fragment_proposal_field = view.findViewById(R.id.fragment_proposal_field);
    fragment_tag_proposal_field = view.findViewById(R.id.fragment_tag_proposal_field);
    meal_for_tag_field = view.findViewById(R.id.fragment_proposal_meal_for_tag);
    tag_of_meal_field = view.findViewById(R.id.fragment_proposal_tag_of_meal);

    fragment_proposal_button = view.findViewById(R.id.fragment_proposal_button);
    fragment_tag_proposal_button = view.findViewById(R.id.fragment_tag_proposal_button);
    propose_tag_for_meal = view.findViewById(R.id.fragment_proposal_tag_for_meal_button);

    mealNames = getMealNames();
    tagNames = db.nevaDao().getTagNames();


    mealAutoCompleteAdapter = new IgnoreAccentsArrayAdapter(getContext(),  R.layout.textview_autocomplete_item, mealNames);
    meal_for_tag_field.setAdapter(mealAutoCompleteAdapter);

    tagAutoCompleteAdapter = new IgnoreAccentsArrayAdapter(getContext(),  R.layout.textview_autocomplete_item, mealNames);
    tag_of_meal_field.setAdapter(tagAutoCompleteAdapter);

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
      if(meal_for_tag_field.isSelectionFromPopUp()) {
        if(tag_of_meal_field.isSelectionFromPopUp()) {
          MealTagProposalTask mealTagProposalTask = new MealTagProposalTask();
          mealTagProposalTask.execute();
        } else {
          tag_of_meal_field.setError("Please select tag from pop-up list");
        }
      } else {
        meal_for_tag_field.setError("Please select meal from pop-up list");
      }
      }
    });

    mealNameLayout.setVisibility(View.VISIBLE);
    tagNameLayout.setVisibility(View.GONE);
    tagForFoodLayout.setVisibility(View.GONE);

    descriptionText.setText("Propose us new meals if you can't find it in our list");

    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override            public void onTabSelected(TabLayout.Tab tab) {

        Log.e("tab ","onTabSelected ===>"+tab.getPosition());

        if(tab.getPosition()==0) {
          mealNameLayout.setVisibility(View.VISIBLE);
          descriptionText.setText("Propose us new meals if you can't find it in our list");
        }
        else if(tab.getPosition()==1) {
          tagNameLayout.setVisibility(View.VISIBLE);
          descriptionText.setText("Propose us new tags if you can't find it in our list");
        }
        else if(tab.getPosition()==2) {
          tagForFoodLayout.setVisibility(View.VISIBLE);
          descriptionText.setText("Add a new tag for an already existing item");
        }
      }
      @Override            public void onTabUnselected(TabLayout.Tab tab) {
        Log.e("tab ","onTabUnselected ===>"+tab.getPosition());
        if(tab.getPosition()==0) {
          mealNameLayout.setVisibility(View.GONE);
        }
        else if(tab.getPosition()==1) {
          tagNameLayout.setVisibility(View.GONE);
        }
        else if(tab.getPosition()==2) {
          tagForFoodLayout.setVisibility(View.GONE);
        }
      }
      @Override            public void onTabReselected(TabLayout.Tab tab) {
        Log.e("tab ","onTabReselected ===>"+tab.getPosition());
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
