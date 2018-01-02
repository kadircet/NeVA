package mealrecommender.neva.com.neva_android_app.database;

import android.arch.persistence.room.Entity;

/**
 * Created by hakan on 1/2/18.
 */
@Entity(tableName = "meal_tag_relations", primaryKeys = {"mealId", "tagId"})
public class MealTagRelation {

  MealTagRelation(int mealId, int tagId) {
    this.mealId = mealId;
    this.tagId = tagId;
  }

  public int mealId;
  public int tagId;

}
