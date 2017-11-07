package mealrecommender.neva.com.neva_android_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LoginResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_result);
        Intent intent = getIntent();
        String result = intent.getStringExtra(LoginActivity.MESSAGE_CLASS);

        TextView tv = findViewById(R.id.textView);
        tv.setText(result);
    }
}
