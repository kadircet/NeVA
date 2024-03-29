package mealrecommender.neva.com.neva_android_app.activities;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import mealrecommender.neva.com.neva_android_app.util.NevaConnectionManager;
import mealrecommender.neva.com.neva_android_app.R;
import neva.backend.UserOuterClass;
import neva.backend.util.Util;

public class RegisterActivity extends AppCompatActivity {

  private final String TAG = this.getClass().getSimpleName();

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
  NevaConnectionManager connectionManager;

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
    connectionManager = NevaConnectionManager.getInstance();

  }

  public void onGenderButton(View view) {
    boolean checked = ((RadioButton) view).isChecked();
    if (checked) {
      gender_field = (RadioButton) view;
    }
  }

  public void onBirthdayButton(View view) {
    final Calendar currentTime = Calendar.getInstance();
    int year = currentTime.get(Calendar.YEAR);
    int month = currentTime.get(Calendar.MONTH);
    int day = currentTime.get(Calendar.DAY_OF_MONTH);

    mDateSetListener = new OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.clear(Calendar.YEAR);
        cal.clear(Calendar.MONTH);
        cal.clear(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
        String date =
            cal.get(Calendar.DAY_OF_MONTH) + " / " + monthName + " / " + cal.get(Calendar.YEAR);
        birthdate_field.setText(date);
        birthday_time = (int) (cal.getTimeInMillis() / 1000);
        Log.d("BDay: ", Integer.toString(birthday_time));
      }
    };

    DatePickerDialog dpDialog = new DatePickerDialog(RegisterActivity.this, mDateSetListener, year, month, day);
    dpDialog.show();

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
      signup_button.setEnabled(true);
      return;
    }
    //TODO: Move this code to NevaLoginManager

    signup_button.setEnabled(false);
    pb.setVisibility(View.VISIBLE);
    String email = email_field.getText().toString().trim();
    String username = username_field.getText().toString().trim();
    gender = getGenderOfButton(gender_field);
    Util.Timestamp bdate = Util.Timestamp.newBuilder().setSeconds(birthday_time).build();
    String password = password_field.getText().toString();

    boolean registerSuccess = connectionManager.registerRequest(username, email, password, gender, bdate);
    if(registerSuccess) {
      Toast.makeText(this, getResources().getString(R.string.success_signup), Toast.LENGTH_LONG)
          .show();
      signup_button.setEnabled(true);
      pb.setVisibility(View.GONE);
      //startActivity(intent);
      setResult(RESULT_OK);
      finish();
    } else {
      Toast.makeText(getBaseContext(), "Register Failed", Toast.LENGTH_LONG).show();
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
      username_field.setError(getResources().getString(R.string.error_invalid_name));
      return false;
    } else {
      username_field.setError(null);
      return true;
    }
  }

  public boolean validateEmail() {
    String email = email_field.getText().toString();
    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      email_field.setError(getResources().getString(R.string.error_invalid_email));
      return false;
    }
    return true;
  }

  public boolean validatePassword() {
    String password = password_field.getText().toString();
    if (password.isEmpty()) {
      Toast.makeText(this, getResources().getString(R.string.error_fix_credentials), Toast.LENGTH_LONG).show();
      return false;
    } else {
      password_field.setError(null);
    }
    return true;
  }

  public boolean validateGender() {
    if (gender_field == null) {
      Toast.makeText(this, getResources().getString(R.string.error_invalid_gender), Toast.LENGTH_LONG).show();
      return false;
    }
    return true;
  }

  public boolean validateBirthday() {
    long currentTime = Calendar.getInstance().getTimeInMillis() - 6000000;
    if (birthday_time != null && birthday_time < (currentTime/1000) ) {
      return true;
    }
    Toast.makeText(this, getResources().getString(R.string.error_invalid_birthday), Toast.LENGTH_LONG).show();
    return false;
  }

  public boolean validate() {
    return validateUsername() && validateEmail() && validatePassword()
        && validateGender() && validateBirthday();
  }
}
