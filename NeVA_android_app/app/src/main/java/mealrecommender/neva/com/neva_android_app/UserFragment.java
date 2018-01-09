package mealrecommender.neva.com.neva_android_app;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
