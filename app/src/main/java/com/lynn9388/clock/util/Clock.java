/*
 * Copyright (C) 2016  Lynn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lynn9388.clock.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by Lynn on 11/1/2015.
 */
public class Clock {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String NOT_NULL = " NOT NULL";
    private static final String COMMA_SEP = ",";

    public static abstract class ClockEntry implements BaseColumns {
        public static final String TABLE_NAME = "clock";
        public static final String COLUMN_NAME_PLACE = "place";
        public static final String COLUMN_NAME_CHECKED = "checked";

        public static final String SQL_CREATE_ENTRY =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP
                        + COLUMN_NAME_PLACE + TEXT_TYPE + NOT_NULL + COMMA_SEP
                        + COLUMN_NAME_CHECKED + INTEGER_TYPE + " );";

        public static long insert(DatabaseHelper databaseHelper, String place) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_PLACE, place);
            values.put(COLUMN_NAME_CHECKED, 0);

            return db.insert(TABLE_NAME, null, values);
        }

        public static final Cursor queryAll(DatabaseHelper databaseHelper) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String sortOrder = _ID + " DESC";
            return db.query(TABLE_NAME, null, null, null, null, null, sortOrder);
        }

        public static final Cursor queryChecked(DatabaseHelper databaseHelper) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            String selection = COLUMN_NAME_CHECKED + " = 1";
            String sortOrder = _ID + " DESC";

            return db.query(TABLE_NAME, null, selection, null, null, null, sortOrder);
        }

        public static final void update(DatabaseHelper databaseHelper, String place,
                                        boolean checked) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_CHECKED, checked ? 1 : 0);
            String whereClause = COLUMN_NAME_PLACE + "=?";
            String[] whereArgs = new String[]{place};
            db.update(TABLE_NAME, values, whereClause, whereArgs);
        }
    }


    public static abstract class AlarmEntry implements BaseColumns {
        public static final String TABLE_NAME = "alarm";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_ENABLE = "enable";
        public static final String COLUMN_NAME_REPEAT = "repeat";
        public static final String COLUMN_NAME_RINGTONE = "ringtone";
        public static final String COLUMN_NAME_VIBRATE = "vibrate";
        public static final String COLUMN_NAME_LABEL = "label";

        public static final String SQL_CREATE_ENTRY =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP
                        + COLUMN_NAME_TIME + INTEGER_TYPE + NOT_NULL + COMMA_SEP
                        + COLUMN_NAME_ENABLE + INTEGER_TYPE + NOT_NULL + COMMA_SEP
                        + COLUMN_NAME_REPEAT + TEXT_TYPE + NOT_NULL + COMMA_SEP
                        + COLUMN_NAME_RINGTONE + TEXT_TYPE + NOT_NULL + COMMA_SEP
                        + COLUMN_NAME_VIBRATE + INTEGER_TYPE + NOT_NULL + COMMA_SEP
                        + COLUMN_NAME_LABEL + TEXT_TYPE + " );";

        public static long insert(DatabaseHelper databaseHelper, int time, boolean enable,
                                  String repeat, String ringtone, boolean vibrate, String label) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_TIME, time);
            values.put(COLUMN_NAME_ENABLE, enable ? 1 : 0);
            values.put(COLUMN_NAME_REPEAT, repeat);
            values.put(COLUMN_NAME_RINGTONE, ringtone);
            values.put(COLUMN_NAME_VIBRATE, vibrate ? 1 : 0);
            values.put(COLUMN_NAME_LABEL, label);

            return db.insert(TABLE_NAME, null, values);
        }

        public static void delete(DatabaseHelper databaseHelper, int id) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            String selection = _ID + "=?";
            String[] selectionArgs = {String.valueOf(id)};
            db.delete(TABLE_NAME, selection, selectionArgs);
        }

        public static final Cursor queryAll(DatabaseHelper databaseHelper) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String sortOrder = COLUMN_NAME_TIME + " ASC";
            return db.query(TABLE_NAME, null, null, null, null, null, sortOrder);
        }

        public static final Cursor queryEnable(DatabaseHelper databaseHelper) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            String selection = COLUMN_NAME_ENABLE + " = 1";
            String sortOrder = COLUMN_NAME_TIME + " ASC";

            return db.query(TABLE_NAME, null, selection, null, null, null, sortOrder);
        }

        public static final void update(DatabaseHelper databaseHelper, int id, Integer time,
                                        Boolean enable, String repeat, String ringtone,
                                        Boolean vibrate, String label) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            if (time != null) {
                values.put(COLUMN_NAME_TIME, time);
            }
            if (enable != null) {
                values.put(COLUMN_NAME_ENABLE, enable ? 1 : 0);
            }
            if (repeat != null) {
                values.put(COLUMN_NAME_REPEAT, repeat);
            }
            if (ringtone != null) {
                values.put(COLUMN_NAME_RINGTONE, ringtone);
            }
            if (vibrate != null) {
                values.put(COLUMN_NAME_VIBRATE, vibrate ? 1 : 0);
            }
            if (label != null) {
                values.put(COLUMN_NAME_LABEL, label);
            }
            String whereClause = _ID + "=?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            db.update(TABLE_NAME, values, whereClause, whereArgs);
        }
    }
}
