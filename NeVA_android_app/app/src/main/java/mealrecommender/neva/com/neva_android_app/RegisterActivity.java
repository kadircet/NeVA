package mealrecommender.neva.com.neva_android_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.UserOuterClass;
import neva.backend.util.Util;

public class RegisterActivity extends AppCompatActivity {

  private static final String TAG = "RegisterActivity";

  public static final String MESSAGE_CLASS = "com.neva.mealrecommender.MESSAGE";

  EditText username_field;
  EditText email_field;
  EditText password_field;
  Button signup_button;
  TextView already_member;
  TextView birthdate_field;
  RadioButton gender_field;
  ProgressBar pb;
  Integer birthday_time;
  UserOuterClass.User.Gender gender;

  DatePickerDialog.OnDateSetListener mDateSetListener;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);
    username_field = findViewById(R.id.username);
    email_field = findViewById(R.id.email);
    password_field = findViewById(R.id.password);
    signup_button = findViewById(R.id.signup_button);
    already_member = findViewById(R.id.already_member);
    birthdate_field = findViewById(R.id.birthdate);
    pb = findViewById(R.id.progress_bar);

    mDateSetListener = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
        String date =
            cal.get(Calendar.DAY_OF_MONTH) + " / " + monthName + " / " + cal.get(Calendar.YEAR);
        birthdate_field.setText(date);
        birthday_time = (int) (cal.getTimeInMillis() / 1000);
        Log.d("BDay: ", Integer.toString(birthday_time));

      }
    };
  }

  public void onGenderButton(View view) {
    boolean checked = ((RadioButton) view).isChecked();
    if (checked) {
      gender_field = (RadioButton) view;
    }
  }

  public void onBirthdayButton(View view) {
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH);
    int day = cal.get(Calendar.DAY_OF_MONTH);

    DatePickerDialog dialog = new DatePickerDialog(
        RegisterActivity.this,
        android.R.style.Theme_DeviceDefault_Dialog_MinWidth,
        mDateSetListener,
        year, month, day);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    dialog.show();
  }

  public UserOuterClass.User.Gender getGenderOfButton(RadioButton button) {
    switch (button.getId()) {
      case R.id.gender_female:
        return UserOuterClass.User.Gender.FEMALE;
      case R.id.gender_male:
        return UserOuterClass.User.Gender.MALE;
      default:
        return UserOuterClass.User.Gender.UNRECOGNIZED;
    }
  }

  public void onSignupButton(View view) {

    if (!validate()) {
      Toast.makeText(getBaseContext(), "Fix your credentials.", Toast.LENGTH_LONG).show();
      return;
    }
    //TODO: Move this code to NevaLoginManager
    try {
      signup_button.setEnabled(false);
      pb.setVisibility(View.VISIBLE);
      String email = email_field.getText().toString();
      String username = username_field.getText().toString();
      gender = getGenderOfButton(gender_field);
      Util.Timestamp bdate = Util.Timestamp.newBuilder().setSeconds(birthday_time).build();
      String password = password_field.getText().toString();

      UserOuterClass.User user = UserOuterClass.User.newBuilder().setName(username).setEmail(email)
          .setGender(gender).setDateOfBirth(bdate).setPassword(password).build();

      BackendOuterClass.RegisterRequest registerRequest = BackendOuterClass.RegisterRequest
          .newBuilder().setUser(user).build();
      NevaLoginManager nevaLoginManager = NevaLoginManager.getInstance();
      BackendGrpc.BackendBlockingStub blockingStub = nevaLoginManager.blockingStub;

      BackendOuterClass.GenericReply registerReply = blockingStub.register(registerRequest);
      //Intent intent = new Intent(this, LoginActivity.class);
      Toast.makeText(this, "Succesfully Signed Up!", Toast.LENGTH_LONG).show();
      signup_button.setEnabled(true);
      pb.setVisibility(View.GONE);
      //startActivity(intent);
      setResult(RESULT_OK);
      finish();
    } catch (Exception e) {
      Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
      pb.setVisibility(View.GONE);
      signup_button.setEnabled(true);
    }
  }

  public void onMemberButton(View view) {
    setResult(RESULT_OK);
    finish();
  }

  public boolean validateUsername() {
    String username = username_field.getText().toString();
    if (username.isEmpty()) {
      username_field.setError("Enter a valid username");
      return false;
    } else {
      username_field.setError(null);
      return true;
    }
  }

  public boolean validateEmail() {
    String email = email_field.getText().toString();
    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      email_field.setError("Enter a valid email.");
      return false;
    }
    return true;
  }

  public boolean validatePassword() {
    String password = password_field.getText().toString();
    if (!isValidPassword(password)) {
      return false;
    } else {
      password_field.setError(null);
    }
    return true;
  }

  public boolean validateGender() {
    if (gender_field == null) {
      Toast.makeText(this, "Please check radio buttons", Toast.LENGTH_LONG).show();
      return false;
    }
    return true;
  }

  public boolean validateBithday() {
    long currentTime = Calendar.getInstance().getTimeInMillis() - 6000000;
    if (birthday_time > currentTime) {
      Toast.makeText(this, "Please enter a valid birthday", Toast.LENGTH_LONG).show();
      return false;
    }
    return true;
  }

  public boolean validate() {
    return validateUsername() && validateEmail() && validatePassword()
        && validateGender() && validateBithday();
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
