package mealrecommender.neva.com.neva_android_app.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by hakan on 12/27/17.
 */

@Database(entities = {Meal.class, HistoryEntry.class}, version = 2)
public abstract class NevaDatabase extends RoomDatabase{
  public abstract NevaDao nevaDao();
}
