package com.example.gesturaapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ScoreDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "scores.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SCORES = "scores";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CATEGORY = "category"; // Quiz or Replication
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_DATE = "dateTaken";

    public ScoreDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_SCORES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_SCORE + " INTEGER, " +
                COLUMN_DATE + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }

    // ✅ Add a new score record
    public void addScore(QuizResult result) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, result.getCategory());
        values.put(COLUMN_SCORE, result.getScore());
        values.put(COLUMN_DATE, result.getDateTaken());
        db.insert(TABLE_SCORES, null, values);
        db.close();
    }

    // ✅ Get all score history
    public List<QuizResult> getAllScores() {
        List<QuizResult> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SCORES + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                QuizResult result = new QuizResult();
                result.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                result.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                result.setScore(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE)));
                result.setDateTaken(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                results.add(result);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return results;
    }
}
