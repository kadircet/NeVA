package mealrecommender.neva.com.neva_android_app;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import mealrecommender.neva.com.neva_android_app.database.HistoryEntry;
import mealrecommender.neva.com.neva_android_app.database.NevaDatabase;
import neva.backend.UserHistoryOuterClass.Choice;


public class HistoryFragment extends ListFragment {

  private final String TAG = this.getClass().getSimpleName();

  ByteString loginToken;
  NevaDatabase db;
  HistoryCursorAdapter adapter;
  NevaLoginManager nevaLoginManager;
  NevaConnectionManager connectionManager;

  public HistoryFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_history_list, container, false);

    MainActivity mainActivity = (MainActivity) getActivity();
    nevaLoginManager = NevaLoginManager.getInstance();
    connectionManager = NevaConnectionManager.getInstance();
    loginToken = nevaLoginManager.getByteStringToken();
    db = mainActivity.db;
    adapter = mainActivity.adapter;

    Log.d(TAG, "Getting the last stored \"choiceId\" in database");
    int lastChoiceId = db.nevaDao().getLastChoiceIdOfUser(nevaLoginManager.getEmail());
    Log.e(TAG, Integer.toString(lastChoiceId) + " " + nevaLoginManager.getEmail());
    try {
      Log.d(TAG, "Sending FetchUserHistoryRequest to server");
      List<Choice> fetchedHistory = connectionManager.fetchUserHistory(lastChoiceId);//fetchUserHistoryReply.getUserHistory().getHistoryList();
      Log.d(TAG, "Got FetchUserHistoryReply, Creating HistoryEntries");
      List<HistoryEntry> historyEntries = userHistoryToHistoryEntry(fetchedHistory);
      Log.d(TAG, "Adding Fetched HistoryEntries to the database");
      db.nevaDao().addHistoryEntires(historyEntries);
    } catch (Exception e) {
      Toast.makeText(getContext(), getResources().getString(R.string.error_fetch_userhistory), Toast.LENGTH_LONG).show();
    }
    Log.d(TAG, "Getting meal names for HistoryEntries");
    //Cursor cursor = db.nevaDao().getHistoryEntriesWithMealName();
    Cursor cursor = db.nevaDao().getUserHistoryMeals(nevaLoginManager.getEmail());
    cursor.moveToFirst();
    adapter = new HistoryCursorAdapter(getContext(), cursor, 0);
    mainActivity.adapter = adapter;
    setListAdapter(mainActivity.adapter);

    return view;
  }

  List<HistoryEntry> userHistoryToHistoryEntry(List<Choice> fetchedChoices) {
    List<HistoryEntry> meals = new ArrayList<HistoryEntry>();
    for (Choice choice : fetchedChoices) {
      int choiceId = choice.getChoiceId();
      String username = nevaLoginManager.getEmail();
      int suggesteeId = choice.getSuggesteeId();
      long epochTime = choice.getTimestamp().getSeconds() *1000;
      long timezoneOffset = Calendar.getInstance().getTimeZone().getRawOffset();
      meals.add(new HistoryEntry(choiceId, username, suggesteeId, epochTime-timezoneOffset));
      Log.d(TAG, "::userHistoryToHistoryEntry: Converted "+Integer.toString(choiceId)+" "
          +Integer.toString(suggesteeId)+" "+ Long.toString(epochTime));
    }
    return meals;
  }
}
