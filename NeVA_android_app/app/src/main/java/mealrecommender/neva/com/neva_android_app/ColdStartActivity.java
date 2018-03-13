package mealrecommender.neva.com.neva_android_app;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.flexbox.FlexboxLayout;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import mealrecommender.neva.com.neva_android_app.database.Meal;
import mealrecommender.neva.com.neva_android_app.database.MealTagRelation;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import mealrecommender.neva.com.neva_android_app.util.ColorGenerator;
import neva.backend.BackendOuterClass.GetSuggestionItemListReply;
import neva.backend.BackendOuterClass.GetSuggestionItemListRequest;
import neva.backend.BackendOuterClass.GetTagsRequest;
import neva.backend.SuggestionOuterClass;
import neva.backend.SuggestionOuterClass.Suggestion;
import neva.backend.SuggestionOuterClass.Suggestion.SuggestionCategory;
import neva.backend.SuggestionOuterClass.Tag;

public class ColdStartActivity extends AppCompatActivity {

  private final String TAG = this.getClass().getSimpleName();
  final int kQuestionNumber = 10;
  FlexboxLayout flexboxLayout;

  ByteString loginToken;
  NevaDatabase db;
  NevaConnectionManager connectionManager;
  SharedPreferences sharedPreferences;

  TextView recommendedView;
  Button likeButton;
  Button dislikeButton;

  ProgressBar progressBar;

  ConcurrentLinkedQueue<Meal> suggestionList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cold_start);
    Toolbar toolbar = findViewById(R.id.cold_start_toolbar);
    setSupportActionBar(toolbar);
    setTitle("Cold Start");
    db = Room.databaseBuilder(getBaseContext(), NevaDatabase.class, getResources().getString(R.string.database_name))
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build();
    connectionManager = NevaConnectionManager.getInstance();
    sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_pref_filename), MODE_PRIVATE);
    loginToken = NevaLoginManager.getInstance().getByteStringToken();

    flexboxLayout = findViewById(R.id.cold_start_flexbox_layout);
    suggestionList = new ConcurrentLinkedQueue<Meal>();
    likeButton = findViewById(R.id.cold_start_like_button);
    dislikeButton = findViewById(R.id.cold_start_dislike_button);
    recommendedView = findViewById(R.id.cold_start_recommendation_field);
    progressBar = findViewById(R.id.cold_start_progress_bar);

    FillDatabaseTask fillDatabaseTask = new FillDatabaseTask();
    fillDatabaseTask.execute();

    progressBar.setVisibility(View.VISIBLE);
    progressBar.setMax(kQuestionNumber);
    likeButton.setVisibility(View.GONE);
    dislikeButton.setVisibility(View.GONE);

    ArrayList<Meal> meals = (ArrayList<Meal>) db.nevaDao().getAllMeals();
    int mealCount = meals.size();
    Random rnd = new Random();

    for(int i=0; i<kQuestionNumber; i++) {
      suggestionList.offer(meals.get(rnd.nextInt(mealCount)));
    }
    likeButton.setVisibility(View.VISIBLE);
    dislikeButton.setVisibility(View.VISIBLE);
    displayNextMeal();
  }


  void displayNextMeal() {
    Log.e(TAG, ""+suggestionList.isEmpty()+" "+suggestionList.size());
    if(suggestionList.isEmpty()) {
      Intent intent = new Intent(getBaseContext(), MainActivity.class);
      startActivity(intent);
      finish();
    } else {
      flexboxLayout.removeAllViews();
      Meal meal = suggestionList.poll();
      int[] tagIds = db.nevaDao().getMealTags(meal.id);
      ArrayList<String> tagNames = (ArrayList<String>) db.nevaDao().getTagNames(tagIds);
      recommendedView.setText(meal.mealName);
      for (int i = 0; i < tagNames.size(); i++) {
        TextView tagHolder = new TextView(this);
        tagHolder.setText(tagNames.get(i));
        tagHolder.setTextSize(16);
        tagHolder.setTextColor(getResources().getColor(R.color.textPrimaryColor));
        tagHolder.setPadding(16, 0, 16, 0);
        GradientDrawable bg = (GradientDrawable) getResources()
            .getDrawable(R.drawable.rounded_tagview_background, this.getTheme());
        bg.setColor(ColorGenerator.getColor(tagNames.get(i)));
        tagHolder.setBackground(bg);

        flexboxLayout.addView(tagHolder);
      }
    }
  }

  public void likeButtonClick(View view) {
    SendFeedbackTask sendFeedbackTask = new SendFeedbackTask();
    sendFeedbackTask.execute(true);
    displayNextMeal();
  }

  public void dislikeButtonClick(View view) {
    SendFeedbackTask sendFeedbackTask = new SendFeedbackTask();
    sendFeedbackTask.execute(false);
    displayNextMeal();
  }


  class SendFeedbackTask extends AsyncTask<Boolean, Void, Boolean> {
    int suggesteeId;

    @Override
    protected void onPreExecute() {
      likeButton.setEnabled(false);
      dislikeButton.setEnabled(false);
    }

    @Override
    protected Boolean doInBackground(Boolean... booleans) {
      try {
        suggesteeId = db.nevaDao().getMealId(recommendedView.getText().toString());

        boolean like = booleans[0];

        return connectionManager
            .sendFeedback(like, 0, suggesteeId, 0, 0, 0);
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
        return false;
      }
    }

    @Override
    protected void onPostExecute(Boolean success) {
      if (!success) {
        Snackbar.make(findViewById(R.id.cold_start_content), "Problem sending Feedback", Snackbar.LENGTH_LONG).show();
      }
      likeButton.setEnabled(true);
      dislikeButton.setEnabled(true);
      progressBar.setProgress(progressBar.getProgress()+1);
    }
  }

  class FillDatabaseTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
      //Snackbar.make(findViewById(R.id.content_view), "Adding meals and tags to DB", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
      addMealsToDatabase();
      addTagsToDatabase();
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      Snackbar.make(findViewById(R.id.cold_start_content), "Added meals and tags to DB", Snackbar.LENGTH_SHORT).show();
    }

    public void addTagsToDatabase() {
      GetTagsRequest request;
      int tagTableVersion = sharedPreferences.getInt("tagTableVersion", 0);
      tagTableVersion = 0; //FOR TESTING ONLY
      Log.i(TAG, "Current Tag Version: "+ Integer.toString(tagTableVersion));
      try{
        List<Tag> tagList = connectionManager.getTags(tagTableVersion);
        List<mealrecommender.neva.com.neva_android_app.database.Tag> tags = new ArrayList<>();
        for(SuggestionOuterClass.Tag tag : tagList) {
          mealrecommender.neva.com.neva_android_app.database.Tag tagToAdd = new mealrecommender.neva.com.neva_android_app.database.Tag(tag.getId(), tag.getName());
          tags.add(tagToAdd);
        }
        int inserted = 0;
        int updated = 0;
        for(mealrecommender.neva.com.neva_android_app.database.Tag tag: tags){
          int count = db.nevaDao().tagExists(tag.id); // TODO: insert all tags with insertTags()
          if (count>0) {
            db.nevaDao().updateTag(tag);
            updated++;
          } else {
            db.nevaDao().addTag(tag);
            inserted++;
          }
        }
        Log.d(TAG, Integer.toString(inserted)+" Tags inserted to db");
        Log.d(TAG, Integer.toString(updated)+" Tags updated in db");
      } catch (Exception e) {
        Log.e(TAG, "CANT ADD TAGS");
        Log.e(TAG, e.getMessage());
      }
    }

    public void addMealsToDatabase() {
      GetSuggestionItemListRequest request;
      int mealTableVersion = sharedPreferences.getInt("mealTableVersion", 0);
      mealTableVersion = 0; // FOR TESTING ONLY
      Log.i(TAG, "Current DB ver: "+ Integer.toString(mealTableVersion));
      GetSuggestionItemListReply reply = connectionManager.getSuggestions(SuggestionCategory.MEAL, mealTableVersion);
      List<Suggestion> suggestions = reply.getItems().getSuggestionListList();
      Log.i(TAG, "Reply DB ver: "+ Integer.toString(reply.getLastUpdated()));
      sharedPreferences.edit().putInt("mealTableVersion", reply.getLastUpdated()).commit();

      int inserted = 0;
      int updated = 0;
      for(Suggestion sug : suggestions) {
        Meal meal = new Meal(sug.getSuggesteeId(), sug.getName(), "PhotoURL");
        List<SuggestionOuterClass.Tag> tagList = sug.getTagsList();
        List<mealrecommender.neva.com.neva_android_app.database.Tag> mealTags = new ArrayList<>();
        for (SuggestionOuterClass.Tag tag : tagList) {
          mealrecommender.neva.com.neva_android_app.database.Tag tagToAdd = new mealrecommender.neva.com.neva_android_app.database.Tag(tag.getId(), tag.getName());
          mealTags.add(tagToAdd);
        }

        int count = db.nevaDao().mealExits(meal.id);
        if (count > 0) {
          db.nevaDao().updateMeal(meal);
          updated++;
        } else {
          db.nevaDao().addMeal(meal);
          inserted++;
        }
        int tagInserted = 0;
        int tagUpdated = 0;
        for (mealrecommender.neva.com.neva_android_app.database.Tag tag : mealTags) {
          MealTagRelation relation = new MealTagRelation(meal.id, tag.id);
          count = db.nevaDao().mealTagRelationExists(relation.mealId, relation.tagId);
          if (count > 0) {
            db.nevaDao().updateMealTag(relation);
            tagUpdated++;
          } else {
            db.nevaDao().addMealTag(relation);
            tagInserted++;
          }
          Log.d(TAG,
              Integer.toString(tagInserted) + " tags inserted for " + meal.mealName + " to db");
          Log.d(TAG, Integer.toString(tagUpdated) + " tags updated for " + meal.mealName + " in db");
        }
      }
      Log.d(TAG, Integer.toString(inserted) + " Meals inserted to db");
      Log.d(TAG, Integer.toString(updated) + " Meals updated in db");
    }
  }

}
