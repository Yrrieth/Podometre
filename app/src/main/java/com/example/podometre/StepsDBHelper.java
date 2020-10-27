package com.example.podometre;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class StepsDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StepsDataBase.db";
    private static final String TABLE_STEPS_SUMMARY = "StepsSummary";
    private static final String ID = "id";
    private static final String STEPS_COUNT = "StepsCount";
    private static final String CREATION_DATE = "CreationDate";

    private static final String CREATE_TABLE_STEPS_SUMMARY = "CREATE TABLE " + TABLE_STEPS_SUMMARY
            + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CREATION_DATE + " TEXT, "
            + STEPS_COUNT + " INTEGER"
            + ");";

    public StepsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STEPS_SUMMARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean createStepsEntry()  {
        boolean isDateAlreadyPresent = false;
        boolean createSuccessful = false;
        int currentDateStepCounts = 0;
        Calendar calendar = Calendar.getInstance();
        String todayDate = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + "/"
                + String.valueOf(calendar.get(Calendar.MONTH) + 1)+ "/"
                + String.valueOf(calendar.get(Calendar.YEAR));

        String selectQuery = "SELECT " + STEPS_COUNT + " FROM " + TABLE_STEPS_SUMMARY
                + " WHERE " + CREATION_DATE + " = '" + todayDate + "'";

        // Try to read and to loop in the table of the DB
        try {
            // getReadableDatabase() crée une BDD si il n'y en pas
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    isDateAlreadyPresent = true;
                    currentDateStepCounts = c.getInt(c.getColumnIndex(STEPS_COUNT));
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CREATION_DATE, todayDate);
            if (isDateAlreadyPresent) {
                values.put(STEPS_COUNT, ++currentDateStepCounts);
                int row = db.update(TABLE_STEPS_SUMMARY, values, CREATION_DATE + " = '" + todayDate + "'", null);
                if (row == 1) {
                    createSuccessful = true;
                }
                db.close();
            } else {
                values.put(STEPS_COUNT, 1);
                long row = db.insert(TABLE_STEPS_SUMMARY, null, values);
                if (row != -1) {
                    createSuccessful = true;
                }
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createSuccessful;
    }

    public ArrayList<DateStepsModel> readStepsEntries() {
        ArrayList<DateStepsModel> stepCountList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STEPS_SUMMARY;

        try {
            SQLiteDatabase db = this. getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    DateStepsModel dateStepsModel = new DateStepsModel();
                    dateStepsModel.date = c.getString(c.getColumnIndex(CREATION_DATE));
                    dateStepsModel.stepCount = c.getInt(c.getColumnIndex(STEPS_COUNT));
                    stepCountList.add(dateStepsModel);
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stepCountList;
    }

    public DateStepsModel readCurrentDateStepEntry(String currentDate) {
        DateStepsModel currentStepEntry = new DateStepsModel();
        String selectQuery = "SELECT * FROM " + TABLE_STEPS_SUMMARY + " WHERE " + CREATION_DATE + " = '" + currentDate + "'";

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                currentStepEntry.date = c.getString(c.getColumnIndex(CREATION_DATE));
                currentStepEntry.stepCount = c.getInt(c.getColumnIndex(STEPS_COUNT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentStepEntry;
    }
}
