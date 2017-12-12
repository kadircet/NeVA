package mealrecommender.neva.com.neva_android_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import java.sql.SQLException;
import java.util.Calendar;

import neva.backend.SuggestionOuterClass;

/**
 * Created by hakan on 12/8/17.
 */

public class DatabaseManager {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private String mealTableColumns[] = {DatabaseHelper.MEAL_ID, DatabaseHelper.MEAL_NAME, DatabaseHelper.MEAL_PHOTO};
    private String historyTableColumns[] = {DatabaseHelper.HISTORY_USER_MAIL, DatabaseHelper.HISTORY_MEAL, DatabaseHelper.HISTORY_DATE};

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public DatabaseManager open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        return this;
    }


    public void addMeal(SuggestionOuterClass.Suggestion meal)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.MEAL_ID, meal.getSuggesteeId());
        values.put(DatabaseHelper.MEAL_NAME, meal.getName());
        values.put(DatabaseHelper.MEAL_PHOTO, "NoPhotoURL");


        long returnID = database.insert(DatabaseHelper.MEAL_TABLE, null, values);
        Cursor cursor = database.query(DatabaseHelper.MEAL_TABLE, mealTableColumns, null,
                        null, null, null, null);
        cursor.moveToFirst();
        Log.i("DB MEAL:", "MEAL");
        int i=0;
        do{
            Log.i("     E: ", Integer.toString(cursor.getCount())+" "+cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2));
        } while (cursor.moveToNext());
        if(cursor.isLast()) Log.i("LAST OUTSIDE LOOP", null);
        cursor.close();

    }
    public void addHistoryData(String email, String mealName, Calendar date)
    {
        String mealTableNameColumn[] = {DatabaseHelper.MEAL_ID, DatabaseHelper.MEAL_NAME};
        Cursor cursor = database.query(DatabaseHelper.MEAL_TABLE,mealTableNameColumn, DatabaseHelper.MEAL_NAME + "= ?" , new String[]{mealName}, null, null, null);
        Log.i("Find Meal Name Count: ", Integer.toString(cursor.getCount())); //TODO: MAKE TEXT BOX RETURN MEAL ID TO DECREASE QUERY NUMS
        cursor.moveToFirst();
        Log.i("FOUND: ", cursor.getString(0)+" "+cursor.getString(1));

        int mealID = cursor.getInt(0);
        int epochDate = (int)(date.getTimeInMillis()/1000);


        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.HISTORY_USER_MAIL, email);
        values.put(DatabaseHelper.HISTORY_MEAL, mealID);
        values.put(DatabaseHelper.HISTORY_DATE, epochDate);

        long returnID = database.insert(DatabaseHelper.HISTORY_TABLE, null, values);

        cursor = database.query(DatabaseHelper.HISTORY_TABLE, historyTableColumns, null, null, null, null, null);
        cursor.moveToFirst();
        for(int i=0; i<cursor.getCount(); i++)
        {
            Log.w("         H:", cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2));
            cursor.moveToNext();
        }
    }

    public void close() {
        dbHelper.close();
    }


}
