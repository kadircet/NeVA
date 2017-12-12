package mealrecommender.neva.com.neva_android_app;

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
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.sql.SQLException;

import io.grpc.ManagedChannel;

import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.SuggestionOuterClass;


public class HistoryFragment extends ListFragment {

    ByteString loginToken;
    ManagedChannel mChannel;
    BackendGrpc.BackendBlockingStub blockingStub;
    HistoryItemAdapter adapter;


    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        loginToken = mainActivity.loginToken;
        mChannel = mainActivity.mChannel;
        blockingStub = mainActivity.blockingStub;



        BackendOuterClass.GetSuggestionItemListRequest request;
        request = BackendOuterClass.GetSuggestionItemListRequest.newBuilder()
                .setToken(loginToken).setStartIndex(0)
                .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
                .build();

        BackendOuterClass.GetSuggestionItemListReply reply = blockingStub.getSuggestionItemList(request);
        SuggestionOuterClass.Suggestion[] values;
        values = new SuggestionOuterClass.Suggestion[(reply.getItemsCount())];
        getContext().deleteDatabase("MEAL_HISTORY.DB");
        DatabaseManager dbman = new DatabaseManager(getContext());
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
        dbman.close();



        adapter = new HistoryItemAdapter(getContext(), R.layout.fragment_history, R.id.firstLine, values);
        setListAdapter(adapter);

        return view;
    }

}
