package mealrecommender.neva.com.neva_android_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by hakan on 12/7/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DB_NAME = "MEAL_HISTORY.DB";

    public static final String MEAL_TABLE = "MEALS";

    public static final String MEAL_ID = "id";
    public static final String MEAL_NAME = "meal_name";
    public static final String MEAL_PHOTO = "meal_photo";

    public static final String HISTORY_TABLE = "HISTORY";

    public static final String HISTORY_USER_MAIL = "user_mail";
    public static final String HISTORY_MEAL = "meal";
    public static final String HISTORY_DATE = "date";

    public static final String CREATE_MEAL_TABLE = "create table "+MEAL_TABLE+" ( "+
            MEAL_ID + " integer primary key, "+
            MEAL_NAME + " text not null, "+
            MEAL_PHOTO + " text);";

    public static final String CREATE_HISTORY_TABLE = "create table "+HISTORY_TABLE+" ( "+
            HISTORY_USER_MAIL + " text not null, "+
            HISTORY_MEAL + " integer not null, "+
            HISTORY_DATE + " integer, "+
            " foreign key ( "+ HISTORY_MEAL +" )"+
            " references " + MEAL_TABLE + " ( " + MEAL_ID +" ) );";

    public static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d(DatabaseHelper.class.getName(), "CREATE MEAL");
        database.execSQL(CREATE_MEAL_TABLE);
        Log.d(DatabaseHelper.class.getName(), "CREATE HIST");
        database.execSQL(CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
        database.execSQL("DROP TABLE IF EXISTS " + MEAL_TABLE);
        onCreate(database);
    }

}
