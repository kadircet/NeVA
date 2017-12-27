package mealrecommender.neva.com.neva_android_app.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by hakan on 12/27/17.
 */
@Entity(tableName = "meals")
public class Meal {

  public Meal(int id, String mealName, String mealPicture) {
    this.id = id;
    this.mealName = mealName;
    this.mealPicture = mealPicture;
  }

  @PrimaryKey
  public int id;
  public String mealName;
  public String mealPicture;
}
