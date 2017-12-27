package mealrecommender.neva.com.neva_android_app;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hakan on 12/12/17.
 */

public class HistoryCursorAdapter extends CursorAdapter {

  public static final String TAG = "HistoryCursorAdapter";

  LayoutInflater inflater;

  public HistoryCursorAdapter(Context context, Cursor cursor, int flags) {
    super(context, cursor, flags);
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    String mealName = cursor.getString(cursor.getColumnIndex("mealName"));
    long epochTime = cursor.getLong(cursor.getColumnIndex("date"));
    Date date = new Date(epochTime);
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, EEEE,MMMM d,yyyy");
    String dateText = sdf.format(date);
    Log.d(TAG, "Bind View: " + mealName + " " + dateText);
    //TODO: HANDLE PHOTO URL

    TextView fl = view.findViewById(R.id.firstLine);
    TextView sl = view.findViewById(R.id.secondLine);

    fl.setText(mealName);
    sl.setText(dateText);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return inflater.inflate(R.layout.fragment_history, parent, false);
  }

}
