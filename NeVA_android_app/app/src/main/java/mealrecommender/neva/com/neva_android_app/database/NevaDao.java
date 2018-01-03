package mealrecommender.neva.com.neva_android_app.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
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
  void addMeal(Meal meal);

  @Insert
  public void addHistoryEntry(HistoryEntry historyEntry);

  @Insert
  public void addHistoryEntires(List<HistoryEntry> historyEntries);

  @Insert
  public void addTag(Tag tag);

  @Insert
  public void addTags(List<Tag> tags);

  @Insert
  public void addMealTag(MealTagRelation relation);

  @Insert
  public void addMealTags(List<MealTagRelation> mealTagRelations);

  @Update
  public void updateMeal(Meal meal);

  @Update
  public void updateMeals(List<Meal> meals);

  @Update
  public void updateTag(Tag tag);

  @Update
  public void updateTags(List<Tag> tags);

  @Update
  public void updateMealTag(MealTagRelation relation);

  @Query("SELECT * FROM meals")
  public List<Meal> getAllMeals();

  @Query("SELECT * FROM meals WHERE id = :mealId")
  public Meal getMeal(int mealId);

  @Query("SELECT COUNT(*) FROM meals WHERE id = :mealId")
  public int mealExits(int mealId);

  @Query("SELECT COUNT(*) FROM tags WHERE id = :tagId")
  public int tagExists(int tagId);

  @Query("SELECT COUNT(*) FROM meal_tag_relations WHERE mealId = :mealId AND tagId = :tagId")
  public int mealTagRelationExists(int mealId, int tagId);

  @Query("SELECT name FROM tags WHERE id IN (:tagIds)")
  public String[] getTagNames(int[] tagIds);

  @Query("SELECT name FROM tags")
  public String[] getTagNames();

  @Query("SELECT id as _id, mealName, mealPicture FROM meals")
  public Cursor getCursorAllMeals();

  @Query("SELECT h.choiceId as _id, m.mealName, m.mealPicture, h.date FROM history as h "
      + "INNER JOIN meals as m ON m.id = h.mealId "
      + "ORDER BY h.date DESC")
  public Cursor getHistoryEntriesWithMealName();

  @Query("SELECT * FROM history")
  public List<HistoryEntry> getAllHistory();

  @Query("SELECT id FROM meals WHERE mealName = :mealName")
  public int getMealId(String mealName);

  @Query("SELECT id FROM tags WHERE name = :tagName")
  public int getTagId(String tagName);

  @Query("SELECT choiceId FROM history WHERE userMail = :userMail ORDER BY choiceId DESC LIMIT 1")
  public int getLastChoiceIdOfUser(String userMail);

  @Query("SELECT h.choiceId as _id, m.mealName, m.mealPicture, h.date FROM history as h "
      + "INNER JOIN meals as m ON m.id = h.mealId "
      + "WHERE h.usermail = :username ORDER BY h.date DESC")
  public Cursor getUserHistoryMeals(String username);
}
