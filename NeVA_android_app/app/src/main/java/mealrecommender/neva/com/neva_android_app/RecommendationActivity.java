package mealrecommender.neva.com.neva_android_app;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class RecommendationActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle burgerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        drawerLayout = findViewById(R.id.drawer_layout);
        burgerButton = new ActionBarDrawerToggle(this,drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(burgerButton);
        burgerButton.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String result = intent.getStringExtra(LoginActivity.MESSAGE_CLASS);

        TextView tv = findViewById(R.id.textView);
        tv.setText(result);
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

            case R.id.nav_settings:

                return true;
            case R.id.nav_logout:

                return true;


            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
