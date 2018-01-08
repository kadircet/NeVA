package mealrecommender.neva.com.neva_android_app;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import mealrecommender.neva.com.neva_android_app.database.HistoryEntry;
import mealrecommender.neva.com.neva_android_app.database.Meal;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import neva.backend.BackendGrpc.BackendBlockingStub;
import neva.backend.BackendOuterClass.InformUserChoiceReply;
import neva.backend.BackendOuterClass.InformUserChoiceRequest;
import neva.backend.UserHistoryOuterClass.Choice;
import neva.backend.util.Util.Timestamp;

;


public class AddHistoryItemFragment extends Fragment {

  private final String TAG = this.getClass().getSimpleName();

  ByteString loginToken;
  FragmentManager fm;
  HistoryCursorAdapter cursorAdapter;

  String[] mealNames;
  ArrayAdapter<String> adapter;

  AutoCompleteTextView mealNameField;
  EditText timeField;
  Button addHistoryButton;

  Calendar date;
  LocationManager locationManager;

  TimePickerDialog.OnTimeSetListener timeSetListener;
  DatePickerDialog.OnDateSetListener dateSetListener;

  NevaDatabase db;
  NevaConnectionManager connectionManager;

  public AddHistoryItemFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_add_history, container, false);

    MainActivity mainActivity = (MainActivity) getActivity();
    loginToken = mainActivity.loginToken;
    cursorAdapter = mainActivity.adapter;
    db = mainActivity.db;
    connectionManager = NevaConnectionManager.getInstance();
    fm = mainActivity.getFragmentManager();
    mealNameField = view.findViewById(R.id.eaten_meal_field);
    timeField = view.findViewById(R.id.time_field);
    addHistoryButton = view.findViewById(R.id.sendMealHistory);
    mealNames = getSuggestionNames();
    adapter = new ArrayAdapter<>(getContext(), R.layout.textview_autocomplete_item, mealNames);
    mealNameField.setAdapter(adapter);
    locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ActivityCompat.requestPermissions(getActivity(),
        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                     Manifest.permission.ACCESS_COARSE_LOCATION},
                     1);

    timeField.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "TimeSelector");
        datePickerDialog();
      }
    });

    addHistoryButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AddHistoryTask addHistoryTask = new AddHistoryTask();
        addHistoryTask.execute();
      }
    });
  }

  class AddHistoryTask extends AsyncTask<Void, Void, Boolean> {

    boolean locationEnabled;
    Cursor newCursor;

    @Override
    protected void onPreExecute() {
      addHistoryButton.setEnabled(false);
      locationEnabled = checkLocationPermissions();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
      int mealID = db.nevaDao().getMealId(mealNameField.getText().toString()); // TODO:GET MEAL ID DIRECTLY FROM TEXT BOX?
      long timezoneOffset = date.getTimeZone().getRawOffset();
      long dateEpoch = (date.getTimeInMillis() + timezoneOffset) / 1000;
      double latitude = 0;
      double longitude = 0;
      if(locationEnabled) {
        Location currentLoc = getLocation();
        if (currentLoc != null) {
          latitude = currentLoc.getLatitude();
          longitude = currentLoc.getLongitude();
        }
      }
      try {
        InformUserChoiceReply informUserChoiceReply = connectionManager.informUserChoice(mealID, dateEpoch, latitude, longitude);
        Log.d(TAG, "Adding \"Choice\" to database (choiceId = "+informUserChoiceReply.getChoiceId()+" )");
        db.nevaDao().addHistoryEntry(new HistoryEntry(
            informUserChoiceReply.getChoiceId(),
            NevaLoginManager.getInstance().getEmail(),
            mealID,
            date.getTimeInMillis()));

        Log.d(TAG, "Getting History from database again, and changing view cursors");
        newCursor = db.nevaDao().getUserHistoryMeals(NevaLoginManager.getInstance().getEmail());
        return true;
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
        return false;
      }
    }

    @Override
    protected void onPostExecute(Boolean addMealSuccess) {
      if(addMealSuccess) {
        cursorAdapter.swapCursor(newCursor);
        Toast.makeText(getContext(), getResources().getString(R.string.success_add_history_entry), Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
      } else {
        Toast.makeText(getContext(), "Problem while adding meal", Toast.LENGTH_LONG).show();
      }
      addHistoryButton.setEnabled(true);
    }
  }

  private void datePickerDialog() {
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
        date = cal;
        timePickerDialog();
      }
    };

    DatePickerDialog dpDialog = new DatePickerDialog(getContext(), dateSetListener, year, month, day);
    dpDialog.show();
  }

  private void timePickerDialog() {
    final Calendar cal = date;
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int mins = cal.get(Calendar.MINUTE);

    timeSetListener = new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        Calendar cal = date;
        cal.clear(Calendar.HOUR_OF_DAY);
        cal.clear(Calendar.MINUTE);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        date = cal;
        Log.d(TAG, "Timestamp(milliseconds): "+Long.toString(date.getTimeInMillis()));
        Date mDate = new Date(date.getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, EEE, MMMM d");
        String dateText = sdf.format(mDate);
        timeField.setText(dateText);
      }
    };

    TimePickerDialog tpDialog = new TimePickerDialog(getContext()
        ,timeSetListener
        ,hour
        ,mins
        ,true);
    tpDialog.show();
  }
  public boolean checkLocationPermissions() {

    // Check if the gps is enabled
    if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
      Log.e(TAG, "Provider not enabled");
      //Check permissions for location access.
      if (ActivityCompat.checkSelfPermission(getActivity()
          , Manifest.permission.ACCESS_FINE_LOCATION)
          != PackageManager.PERMISSION_GRANTED
          && ActivityCompat.checkSelfPermission(getActivity()
          , Manifest.permission.ACCESS_COARSE_LOCATION)
          != PackageManager.PERMISSION_GRANTED) {
        Log.d(TAG, "Location permission Denied");
        Log.d(TAG, "Requesting location permission");
        ActivityCompat.requestPermissions(getActivity(),
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION},
            1);
        if (ActivityCompat.checkSelfPermission(getActivity()
            , Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(getActivity()
            , Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
          //We have location access
          return true;
        } else {
          //User denied location access permission
          return false;
        }
      }
      else {
        // GPS is enabled and we have location acceess
        return true;
      }
    }
    // GPS is disabled
    return false;
  }

  public Location getLocation() {
      Location location = null;
      // Check permissions again in case something goes wrong
      if (ActivityCompat.checkSelfPermission(getActivity()
          , Manifest.permission.ACCESS_FINE_LOCATION)
          == PackageManager.PERMISSION_GRANTED
          && ActivityCompat.checkSelfPermission(getActivity()
          , Manifest.permission.ACCESS_COARSE_LOCATION)
          == PackageManager.PERMISSION_GRANTED) {
        Log.d(TAG, "Getting location");
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        return location;
      }
      else {
        return null;
      }
  }

  String[] getSuggestionNames() {
    ArrayList<Meal> meals = (ArrayList<Meal>) db.nevaDao().getAllMeals();
    String[] values;
    values = new String[meals.size()];
    Log.i(TAG, "MealsSize: "+ Integer.toString(meals.size()));
    for (int i = 0; i < meals.size(); i++) {
      values[i] = meals.get(i).mealName;
    }
    return values;
  }


}
