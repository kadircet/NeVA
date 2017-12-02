package mealrecommender.neva.com.neva_android_app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.SuggestionOuterClass;

public class RecommendationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle burgerButton;
    TextView recommendedMeal;
    Button recommendButton;
    ByteString loginToken;
    NavigationView navigationView;
    ManagedChannel mChannel;
    BackendGrpc.BackendBlockingStub blockingStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        drawerLayout = findViewById(R.id.drawer_layout);
        burgerButton = new ActionBarDrawerToggle(this,drawerLayout, R.string.open, R.string.close);
        navigationView = findViewById(R.id.nav_menu);

        drawerLayout.addDrawerListener(burgerButton);
        navigationView.setNavigationItemSelectedListener(this);
        burgerButton.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        byte[] loginTokenArray = intent.getByteArrayExtra(LoginActivity.TOKEN_EXTRA);
        loginToken = ByteString.copyFrom(loginTokenArray);

        recommendedMeal = findViewById(R.id.recommendation_field);
        recommendButton = findViewById(R.id.recommend_button);
        //TODO: Request Recommendation
        recommendedMeal.setText("Recommended Meal Name");

        mChannel = ManagedChannelBuilder.forAddress("www.0xdeffbeef.com", 50051).usePlaintext(true).build();
        blockingStub = BackendGrpc.newBlockingStub(mChannel);
    }

    public void onGetRecommendationClick(View view) {
        recommendButton.setEnabled(false);
        BackendOuterClass.GetSuggestionRequest recommendationReq;
        recommendationReq = BackendOuterClass.GetSuggestionRequest.newBuilder()
                            .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
                            .setToken(loginToken).build();

        BackendOuterClass.GetSuggestionReply recommendationRep;
        try {
            recommendationRep = blockingStub.getSuggestion(recommendationReq);
            recommendedMeal.setText(recommendationRep.getName());

        }
        catch (Exception e)
        {
            Toast.makeText(this,  e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        recommendButton.setEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(burgerButton.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId())
        {
            case R.id.nav_profile:
                return true;
            case R.id.nav_suggest:
                Intent intent = new Intent(this, SuggestionActivity.class);
                intent.putExtra(LoginActivity.TOKEN_EXTRA, loginToken.toByteArray());
                startActivity(intent);
                return true;
            case R.id.nav_settings:

                return true;
            case R.id.nav_logout:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()){
            case R.id.nav_profile:
                intent = new Intent(this, ProfileActivity.class);
                intent.putExtra(LoginActivity.TOKEN_EXTRA, loginToken.toByteArray());
                startActivity(intent);
                break;
            case R.id.nav_recommendation:
                break;
            case R.id.nav_suggest:
                intent = new Intent(this, SuggestionActivity.class);
                intent.putExtra(LoginActivity.TOKEN_EXTRA, loginToken.toByteArray());
                startActivity(intent);
                break;
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(LoginActivity.TOKEN_EXTRA, loginToken.toByteArray());
                startActivity(intent);
                break;
            case R.id.nav_logout:
                logout();
                break;
            default:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START, true);
        return true;
    }

    public void logout() {

    }
}
