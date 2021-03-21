package com.example.project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.QuickViewConstants;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Locale;

public class DatabaseHelper {
    private final Context context;
    private static final String DATABASE_NAME = "database.db";
    private static final int SCHEMA = 1;
    private static final String TABLE = "symbols";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_VECTOR = "vector";
    public static final String COLUMN_TELEPHONE_NUMBER = "telephone number";

    public DatabaseHelper(Context context) {
        this.context = context;
        onCreate();
    }

    public void onCreate() {
        SQLiteDatabase db = getDatabase();
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        String query = String.format(
                "CREATE TABLE IF NOT EXISTS %s (%s TEXT, %s TEXT);",
                TABLE, COLUMN_TELEPHONE_NUMBER, COLUMN_VECTOR
        );
        db.execSQL(query);
    }

    public void onUpgrade(int oldVersion, int newVersion) {
        SQLiteDatabase db = getDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
    }

    public void onInsert(String telephoneNumber, float[] vector) {
        SQLiteDatabase db = getDatabase();
        StringBuilder stringBuilder = new StringBuilder();
        for (float v : vector) {
            stringBuilder.append(v).append(" ");
        }
        String string = stringBuilder.toString();
        string = string.substring(0, string.length() - 1);
        @SuppressLint("DefaultLocale")
        String query = String.format(
                "INSERT INTO %s VALUES ('%s', '%s');",
                TABLE, telephoneNumber, string);
        db.execSQL(query);
    }
    public HashMap<String, Vector> onSelectAll() {
        SQLiteDatabase db = getDatabase();
        String query = String.format("SELECT * FROM %s;", TABLE);
        Cursor cursor = db.rawQuery(query, null);
        HashMap<String, Vector> vectors = new HashMap<>();
        HashMap<String, Integer> lengths = new HashMap<>();
        while (cursor.moveToNext()) {
            String telephoneNumber = cursor.getString(0);
            Log.v("telephone",  telephoneNumber);
            String string = cursor.getString(1);
            Vector vector = Vector.fromString(string);
            if (vectors.containsKey(telephoneNumber)) {
                vectors.put(telephoneNumber,
                        Vector.add(vectors.get(telephoneNumber), vector));
                lengths.put(telephoneNumber, lengths.get(telephoneNumber) + 1);
            } else {
                vectors.put(telephoneNumber, vector);
                lengths.put(telephoneNumber, 1);
            }
        }
        HashMap<String, Vector> results = new HashMap<>();
        for (String key : vectors.keySet()) {
            results.put(key, vectors.get(key).div(lengths.get(key)));
        }
        cursor.close();
        return results;
    }
    public SQLiteDatabase getDatabase() {
        return context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
    }
}
