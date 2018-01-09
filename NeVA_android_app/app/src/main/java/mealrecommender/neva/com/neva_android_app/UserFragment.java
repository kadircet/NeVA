package mealrecommender.neva.com.neva_android_app;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import neva.backend.UserOuterClass.User;


public class UserFragment extends Fragment {

  private final String TAG = getClass().getSimpleName();
  ImageView profile_pic;
  TextView profile_username;
  TextView profile_email;
  TextView profile_bday;
  TextView profile_gender;
  TextView profile_weight;

  AlertDialog changeTextDialog;

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
        showDialog(view);
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
        showDialog(view);
      }
    });
  }

  public void showDialog(View view) {
    TextView thisView = (TextView) view;
    changeTextDialog = new AlertDialog.Builder(getContext()).create();
    EditText newTextField = new EditText(getContext());

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
      case R.id.profile_gender:
        newTextField.setHint("Gender");
        changeTextDialog.setTitle("Gender");
        break;
      case R.id.profile_bday:
        newTextField.setHint("Birthday");
        changeTextDialog.setTitle("Birthday");
        break;
      case R.id.profile_weight:
        newTextField.setHint("Weight");
        changeTextDialog.setTitle("Weight");
        break;
    }
    changeTextDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Save", new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        //Update task if success modify ui, else dismiss
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

  public void updateBirthday(View view) {

  }

  public void updateGender(View view) {

  }

  public void updateWeight(View view) {

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
