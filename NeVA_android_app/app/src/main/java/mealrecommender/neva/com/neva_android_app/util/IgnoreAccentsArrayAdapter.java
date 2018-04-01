package mealrecommender.neva.com.neva_android_app.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class IgnoreAccentsArrayAdapter<T> extends BaseAdapter implements Filterable {
  // Data Source
  private List<T> mObjects;

  // Lock for Filter
  private final Object mLock = new Object();
  private int mResource;
  private int mDropDownResource;
  private int mFieldId = 0;
  private boolean mNotifyOnChange = true;
  private Context mContext;
  private ArrayList<T> mOriginalValues;
  private TRArrayFilter mFilter;

  private LayoutInflater mInflater;

  public IgnoreAccentsArrayAdapter(Context context, int textViewResourceId) {
    init(context, textViewResourceId, 0, new ArrayList<T>());
  }

  public IgnoreAccentsArrayAdapter(Context context, int resource, int textViewResourceId) {
    init(context, resource, textViewResourceId, new ArrayList<T>());
  }

  public IgnoreAccentsArrayAdapter(Context context, int textViewResourceId, T[] objects) {
    init(context, textViewResourceId, 0, Arrays.asList(objects));
  }

  public IgnoreAccentsArrayAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
    init(context, resource, textViewResourceId, Arrays.asList(objects));
  }

  public IgnoreAccentsArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
    init(context, textViewResourceId, 0, objects);
  }

  public IgnoreAccentsArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
    init(context, resource, textViewResourceId, objects);
  }

  private void init(Context context, int resource, int textViewResourceId, List<T> objects) {
    mContext = context;
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mResource = mDropDownResource = resource;
    mObjects = objects;
    mFieldId = textViewResourceId;
  }

  public void add(T object) {
    if (mOriginalValues != null) {
      synchronized (mLock) {
        mOriginalValues.add(object);
        if (mNotifyOnChange) notifyDataSetChanged();
      }
    } else {
      mObjects.add(object);
      if (mNotifyOnChange) notifyDataSetChanged();
    }
  }

  public void insert(T object, int index) {
    if (mOriginalValues != null) {
      synchronized (mLock) {
        mOriginalValues.add(index, object);
        if (mNotifyOnChange) notifyDataSetChanged();
      }
    } else {
      mObjects.add(index, object);
      if (mNotifyOnChange) notifyDataSetChanged();
    }
  }

  public void remove(T object) {
    if (mOriginalValues != null) {
      synchronized (mLock) {
        mOriginalValues.remove(object);
      }
    } else {
      mObjects.remove(object);
    }
    if (mNotifyOnChange) notifyDataSetChanged();
  }

  public void clear() {
    if (mOriginalValues != null) {
      synchronized (mLock) {
        mOriginalValues.clear();
      }
    } else {
      mObjects.clear();
    }
    if (mNotifyOnChange) notifyDataSetChanged();
  }

  public void sort(Comparator<? super T> comparator) {
    Collections.sort(mObjects, comparator);
    if (mNotifyOnChange) notifyDataSetChanged();
  }

  @Override
  public void notifyDataSetChanged() {
    super.notifyDataSetChanged();
    mNotifyOnChange = true;
  }

  public void setNotifyOnChange(boolean notifyOnChange) {
    mNotifyOnChange = notifyOnChange;
  }

  public Context getContext() {
    return mContext;
  }

  public int getCount() {
    return mObjects.size();
  }

  public T getItem(int position) {
    return mObjects.get(position);
  }

  public int getPosition(T item) {
    return mObjects.indexOf(item);
  }

  public long getItemId(int position) {
    return position;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    return createViewFromResource(position, convertView, parent, mResource);
  }

  private View createViewFromResource(int position, View convertView, ViewGroup parent,
      int resource) {
    View view;
    TextView text;

    if (convertView == null) {
      view = mInflater.inflate(resource, parent, false);
    } else {
      view = convertView;
    }

    try {
      if (mFieldId == 0) {
        //  If no custom field is assigned, assume the whole resource is a TextView
        text = (TextView) view;
      } else {
        //  Otherwise, find the TextView field within the layout
        text = (TextView) view.findViewById(mFieldId);
      }
    } catch (ClassCastException e) {
      Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
      throw new IllegalStateException(
          "ArrayAdapter requires the resource ID to be a TextView", e);
    }

    text.setText(getItem(position).toString());

    return view;
  }

  public void setDropDownViewResource(int resource) {
    this.mDropDownResource = resource;
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    return createViewFromResource(position, convertView, parent, mDropDownResource);
  }

  public static IgnoreAccentsArrayAdapter<CharSequence> createFromResource(Context context,
      int textArrayResId, int textViewResId) {
    CharSequence[] strings = context.getResources().getTextArray(textArrayResId);
    return new IgnoreAccentsArrayAdapter<CharSequence>(context, textViewResId, strings);
  }

  public TRArrayFilter getFilter() {
    if (mFilter == null) {
      mFilter = new TRArrayFilter();
    }
    return mFilter;
  }

  private class TRArrayFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence prefix) {
      FilterResults results = new FilterResults();

      if (mOriginalValues == null) {
        synchronized (mLock) {
          mOriginalValues = new ArrayList<T>(mObjects);
        }
      }

      if (prefix == null || prefix.length() == 0) {
        synchronized (mLock) {
          ArrayList<T> list = new ArrayList<T>(mOriginalValues);
          results.values = list;
          results.count = list.size();
        }
      } else {
        String prefixString = prefix.toString().toLowerCase();

        ArrayList<T> values = mOriginalValues;
        final int count = values.size();

        final ArrayList<T> newValues = new ArrayList<T>(count);
        final ArrayList<String> strippedAccents = new ArrayList<String>();

        for (int i = 0; i < count; i++) {
          final T value = values.get(i);
          final String valueText = value.toString().toLowerCase();
          String valueTextNoPalatals = stripAccents(valueText);
          String prefixStringNoPalatals = stripAccents(prefixString);

          // First match against the whole, non-splitted value
          if (valueText.startsWith(prefixString) || valueTextNoPalatals.startsWith(prefixStringNoPalatals)) {
            newValues.add(value);
          } else {
            final String[] words = valueText.split(" ");
            final int wordCount = words.length;

            for (int k = 0; k < wordCount; k++) {
              if (words[k].startsWith(prefixString)) {
                newValues.add(value);
                break;
              }
            }
          }
        }

        results.values = newValues;
        results.count = newValues.size();
      }

      return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      //noinspection unchecked
      mObjects = (List<T>) results.values;
      if (results.count > 0) {
        notifyDataSetChanged();
      } else {
        notifyDataSetInvalidated();
      }
    }
  }

  private static String stripAccents(String s) {
    s = Normalizer.normalize(s, Normalizer.Form.NFD);
    s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return s;
  }
}