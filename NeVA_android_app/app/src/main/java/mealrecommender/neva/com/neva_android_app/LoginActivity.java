package mealrecommender.neva.com.neva_android_app;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import backend.BackendOuterClass;
import backend.BackendGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class LoginActivity extends AppCompatActivity {
    public static final String MESSAGE_CLASS = "com.neva.mealrecommender.MESSAGE";

    CallbackManager callbackManager;
    EditText username_field;
    EditText password_field;
    Button login_button;
    LoginButton facebook_login_button;
    ProgressBar pb;
    TextView registerButton;

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
        registerButton = findViewById(R.id.register);
        facebook_login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getBaseContext(), "Facebook Login Success" + loginResult.getAccessToken().getUserId(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(),LoginResultActivity.class);
                //TODO: Get username from facebook.
                intent.putExtra(MESSAGE_CLASS, "Succesfully logged");
                startActivity(intent);
            }

            @Override
            public void onCancel() {
                Toast.makeText(getBaseContext(), "Facebook Login Cancelled.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getBaseContext(), "Facebook Login Error.", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onRegisterButton(View view){
        Intent intent = new Intent(getBaseContext(),RegisterActivity.class);
        startActivity(intent);
    }



    //Process login request on login button press
    public void onLoginButton(View view) {
        Log.d("LoginActivity", "Login pressed!");

        EditText username_field = findViewById(R.id.username);
        EditText password_field = findViewById(R.id.password);
        ProgressBar pb = findViewById(R.id.progress_bar);

        String username = username_field.getText().toString();
        String password = password_field.getText().toString();


        Button login_button = (Button) findViewById(R.id.login_button);
        if (!validate()) {
            Toast.makeText(getBaseContext(), "Check your credentials.", Toast.LENGTH_LONG).show();
            login_button.setEnabled(true);
            return;
        }
        login_button.setEnabled(false);

        pb.setVisibility(View.VISIBLE);

        // SEND REQUEST
        ManagedChannel mChannel = ManagedChannelBuilder.forAddress("www.0xdeffbeef.com",50051).build();
        BackendGrpc.BackendBlockingStub blockingStub = BackendGrpc.newBlockingStub(mChannel);
        BackendOuterClass.LoginRequest loginRequest = BackendOuterClass.LoginRequest.newBuilder().setEmail(username).setPassword(password).build();

        // GET ANSWER
        BackendOuterClass.LoginReply loginReply = blockingStub.login(loginRequest);
        String loginToken = loginReply.getToken();

        Intent intent = new Intent(this, LoginResultActivity.class);
        String login_res = "Successfully Logged in as: " + username + " with grpc token: " + loginToken;
        intent.putExtra(MESSAGE_CLASS, login_res);
        pb.setVisibility(View.GONE);
        startActivity(intent);
    }


    // Validate username and password data
    //TODO: ADD INPUT SANITATION
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

    // Check password with RegEx to see if it fits the qualifications
    private static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

}


