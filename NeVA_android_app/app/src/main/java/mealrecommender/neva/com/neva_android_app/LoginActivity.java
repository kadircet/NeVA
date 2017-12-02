package mealrecommender.neva.com.neva_android_app;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.protobuf.ByteString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import neva.backend.BackendOuterClass;
import neva.backend.BackendGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class LoginActivity extends AppCompatActivity {
    public static final String TOKEN_EXTRA = "com.neva.mealrecommender.TOKEN";

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
                Intent intent = new Intent(getBaseContext(),RecommendationActivity.class);
                //TODO: Get username from facebook.
                intent.putExtra(TOKEN_EXTRA, "Succesfully logged");
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


        Button login_button = findViewById(R.id.login_button);
        if (!validate()) {
            Toast.makeText(getBaseContext(), "Check your credentials.", Toast.LENGTH_LONG).show();
            login_button.setEnabled(true);
            return;
        }

        /*

        Intent intent = new Intent(this, RecommendationActivity.class);
        String login_res = "Successfully Logged in as: " + username + " with grpc token: ";
        intent.putExtra(TOKEN_EXTRA, login_res);
        login_button.setEnabled(true);
        pb.setVisibility(View.GONE);
        startActivity(intent);

        */

        //String loginToken = null;
        ByteString loginToken;
        try {
            // SEND REQUEST
            login_button.setEnabled(false);

            pb.setVisibility(View.VISIBLE);

            ManagedChannel mChannel = ManagedChannelBuilder.forAddress("www.0xdeffbeef.com", 50051).usePlaintext(true).build();
            BackendGrpc.BackendBlockingStub blockingStub = BackendGrpc.newBlockingStub(mChannel);
            BackendOuterClass.LoginRequest loginRequest = BackendOuterClass.LoginRequest.newBuilder()
                                                            .setEmail(username)
                                                            .setPassword(password)
                                                            .setAuthenticationType(BackendOuterClass.LoginRequest.AuthenticationType.DEFAULT)
                                                            .build();

            // GET ANSWER
            BackendOuterClass.LoginReply loginReply = blockingStub.login(loginRequest);
            loginToken = loginReply.getToken();

            Intent intent = new Intent(this, RecommendationActivity.class);
            //String login_res = "Successfully Logged in as: " + username + " with grpc token: " + loginToken.toString();
            Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_LONG).show();
            byte[] loginTokenArray = loginToken.toByteArray();
            intent.putExtra(TOKEN_EXTRA, loginTokenArray);
            login_button.setEnabled(true);
            pb.setVisibility(View.GONE);
            startActivity(intent);
            finish();

        }
        catch (Exception e)
        {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            pb.setVisibility(View.GONE);
            login_button.setEnabled(true);
        }


    }
    public boolean validateUsername() {
        String username = username_field.getText().toString();
        if(username.isEmpty())
        {
            username_field.setError("Enter a valid username");
            return false;
        }
        else
        {
            username_field.setError(null);
            return true;
        }
    }
    public boolean validatePassword() {
        String password = password_field.getText().toString();
        if(!isValidPassword(password))
        {
            return false;
        }
        else
        {
            password_field.setError(null);
        }
        return true;
    }

    // Validate username and password data
    //TODO: ADD INPUT SANITATION
    public boolean validate() {
        return validateUsername() && validatePassword();
    }

    // Check password with RegEx to see if it fits the qualifications
    private static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z]).{4,}$"; // <---SIMPLIFIED  "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

}


