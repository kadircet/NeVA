package mealrecommender.neva.com.neva_android_app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import neva.backend.UserOuterClass.User;
import neva.backend.UserOuterClass.User.Gender;
import neva.backend.util.Util.Timestamp;


public class UserFragment extends Fragment {

  private final String TAG = getClass().getSimpleName();
  ImageView profile_pic;
  TextView profile_username;
  TextView profile_email;
  TextView profile_bday;
  TextView profile_gender;
  TextView profile_weight;
  Integer newGenderType;

  AlertDialog changeTextDialog;
  DatePickerDialog.OnDateSetListener dateSetListener;

  public UserFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_user, container, false);
    MainActivity mainActivity = (MainActivity) getActivity();
    profile_pic = view.findViewById(R.id.profile_pic);
    profile_username = view.findViewById(R.id.profile_username);
    profile_email = view.findViewById(R.id.profile_email);
    profile_bday = view.findViewById(R.id.profile_bday);
    profile_gender = view.findViewById(R.id.profile_gender);
    profile_weight = view.findViewById(R.id.profile_weight);
    GetUserData getUserData = new GetUserData();
    getUserData.execute();
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    profile_username.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showDialog(view);
      }
    });
    profile_bday.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        datePickerDialog();
      }
    });
    profile_weight.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showDialog(view);
      }
    });
    profile_gender.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showGenderDialog(view);
      }
    });
  }

  public void datePickerDialog() {
    final Calendar currentTime = Calendar.getInstance();
    int year = currentTime.get(Calendar.YEAR);
    int month = currentTime.get(Calendar.MONTH);
    int day = currentTime.get(Calendar.DAY_OF_MONTH);

    dateSetListener = new OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.clear(Calendar.YEAR);
        cal.clear(Calendar.MONTH);
        cal.clear(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        long seconds = cal.getTimeInMillis() / 1000;
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(seconds).setNanos(0).build();
        User updatedUser = User.newBuilder().setDateOfBirth(timestamp).build();
        UpdateUserDataTask updateTask = new UpdateUserDataTask();
        updateTask.execute(updatedUser);
      }
    };

    DatePickerDialog dpDialog = new DatePickerDialog(getContext(), dateSetListener, year, month, day);
    dpDialog.show();
  }

  public void showGenderDialog(final View view) {
    String[] genders = new String[]{"Male", "Female"};
    AlertDialog builder = new AlertDialog.Builder(getContext()).setTitle("Gender")
        .setSingleChoiceItems(genders, -1, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        newGenderType = i;
      }
    }).setPositiveButton("Save", new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        User updatedUser;
        if(newGenderType == 0){
          updatedUser = User.newBuilder().setGender(Gender.MALE).build();
        } else {
          updatedUser = User.newBuilder().setGender(Gender.FEMALE).build();
        }
        UpdateUserDataTask updateTask = new UpdateUserDataTask();
        updateTask.execute(updatedUser);
      }
    }).setNegativeButton("Discard", new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
      }
    }).create();
    builder.show();
  }

  public void showDialog(final View view) {
    TextView thisView = (TextView) view;
    changeTextDialog = new AlertDialog.Builder(getContext()).create();
    final EditText newTextField = new EditText(getContext());

    newTextField.setTextSize(18);
    TextInputLayout layout = new TextInputLayout(getContext());
    layout.setPadding(48,48,48,48);
    layout.addView(newTextField);
    newTextField.setText(thisView.getText());
    changeTextDialog.setView(layout);
    switch (view.getId()) {
      case R.id.profile_username:
        newTextField.setHint("Name");
        changeTextDialog.setTitle("Name");
        break;
      case R.id.profile_email:
        newTextField.setHint("Email");
        changeTextDialog.setTitle("Email");
      case R.id.profile_weight:
        newTextField.setHint("Weight");
        changeTextDialog.setTitle("Weight");
        break;
    }
    changeTextDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Save", new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        //Update task if success modify ui, else dismiss
        User updatedUser;
        switch (view.getId()) {
          case R.id.profile_username:
            updatedUser = User.newBuilder().setName(newTextField.getText().toString()).build();
            break;
          case R.id.profile_email:
            updatedUser = User.newBuilder().setEmail(newTextField.getText().toString()).build();
          case R.id.profile_weight:
            String weightText = newTextField.getText().toString();
            weightText = weightText.substring(0, weightText.indexOf(" kg"));
            updatedUser = User.newBuilder().setWeight(Float.valueOf(weightText)).build();
            break;
          default:
            updatedUser = User.newBuilder().build();
            break;
        }
        UpdateUserDataTask updateTask = new UpdateUserDataTask();
        updateTask.execute(updatedUser);
      }
    });
    changeTextDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Discard", new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        changeTextDialog.dismiss();
      }
    });
    changeTextDialog.show();
  }

  class UpdateUserDataTask extends AsyncTask<User, Void, Boolean> {

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
      if(aBoolean) {
        GetUserData task = new GetUserData();
        task.execute();
      } else {
        Toast.makeText(getContext(), "Failed to update user data", Toast.LENGTH_SHORT).show();
      }
    }

    @Override
    protected Boolean doInBackground(User... users) {
      try{
        NevaConnectionManager.getInstance().updateUser(users[0]);
        return true;
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
        return false;
      }
    }
  }

  //TODO: Store user data in shared preferences and get it only once

  class GetUserData extends AsyncTask<Void, Void, Boolean> {

    String username;
    String email;
    String gender;
    String bday;
    String weight;

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Boolean doInBackground(Void... voids) {
      try {
      User user = NevaConnectionManager.getInstance().getUser();
      username = user.getName();
      //email = user.getEmail();
      email = NevaLoginManager.getInstance().getEmail();
      gender = user.getGender().toString();
      Log.d(TAG, gender);
      long epochTime = user.getDateOfBirth().getSeconds()*1000;
      Date bdate = new Date(epochTime);
      SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM YYYY");
      bday = sdf.format(bdate);
      weight = Float.toString(user.getWeight()) + " kg";
      return true;
      } catch (Exception e) {
        Log.e(TAG,e.getMessage());
        return false;
      }
    }

    @Override
    protected void onPostExecute(Boolean getUserSuccess) {
      if(getUserSuccess) {
        profile_username.setText(username);
        profile_email.setText(email);
        profile_bday.setText(bday);
        profile_gender.setText(gender);
        profile_weight.setText(weight);
      }
    }
  }

}
