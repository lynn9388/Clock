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

package com.lynn9388.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.lynn9388.clock.util.Clock;
import com.lynn9388.clock.util.DatabaseHelper;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ALARM_TIME_HOUR
            = "com.lynn9388.clock.AlarmReceiver.ALARM_TIME_HOUR";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
    }

    public void setAlarm(Context context) {
        int alarmTime = getNearestAlarmTime(context);
        int hour = alarmTime / 60;
        int minute = alarmTime % 60;
        Log.d("set:", hour + ":" + minute);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(ALARM_TIME_HOUR, hour);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    private int getNearestAlarmTime(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Cursor cursor = Clock.AlarmEntry.queryEnable(databaseHelper);
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int currentTime = hour * 60 + minute;
        int alarmTime = -1;
        cursor.moveToFirst();
        for (int i = 0; i < 7; i++) {
            int week = (i + dayOfWeek - 1) % 7;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int time = cursor.getInt(cursor.getColumnIndex(Clock.AlarmEntry.COLUMN_NAME_TIME));
                String repeat = cursor
                        .getString(cursor.getColumnIndex(Clock.AlarmEntry.COLUMN_NAME_REPEAT));
                if ((repeat.equals("") || repeat.contains(String.valueOf(dayOfWeek - 1)))
                        && time > currentTime) {
                    alarmTime = time;
                    break;
                } else if (repeat.contains(String.valueOf(week))) {
                    alarmTime = time;
                    break;
                }
                cursor.moveToNext();
            }
            if (alarmTime > 0) {
                break;
            }
        }
        return alarmTime;
    }
}
