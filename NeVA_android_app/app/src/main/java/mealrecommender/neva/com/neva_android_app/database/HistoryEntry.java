package mealrecommender.neva.com.neva_android_app.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import java.sql.Date;

/**
 * Created by hakan on 12/27/17.
 */

@Entity(tableName = "history",foreignKeys = @ForeignKey(entity = Meal.class, parentColumns = "id", childColumns = "mealId", onDelete = ForeignKey.CASCADE))
public class HistoryEntry {

  @PrimaryKey
  public int choiceId;
  public long date;
  public String userMail;
  public int mealId;

}
