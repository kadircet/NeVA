package mealrecommender.neva.com.neva_android_app.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by hakan on 1/2/18.
 */

@Entity(tableName = "tags")
public class Tag {

  Tag(int id, String name){
    this.id = id;
    this.name = name;
  }

  @PrimaryKey
  public int id;
  public String name;
}
