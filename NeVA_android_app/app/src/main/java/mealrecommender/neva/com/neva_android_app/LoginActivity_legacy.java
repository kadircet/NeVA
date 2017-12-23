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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.protobuf.ByteString;


import org.json.JSONObject;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import neva.backend.BackendOuterClass;
import neva.backend.BackendGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class LoginActivity_legacy extends AppCompatActivity {

    private static final String TAG = "LoginActivity_legacy";
    public static final String TOKEN_EXTRA = "com.neva.mealrecommender.TOKEN";
    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_AUTH_TYPE = "authType";
    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "isAddingNewAccount";

    CallbackManager callbackManager;
    EditText username_field;
    EditText password_field;
    Button login_button;
    LoginButton facebook_login_button;
    ProgressBar pb;
    TextView registerButton;
    BackendGrpc.BackendBlockingStub blockingStub;
    ManagedChannel mChannel;
    ByteString loginToken;

    NevaLoginManager nevaLoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.background);
        setContentView(R.layout.activity_login_legacy);
        username_field = findViewById(R.id.username);
        password_field = findViewById(R.id.password);
        pb = findViewById(R.id.progress_bar);
        login_button = findViewById(R.id.login_button);
        facebook_login_button = findViewById(R.id.facebook_login_button);
        callbackManager = CallbackManager.Factory.create();
        registerButton = findViewById(R.id.register);
        mChannel = ManagedChannelBuilder.forAddress("www.0xdeffbeef.com", 50051).usePlaintext(true).build();
        blockingStub = BackendGrpc.newBlockingStub(mChannel);
        facebook_login_button.setReadPermissions(Arrays.asList("public_profile","email"));
        nevaLoginManager = NevaLoginManager.getInstance();
        loginToken =null;

        if(nevaLoginManager.isLoggedIn())
        {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            Toast.makeText(getBaseContext(), "Already logged in!", Toast.LENGTH_LONG).show();
            startActivity(intent);
            finish();
        }

        facebook_login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(final LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();
                Log.i("Facebook Access Token", accessToken);
                GraphRequest graphReq =GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("Login Activity", response.toString());
                        if(response.getError() != null) {
                            //Handle Error
                        }
                        else {
                            String email = object.optString("email");
                            String name = object.optString("name");

                            Log.i("Email: ", email);
                            Log.i("Name: ", name);
                            try {
                                loginToken = nevaLoginManager.logIn(email,
                                            loginResult.getAccessToken().getToken(),
                                        BackendOuterClass.LoginRequest.AuthenticationType.FACEBOOK);
                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                Toast.makeText(getBaseContext(), "Successfully FB logged in!", Toast.LENGTH_LONG).show();
                                startActivity(intent);
                            }
                            catch (Exception e)
                            {
                                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                LoginManager.getInstance().logOut();
                            }



                        }

                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields","id,name,email");
                graphReq.setParameters(parameters);
                graphReq.executeAsync();
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
        Log.d("LoginActivity_legacy", "Login pressed!");

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

        //String loginToken = null;
        try {
            // SEND REQUEST
            login_button.setEnabled(false);

            pb.setVisibility(View.VISIBLE);

            loginToken = nevaLoginManager.logIn(username,password, BackendOuterClass.LoginRequest.AuthenticationType.DEFAULT);
            Log.i(TAG, loginToken.toString());
            if(loginToken != null) {
                Intent intent = new Intent(this, MainActivity.class);
                Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_LONG).show();
                login_button.setEnabled(true);
                pb.setVisibility(View.GONE);
                startActivity(intent);
                finish();
            }
            else
            {
                Toast.makeText(this, "FAILED LOGIN", Toast.LENGTH_SHORT).show();
            }
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


