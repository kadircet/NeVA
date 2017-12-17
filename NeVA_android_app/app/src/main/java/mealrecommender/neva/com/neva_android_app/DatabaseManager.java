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
    private static final String TAG = "DatabaseManager";

    private DatabaseHelper dbHelper;
    //
    //  DIRTY FIX - BAD PACTICE
    //  TODO: FIX PUBLIC DATABASE ACCESS
    //
    public SQLiteDatabase database;

    private String mealTableColumns[] = {DatabaseHelper.MEAL_ID, DatabaseHelper.MEAL_NAME, DatabaseHelper.MEAL_PHOTO};
    private String historyTableColumns[] = {DatabaseHelper.HISTORY_USER_MAIL, DatabaseHelper.HISTORY_MEAL, DatabaseHelper.HISTORY_DATE};

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public DatabaseManager open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        return this;
    }


    public long addMeal(SuggestionOuterClass.Suggestion meal)
    {
        Log.i(TAG, "Adding "+ meal.getName()+" to MEALS");

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.MEAL_ID, meal.getSuggesteeId());
        values.put(DatabaseHelper.MEAL_NAME, meal.getName());
        values.put(DatabaseHelper.MEAL_PHOTO, "NoPhotoURL");

        long rowNo = database.insert(DatabaseHelper.MEAL_TABLE, null, values);
        Cursor cursor = database.query(DatabaseHelper.MEAL_TABLE, mealTableColumns, null,
                        null, null, null, null);
        cursor.close();
        return rowNo;
    }

    public int getMealId(String mealName)
    {
        String mealTableNameColumn[] = {DatabaseHelper.MEAL_ID, DatabaseHelper.MEAL_NAME};
        Cursor c = database.query(DatabaseHelper.MEAL_TABLE,mealTableNameColumn,
                DatabaseHelper.MEAL_NAME + "= ?" , new String[]{mealName},
                null, null, null);
        c.moveToFirst();
        return c.getInt(0);
    }

    public void resetTables()
    {
        database.delete(DatabaseHelper.MEAL_TABLE, null, null);
        database.delete(DatabaseHelper.HISTORY_TABLE, null, null);
    }

    public long addHistoryData(String email, int mealID, Calendar date)
    {
        Log.i(TAG, "Adding History Data");

        int epochDate = (int)(date.getTimeInMillis()/1000);


        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.HISTORY_USER_MAIL, email);
        values.put(DatabaseHelper.HISTORY_MEAL, mealID);
        values.put(DatabaseHelper.HISTORY_DATE, epochDate);

        long rowNo = database.insert(DatabaseHelper.HISTORY_TABLE, null, values);

        return rowNo;
    }

    public Cursor getHistory()
    {
        Log.i(TAG, "Getting History from DB");
        Cursor cursor = database.rawQuery("SELECT m.id as _id, m.meal_name, m.meal_photo, h.date" +
                                            " FROM MEALS as m CROSS JOIN HISTORY as h" +
                                            " WHERE m.id=h.meal" +
                                            " ORDER BY h.date DESC", null, null);
        Cursor cursora = database.rawQuery("SELECT id as _id, meal_name, meal_photo"+
                                            " FROM MEALS ", null,null);

        Log.d(TAG, Integer.toString(cursora.getCount()));
        Log.d(TAG, Integer.toString(cursor.getCount()));

        cursor.moveToFirst();
        for(int i=0; i<cursor.getCount(); i++)
        {
            Log.d("Cursor: ", cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getString(3));
            cursor.moveToNext();
        }
        return cursor;
    }

    public void close() {
        dbHelper.close();
    }


}
