package mealrecommender.neva.com.neva_android_app.customviews;

import android.content.Context;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by hakan on 2/20/18.
 */

public class NiceAutoCompleteTextView extends AppCompatAutoCompleteTextView {

  {
    addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        /** no-op */
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        /** no-op */
      }

      @Override
      public void afterTextChanged(Editable s) {
        mSelectionFromPopUp = false;
      }
    });
  }

  /**
   * A true value indicates that the user selected a suggested completion
   * from the popup, false otherwise.
   * @see #replaceText(CharSequence)
   */
  private boolean mSelectionFromPopUp;

  public NiceAutoCompleteTextView(Context context) {
    super(context);
  }

  public NiceAutoCompleteTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NiceAutoCompleteTextView(Context context, AttributeSet attrs,
      int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean enoughToFilter() {
    /**
     * There is no filtering standard; it is always enough to filter.
     */
    return true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (getAdapter() != null) {
      performFiltering(getText(), 0);
    }

    return super.onTouchEvent(event);
  }

  @Override
  protected void replaceText(CharSequence text) {
    super.replaceText(text);
    /**
     * The user selected an item from the suggested completion list.
     * The selection got converted to String, and replaced the whole content
     * of the edit box.
     */
    mSelectionFromPopUp = true;
  }

  public boolean isSelectionFromPopUp() {
    return mSelectionFromPopUp;
  }
}
