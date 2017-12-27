package mealrecommender.neva.com.neva_android_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
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

import java.io.IOException;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import neva.backend.BackendGrpc;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  private static final String TAG = "MainActivity";

  public ByteString loginToken;
  public ManagedChannel mChannel;
  public BackendGrpc.BackendBlockingStub blockingStub;
  public FloatingActionButton fab;
  public NevaDatabase db;
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
    TextView navdrawUsername = navigationView.getHeaderView(0)
        .findViewById(R.id.nav_header_username);
    navdrawUsername.setText(NevaLoginManager.getInstance().getUsername());

    loginToken = NevaLoginManager.getInstance().getByteStringToken();
    mChannel = ManagedChannelBuilder.forAddress("neva.0xdeffbeef.com", 50053).build();
    blockingStub = BackendGrpc.newBlockingStub(mChannel);

    db = Room.databaseBuilder(getBaseContext(), NevaDatabase.class, "nevadb")
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build();

    fab = findViewById(R.id.fab);

    Fragment fragment = new RecommendFragment();
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content_view, fragment)
        .addToBackStack(fragment.getTag()).commit();

    MenuItem item = navigationView.getMenu().getItem(0);
    item.setChecked(true);
    setTitle(item.getTitle());

  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    FragmentManager fragmentManager = getSupportFragmentManager();
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    } else {
      super.onBackPressed();
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

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();
    Fragment fragment = null;
    Class fragmentClass = null;

    if (id == R.id.nav_recommend) {
      fragmentClass = RecommendFragment.class;
    } else if (id == R.id.nav_propose) {
      fragmentClass = ProposeFragment.class;
    } else if (id == R.id.nav_history) {
      fragmentClass = HistoryFragment.class;
    } else if (id == R.id.nav_logout) {
      AccountManager am = AccountManager.get(getBaseContext());
      Account acc[] = am.getAccountsByType(LoginActivity.ACCOUNT_TYPE);
      if (acc[0] != null) {
        am.removeAccount(acc[0], null, null);
      }
      NevaLoginManager.getInstance().logOut();
      Intent loginActivity = new Intent(getBaseContext(), LoginActivity.class);
      startActivity(loginActivity);
      return true;
    }
    try {
      fragment = (Fragment) fragmentClass.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }

    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content_view, fragment)
        .addToBackStack(fragment.getTag()).commit();

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
    fragmentManager.beginTransaction().add(R.id.content_view, fragment)
        .addToBackStack(fragment.getTag()).commit();
  }
}
