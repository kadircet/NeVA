package mealrecommender.neva.com.neva_android_app.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;
import java.util.List;

/**
 * Created by hakan on 12/27/17.
 */
@Dao
public interface NevaDao {
  @Insert
  public void addMeals(List<Meal> meals);
  @Insert
  public void addHistoryEntry(HistoryEntry historyEntry);
  @Insert
  public void addHistoryEntires(HistoryEntry... historyEntries);

  @Query("SELECT * FROM meals")
  public List<Meal> getAllMeals();

  @Query("SELECT id as _id, mealName, mealPicture FROM meals")
  public Cursor getCursorAllMeals();

  @Query( "SELECT h.choiceId as _id, m.mealName, m.mealPicture, h.date FROM history as h "
          +"INNER JOIN meals as m ON m.id = h.mealId "
          +"ORDER BY h.date DESC")
  public Cursor getHistoryEntriesWithMealName();

  @Query("SELECT * FROM history")
  public List<HistoryEntry> getAllHistory();

  @Query("SELECT id FROM meals WHERE mealName = :mealName")
  public int getMealId(String mealName);
}
