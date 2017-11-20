package mealrecommender.neva.com.neva_android_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import neva.backend.BackendOuterClass;

public class RegisterActivity extends AppCompatActivity {

    EditText username_field;
    EditText email_field;
    EditText password_field;
    Button signup_button;
    TextView already_member;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username_field = findViewById(R.id.username);
        email_field = findViewById(R.id.email);
        password_field = findViewById(R.id.password);
        signup_button = findViewById(R.id.signup_button);
        already_member = findViewById(R.id.already_member);
        pb = findViewById(R.id.progress_bar);
    }

    public void onSignupButton(View view){

        if(!validate())
        {
            Toast.makeText(getBaseContext(), "Fix your credentials.", Toast.LENGTH_LONG).show();
            return;
        }
        signup_button.setEnabled(false);
        pb.setVisibility(View.VISIBLE);
        String username = username_field.getText().toString();
        String password = password_field.getText().toString();
        String email = email_field.getText().toString();

        //TODO:SignUp logic
        Toast.makeText(getBaseContext(), "Signed Up!", Toast.LENGTH_LONG).show();

    }

    public void onMemberButton(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public boolean validate() {
        String username = username_field.getText().toString();
        String password = password_field.getText().toString();
        String email = email_field.getText().toString();
        boolean val = true;
        if(username.isEmpty())
        {
            username_field.setError("Enter a valid username");
            val=false;
        }
        else
        {
            username_field.setError(null);
        }
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_field.setError("Enter a valid email.");
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
