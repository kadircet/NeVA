package mealrecommender.neva.com.neva_android_app;


import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Locale;

import io.grpc.Context;
import io.grpc.ManagedChannel;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.SuggestionOuterClass;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddHistoryItemFragment extends Fragment {

    private static final String TAG = "AddHistoryItemFragment";

    ByteString loginToken;
    ManagedChannel mChannel;
    BackendGrpc.BackendBlockingStub blockingStub;
    FragmentManager fm;
    HistoryCursorAdapter cursorAdapter;

    String[] mealNames;
    ArrayAdapter<String> adapter;

    AutoCompleteTextView mealNameField;
    EditText timeField;
    Button addHistoryButton;

    Calendar date;

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

        adapter = new ArrayAdapter<String>(getContext(), R.layout.textview_autocomplete_item, mealNames);
        mealNameField.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                        timeField.setText(i + ":" + i1);
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

                DatabaseManager dbman = new DatabaseManager(getContext());
                try {
                    dbman.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                dbman.addHistoryData("hkn@test.com", mealNameField.getText().toString(), date);

                Cursor cursor = dbman.getHistory();

                cursorAdapter.swapCursor(cursor);

                Toast.makeText(getContext(),"CLICK",Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed();
            }
        });
    }

    String[] getSuggestionNames(){

        BackendOuterClass.GetSuggestionItemListRequest request;
        request = BackendOuterClass.GetSuggestionItemListRequest.newBuilder()
                .setToken(loginToken).setStartIndex(0)
                .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
                .build();

        BackendOuterClass.GetSuggestionItemListReply reply = blockingStub.getSuggestionItemList(request);
        String[] values;
        values = new String[reply.getItemsCount()];
        for(int i=0; i<reply.getItemsCount();i++)
        {
            values[i] = reply.getItems(i).getName();
        }

        return values;
    }

}
