package mealrecommender.neva.com.neva_android_app.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by hakan on 12/27/17.
 */

@Entity(tableName = "history",foreignKeys = @ForeignKey(entity = Meal.class, parentColumns = "id", childColumns = "mealId", onDelete = ForeignKey.CASCADE))
public class HistoryEntry {

  public HistoryEntry(int choiceId, String userMail, int mealId, long date) {
    this.choiceId = choiceId;
    this.userMail = userMail;
    this.mealId = mealId;
    this.date = date;
  }

  @PrimaryKey
  public int choiceId;
  public String userMail;
  public int mealId;
  public long date;
}
