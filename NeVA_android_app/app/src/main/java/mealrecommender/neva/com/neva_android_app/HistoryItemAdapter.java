package mealrecommender.neva.com.neva_android_app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import neva.backend.SuggestionOuterClass;

/**
 * Created by hakan on 12/5/17.
 */

public class HistoryItemAdapter extends ArrayAdapter<SuggestionOuterClass.Suggestion> {

  private final String TAG = this.getClass().getSimpleName();

  SuggestionOuterClass.Suggestion[] meals;

  public HistoryItemAdapter(@NonNull Context context, int resource, int textViewResourceId,
      SuggestionOuterClass.Suggestion[] meals) {
    super(context, resource, textViewResourceId);
    this.meals = meals;
  }


  @Override
  public int getCount() {
    return meals.length;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(getContext());
      v = vi.inflate(R.layout.fragment_history, null);
    }

    SuggestionOuterClass.Suggestion item = meals[position];
    if (item != null) {
      TextView tt1 = v.findViewById(R.id.firstLine);
      TextView tt2 = v.findViewById(R.id.secondLine);

      if (tt1 != null) {
        Log.i("NAME: ", meals[position].getName());
        tt1.setText(meals[position].getName());
      } else {
        Log.e("NULL", "null");
      }

      if (tt2 != null) {
        tt2.setText(Integer.toString(position));
      }

    }

    return v;
  }
}
