package mealrecommender.neva.com.neva_android_app;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import java.text.SimpleDateFormat;
import java.util.Date;
import mealrecommender.neva.com.neva_android_app.util.ColorGenerator;

/**
 * Created by hakan on 12/12/17.
 */

public class HistoryCursorAdapter extends CursorAdapter {

  public final String TAG = this.getClass().getSimpleName();
  final String dateFormat = "HH:mm, EEEE, MMMM d YYYY";
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
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    String dateText = sdf.format(date);
    Log.d(TAG, "Bind View: " + mealName + " " + dateText);
    //TODO: HANDLE PHOTO URL
    // if(not photo url) {
    int bgColor = ColorGenerator.getColor(mealName);

    TextDrawable mealPic = TextDrawable.builder()
        .buildRound(mealName.substring(0,1), bgColor);

    ImageView icon = view.findViewById(R.id.meal_icon);
    TextView fl = view.findViewById(R.id.firstLine);
    TextView sl = view.findViewById(R.id.secondLine);

    icon.setImageDrawable(mealPic);
    fl.setText(mealName);
    sl.setText(dateText);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return inflater.inflate(R.layout.fragment_history, parent, false);
  }

}
