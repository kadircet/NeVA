package mealrecommender.neva.com.neva_android_app;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    EditText username_field;
    EditText password_field;
    Button login_button;
    LoginButton facebook_login_button;
    ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username_field = findViewById(R.id.username);
        password_field = findViewById(R.id.password);
        pb = findViewById(R.id.progress_bar);
        login_button = findViewById(R.id.login_button);
        facebook_login_button = findViewById(R.id.facebook_login_button);
        callbackManager = CallbackManager.Factory.create();

        facebook_login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getBaseContext(), "Facebook Login Success" + loginResult.getAccessToken().getUserId(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getBaseContext(), "Facebook Login Cancelled.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        login_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                login();
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void login() {
        Log.d("LoginActivity", "Login pressed!");

        EditText username_field = findViewById(R.id.username);
        EditText password_field = findViewById(R.id.password);
        ProgressBar pb = findViewById(R.id.progress_bar);

        String username = username_field.getText().toString();
        String password = password_field.getText().toString();


        Button login_button = (Button) findViewById(R.id.login_button);
        if (!validate()) {
            Toast.makeText(getBaseContext(), "Login Failed.", Toast.LENGTH_LONG).show();
            login_button.setEnabled(true);
            //finish();
        } else {
            login_button.setEnabled(false);

            pb.setVisibility(View.VISIBLE);

            // SEND REQUEST
            // GET ANSWER
            //pb.setVisibility(View.GONE);
        }
    }



    public boolean validate() {
        String username = username_field.getText().toString();
        String password = password_field.getText().toString();
        boolean val = true;
        if(username.isEmpty())
        {
            username_field.setError("Please enter a valid username");
            val=false;
        }
        else
        {
            username_field.setError(null);
        }
        if(!isValidPassword(password))
        {
            val=false;
        }
        else
        {
            password_field.setError(null);
        }
        return val;
    }

    private static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

}


