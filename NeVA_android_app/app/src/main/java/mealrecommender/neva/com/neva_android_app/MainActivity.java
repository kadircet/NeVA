package mealrecommender.neva.com.neva_android_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.facebook.login.LoginManager;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import mealrecommender.neva.com.neva_android_app.database.Meal;
import mealrecommender.neva.com.neva_android_app.database.MealTagRelation;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import mealrecommender.neva.com.neva_android_app.database.Tag;
import neva.backend.BackendOuterClass.GetSuggestionItemListReply;
import neva.backend.BackendOuterClass.GetSuggestionItemListRequest;
import neva.backend.BackendOuterClass.GetTagsRequest;
import neva.backend.SuggestionOuterClass;
import neva.backend.SuggestionOuterClass.Suggestion;
import neva.backend.SuggestionOuterClass.Suggestion.SuggestionCategory;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, OnBackStackChangedListener {

  private final String TAG = this.getClass().getSimpleName();

  public ByteString loginToken;
  public FloatingActionButton fab;
  public NevaDatabase db;
  public SharedPreferences sharedPreferences;
  public NevaConnectionManager connectionManager;
  TextView navdrawUsername;
  HistoryCursorAdapter adapter;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();



    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    navdrawUsername = navigationView.getHeaderView(0).findViewById(R.id.nav_header_username);
    (new GetUserNameTask()).execute();
    //navdrawUsername.setText(NevaLoginManager.getInstance().getEmail());

    loginToken = NevaLoginManager.getInstance().getByteStringToken();

    db = Room.databaseBuilder(getBaseContext(), NevaDatabase.class, getResources().getString(R.string.database_name))
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build();

    connectionManager = NevaConnectionManager.getInstance();
    sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_pref_filename), MODE_PRIVATE);

    Fragment fragment = new RecommendFragment();
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.addOnBackStackChangedListener(MainActivity.this);
    fragmentManager.beginTransaction().replace(R.id.content_view, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();

    FillDatabaseTask fillDatabaseTask = new FillDatabaseTask();
    fillDatabaseTask.execute();

    fab = findViewById(R.id.fab);

    MenuItem item = navigationView.getMenu().getItem(0);
    item.setChecked(true);
    setTitle(item.getTitle());

  }

  @Override
  public void onBackStackChanged() {

    FragmentManager fm = getSupportFragmentManager();
    int count = fm.getBackStackEntryCount();
    Log.e("BACKSTACK CHANGED", ""+count);
    for(int i =0; i<count; i++) {
      Log.d("BACKSTACK", ""+fm.getBackStackEntryAt(i).getName());
    }
  }

  class GetUserNameTask extends AsyncTask<Void, Void, String> {
    @Override
    protected String doInBackground(Void... voids) {
      try {
        return NevaConnectionManager.getInstance().getUser().getName();
      } catch (Exception e)
      {
        Log.e(TAG, e.getMessage());
        return null;
      }
    }

    protected void onPostExecute(String username) {
      if(username != null || username.length()>0) {
        navdrawUsername.setText(username);
      } else {
        navdrawUsername.setText("Mr.Nobody?");
      }
    }
  }

  class FillDatabaseTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
      Snackbar.make(findViewById(R.id.content_view), "Adding meals and tags to DB", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
      addMealsToDatabase();
      addTagsToDatabase();
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      Snackbar.make(findViewById(R.id.content_view), "Added meals and tags to DB", Snackbar.LENGTH_SHORT).show();
    }

    public void addTagsToDatabase() {
      GetTagsRequest request;
      int tagTableVersion = sharedPreferences.getInt("tagTableVersion", 0);
      tagTableVersion = 0; //FOR TESTING ONLY
      Log.i(TAG, "Current Tag Version: "+ Integer.toString(tagTableVersion));
      try{
        List<SuggestionOuterClass.Tag> tagList = connectionManager.getTags(tagTableVersion);
        List<Tag> tags = new ArrayList<>();
        for(SuggestionOuterClass.Tag tag : tagList) {
          Tag tagToAdd = new Tag(tag.getId(), tag.getName());
          tags.add(tagToAdd);
        }
        int inserted = 0;
        int updated = 0;
        for(Tag tag: tags){
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
        List<Tag> mealTags = new ArrayList<>();
        for (SuggestionOuterClass.Tag tag : tagList) {
          Tag tagToAdd = new Tag(tag.getId(), tag.getName());
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
        for (Tag tag : mealTags) {
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

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    FragmentManager fragmentManager = getSupportFragmentManager();
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else if (fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount()-1).getName().equals(RecommendFragment.class.getSimpleName())) {
      finish();
    } else if (fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount()-1).getName().equals(AddHistoryItemFragment.class.getSimpleName())) {
      fragmentManager.popBackStack();
    } else {
      fragmentManager.popBackStack(RecommendFragment.class.getSimpleName(), 0);
      setTitle("Recommendation");
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    FragmentManager fragmentManager = getSupportFragmentManager();
    int id = item.getItemId();
    Fragment fragment = null;
    Class fragmentClass = null;

    if (id == R.id.nav_recommend) {
      fragmentManager.popBackStack("RecommendFragment", 0);
    } else if (id == R.id.nav_propose) {
      fragmentClass = ProposeFragment.class;
    } else if (id == R.id.nav_history) {
      fragmentClass = HistoryFragment.class;
    } else if (id == R.id.nav_profile){
      fragmentClass = UserFragment.class;
    } else if (id == R.id.nav_logout) {
      AccountManager am = AccountManager.get(getBaseContext());
      Account acc[] = am.getAccountsByType(LoginActivity.ACCOUNT_TYPE);
      if (acc[0] != null) {
        am.removeAccountExplicitly(acc[0]);
        sharedPreferences.edit().putInt("databaseVersion", 0).commit();
      }
      LoginManager.getInstance().logOut();
      NevaLoginManager.getInstance().logOut();
      Intent loginActivity = new Intent(getBaseContext(), SplashScreen.class);
      startActivity(loginActivity);
      return true;
    }
    if(fragmentClass != null) {
      try {
        fragment = (Fragment) fragmentClass.newInstance();
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
      }
      fragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_in_up, R.anim.fast_fade_out, R.anim.fade_in_up, R.anim.fast_fade_out).replace(R.id.content_view, fragment)
          .addToBackStack(fragmentClass.getSimpleName()).commit();
    }

    // Highlight the selected item has been done by NavigationView
    item.setChecked(true);
    // Set action bar title
    setTitle(item.getTitle());

    // Close the navigation drawer
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  public void addHistoryFabClick(View view) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment fragment = new AddHistoryItemFragment();
    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_left_fast, R.anim.fast_fade_out, R.anim.slide_in_left_fast, R.anim.fast_fade_out).add(R.id.content_view, fragment)
        .addToBackStack(fragment.getClass().getSimpleName()).commit();
  }
}
