package mealrecommender.neva.com.neva_android_app;

import android.Manifest;
import android.app.FragmentManager;
import android.app.TimePickerDialog;;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import neva.backend.UserHistoryOuterClass.*;
import neva.backend.SuggestionOuterClass.*;
import neva.backend.util.Util.*;
import neva.backend.BackendOuterClass.*;
import neva.backend.BackendGrpc.*;

import java.sql.SQLException;
import java.util.Calendar;


public class AddHistoryItemFragment extends Fragment {

    private static final String TAG = "AddHistoryItemFragment";

    ByteString loginToken;
    ManagedChannel mChannel;
    BackendBlockingStub blockingStub;
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
        mChannel = mainActivity.mChannel;
        blockingStub = mainActivity.blockingStub;
        cursorAdapter = mainActivity.adapter;

        fm = mainActivity.getFragmentManager();

        mealNameField = view.findViewById(R.id.eaten_meal_field);
        timeField = view.findViewById(R.id.time_field);
        addHistoryButton = view.findViewById(R.id.sendMealHistory);

        mealNames = getSuggestionNames();

        adapter = new ArrayAdapter<>(getContext(), R.layout.textview_autocomplete_item, mealNames);
        mealNameField.setAdapter(adapter);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                1);

        timeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        Calendar cal = Calendar.getInstance();
                        cal.clear(Calendar.HOUR_OF_DAY);
                        cal.clear(Calendar.MINUTE);
                        cal.set(Calendar.HOUR_OF_DAY, i);
                        cal.set(Calendar.MINUTE, i1);
                        date = cal;

                        timeField.setText(String.format("%02d", i) + ":" + String.format("%02d", i1));
                    }
                };

                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int mins = currentTime.get(Calendar.MINUTE);

                TimePickerDialog tpDialog = new TimePickerDialog(getContext(),timeSetListener, hour, mins, true);
                tpDialog.setTitle("Select Time");
                tpDialog.show();
            }
        });

        addHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Location currentLoc = null;
                locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

                if(!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
                    Log.e(TAG, "Provider not enabled");
                } else {
                    currentLoc = getLocation();
                }

                double latitude=-1;
                double longitude=-1;
                if(currentLoc!=null) {
                    latitude = currentLoc.getLatitude();
                    longitude = currentLoc.getLongitude();
                } else {
                    latitude = 0;
                    longitude = 0;
                }

                Log.d(TAG, Double.toString(latitude)+" "+Double.toString(longitude));

                DatabaseManager dbman = new DatabaseManager(getContext());
                try {
                    dbman.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                int mealID = dbman.getMealId(mealNameField.getText().toString()); // TODO:GET MEAL ID DIRECTLY FROM TEXT BOX?

                long timezoneOffset = date.getTimeZone().getOffset(date.getTimeInMillis());

                Choice choice = Choice.newBuilder()
                        .setSuggesteeId(mealID)
                        .setTimestamp(Timestamp.newBuilder().setSeconds((int)((date.getTimeInMillis()+timezoneOffset)/1000)))
                        .setLatitude(latitude)
                        .setLongitude(longitude)
                        .build();

                InformUserChoiceRequest informUserChoiceRequest;
                informUserChoiceRequest = InformUserChoiceRequest.newBuilder()
                                            .setChoice(choice).setToken(loginToken).build();
                try {
                    GenericReply genericReply = blockingStub.informUserChoice(informUserChoiceRequest);
                    dbman.addHistoryData(NevaLoginManager.getInstance().getUsername(), mealID, date);

                    Cursor cursor = dbman.getHistory();

                    cursorAdapter.swapCursor(cursor);

                    Toast.makeText(getContext(),"CLICK",Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                }catch (Exception e)
                {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public Location getLocation() {
        Location location = null;
        if(ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission Denied");
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
        else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return location;
    }

    String[] getSuggestionNames(){

        GetSuggestionItemListRequest request;
        request = GetSuggestionItemListRequest.newBuilder()
                .setToken(loginToken).setStartIndex(0)
                .setSuggestionCategory(Suggestion.SuggestionCategory.MEAL)
                .build();

        GetSuggestionItemListReply reply = blockingStub.getSuggestionItemList(request);
        String[] values;
        values = new String[reply.getItemsCount()];
        for(int i=0; i<reply.getItemsCount();i++)
        {
            values[i] = reply.getItems(i).getName();
        }

        return values;
    }

}
