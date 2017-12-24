package mealrecommender.neva.com.neva_android_app;

import android.database.Cursor;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.sql.SQLException;

import io.grpc.ManagedChannel;

import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.SuggestionOuterClass;


public class HistoryFragment extends ListFragment {

    private static final String TAG = "HistoryFragment";

    ByteString loginToken;
    ManagedChannel mChannel;
    BackendGrpc.BackendBlockingStub blockingStub;
    DatabaseManager dbman;
    HistoryCursorAdapter adapter;

    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        loginToken = NevaLoginManager.getInstance().getByteStringToken();
        mChannel = mainActivity.mChannel;
        blockingStub = mainActivity.blockingStub;
        dbman = mainActivity.dbman;
        adapter = mainActivity.adapter;

        BackendOuterClass.GetSuggestionItemListRequest request;
        request = BackendOuterClass.GetSuggestionItemListRequest.newBuilder()
                .setToken(loginToken).setStartIndex(0)
                .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
                .build();

        BackendOuterClass.GetSuggestionItemListReply reply = blockingStub.getSuggestionItemList(request);
        SuggestionOuterClass.Suggestion[] values;
        values = new SuggestionOuterClass.Suggestion[(reply.getItemsCount())];


        try {
            dbman.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for(int i=0; i<(reply.getItemsCount());i++)
        {
            dbman.addMeal(reply.getItems(i));
            values[i] = reply.getItems(i);
            Log.i("Values:", values[i].getName());
        }

        Cursor cursor = dbman.getHistory();

        dbman.close();

        cursor.moveToFirst();
        Log.d(TAG, Integer.toString(cursor.getCount()));
        for(int i=0; i<cursor.getCount(); i++)
        {
            Log.d(TAG, cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2));
            cursor.moveToNext();
        }
        cursor.moveToFirst();

        //adapter = new HistoryItemAdapter(getContext(), R.layout.fragment_history, R.id.firstLine, values);
        //setListAdapter(adapter);
        adapter = new HistoryCursorAdapter(getContext(), cursor, 0);
        mainActivity.adapter = adapter;
        setListAdapter(mainActivity.adapter);

        return view;
    }

}
