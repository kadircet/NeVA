package mealrecommender.neva.com.neva_android_app;

import android.database.Cursor;
import android.os.Bundle;

import android.provider.ContactsContract.Intents.Insert;
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

import java.util.ArrayList;
import java.util.List;
import mealrecommender.neva.com.neva_android_app.database.Meal;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.SuggestionOuterClass;
import neva.backend.SuggestionOuterClass.Suggestion;


public class HistoryFragment extends ListFragment {

  private static final String TAG = "HistoryFragment";

  ByteString loginToken;
  ManagedChannel mChannel;
  BackendGrpc.BackendBlockingStub blockingStub;
  NevaDatabase db;
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
    db = mainActivity.db;
    adapter = mainActivity.adapter;

    BackendOuterClass.GetSuggestionItemListRequest request;
    request = BackendOuterClass.GetSuggestionItemListRequest.newBuilder()
        .setToken(loginToken).setStartIndex(0)
        .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
        .build();

    BackendOuterClass.GetSuggestionItemListReply reply = blockingStub
        .getSuggestionItemList(request);
    List<Meal> values = new ArrayList<>();

    for(Suggestion sug : reply.getItemsList()) {
      Meal meal = new Meal(sug.getSuggesteeId(), sug.getName(), "PhotoURL");
      values.add(meal);
    }
    Cursor cursor = db.nevaDao().getHistoryEntriesWithMealName();

    cursor.moveToFirst();
    Log.d(TAG, Integer.toString(cursor.getCount()));
    for (int i = 0; i < cursor.getCount(); i++) {
      Log.d(TAG, cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2));
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
