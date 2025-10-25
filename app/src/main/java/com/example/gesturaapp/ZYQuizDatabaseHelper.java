package com.example.gesturaapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class ZYQuizDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quiz_history.db";
    private static final int DATABASE_VERSION = 3; // incremented again because of the new column
    private static final String TABLE_NAME = "quiz_results";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "type"; // "quiz" or "replication"
    private static final String COLUMN_SUBJECT = "subject"; // ✅ new column
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_TOTAL = "total";
    private static final String COLUMN_DATE = "date";

    public ZYQuizDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_SUBJECT + " TEXT, " +
                COLUMN_SCORE + " INTEGER, " +
                COLUMN_TOTAL + " INTEGER, " +
                COLUMN_DATE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // ✅ Insert new quiz or replication result
    public void insertQuizResult(String type, String subject, int score, int total, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_SUBJECT, subject);
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_TOTAL, total);
        values.put(COLUMN_DATE, date);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // ✅ Fetch all results
    public ArrayList<QuizResult> getAllResults() {
        ArrayList<QuizResult> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                String subject = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));
                int total = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

                list.add(new QuizResult(id, type, subject, score, total, date));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    // ✅ Delete a specific record by ID
    public void deleteResult(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
