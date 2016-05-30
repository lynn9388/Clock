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

package com.lynn9388.clock.Fragments;


import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.amulyakhare.textdrawable.TextDrawable;
import com.lynn9388.clock.AlarmReceiver;
import com.lynn9388.clock.R;
import com.lynn9388.clock.util.Clock;
import com.lynn9388.clock.util.DatabaseHelper;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmFragment extends Fragment {
    public static final String SETTINGS_POSITION
            = "com.lynn9388.clock.Fragments.AlarmFragment.SETTINGS_POSITION";

    AlarmReceiver alarmReceiver = new AlarmReceiver();

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private AlarmAdapter alarmAdapter;
    private Cursor cursor;

    public AlarmFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        databaseHelper = new DatabaseHelper(getActivity());

        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_alarm);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        alarmAdapter = new AlarmAdapter();
        recyclerView.setAdapter(alarmAdapter);
        alarmAdapter.setSettingItemPosition(-1);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        int position = preferences.getInt(SETTINGS_POSITION, -1);
        alarmAdapter.setSettingItemPosition(position);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SETTINGS_POSITION, alarmAdapter.getSettingItemPosition());
        editor.commit();
    }

    private void refreshData() {
        cursor = Clock.AlarmEntry.queryAll(databaseHelper);
    }

    private int getAlarmPosition(int id) {
        int position = -1;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getInt(cursor.getColumnIndex(Clock.AlarmEntry._ID)) == id) {
                position = cursor.getPosition();
                break;
            }
            cursor.moveToNext();
        }
        return position;
    }

    public void addAlarm(int hourOfDay, int minute) {
        int time = hourOfDay * 60 + minute;
        long id = Clock.AlarmEntry.insert(databaseHelper, time, true, "", "", true, "");
        refreshData();
        int position = getAlarmPosition((int) id);
        alarmAdapter.setSettingItemPosition(position);
        alarmAdapter.notifyItemInserted(position);
        layoutManager.scrollToPosition(position);

        alarmReceiver.setAlarm(getActivity());
    }

    public class AlarmAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int ALARM_TYPE = 1;
        private static final int ALARM_SETTINGS_TYPE = 2;
        private final String[] WEEK = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri",
                "Sat"};
        private int settingItemPosition;

        @Override
        public int getItemViewType(int position) {
            return position == settingItemPosition ? ALARM_SETTINGS_TYPE : ALARM_TYPE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            RecyclerView.ViewHolder viewHolder = null;
            if (viewType == ALARM_TYPE) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_alarm, parent, false);
                viewHolder = new AlarmHolder(view);
            } else if (viewType == ALARM_SETTINGS_TYPE) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_alarm_settings, parent, false);
                viewHolder = new AlarmSettingsHolder(view);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            cursor.moveToPosition(position);
            int id = cursor.getInt(cursor.getColumnIndex(Clock.AlarmEntry._ID));
            int time = cursor.getInt(cursor.getColumnIndex(Clock.AlarmEntry.COLUMN_NAME_TIME));
            int enable = cursor
                    .getInt(cursor.getColumnIndex(Clock.AlarmEntry.COLUMN_NAME_ENABLE));
            String repeat = cursor
                    .getString(cursor.getColumnIndex(Clock.AlarmEntry.COLUMN_NAME_REPEAT));
            if (getItemViewType(position) == ALARM_SETTINGS_TYPE) {
                String ringtone = cursor
                        .getString(cursor.getColumnIndex(Clock.AlarmEntry.COLUMN_NAME_RINGTONE));
                int vibrate = cursor
                        .getInt(cursor.getColumnIndex(Clock.AlarmEntry.COLUMN_NAME_VIBRATE));
                String label = cursor
                        .getString(cursor.getColumnIndex(Clock.AlarmEntry.COLUMN_NAME_LABEL));
                AlarmSettingsHolder settingsHolder = (AlarmSettingsHolder) holder;
                settingsHolder.id = id;
                settingsHolder.time.setText(timeToString(time));
                settingsHolder.enable.setChecked(enable == 1 ? true : false);
                for (int week = 0; week < 7; week++) {
                    settingsHolder.setWeeks(week, repeat.contains(String.valueOf(week)));
                }
                settingsHolder.ringtone.setText(ringtone);
                settingsHolder.vibrate.setChecked(vibrate == 1 ? true : false);
                settingsHolder.label.setText(label);
            } else if (getItemViewType(position) == ALARM_TYPE) {
                AlarmHolder alarmHolder = (AlarmHolder) holder;
                alarmHolder.id = id;
                alarmHolder.time.setText(timeToString(time));
                alarmHolder.enable.setChecked(enable == 1 ? true : false);
                StringBuilder builder = new StringBuilder();
                if (repeat.equals("")) {
                    alarmHolder.repeat.setText("Only once");
                } else if (repeat.equals("06")) {
                    alarmHolder.repeat.setText("Weekend");
                } else if (repeat.equals("12345")) {
                    alarmHolder.repeat.setText("Weekdays");
                } else if (repeat.equals("0123456")) {
                    alarmHolder.repeat.setText("Every day");
                } else {
                    for (int week = 0; week < 7; week++) {
                        if (repeat.contains(String.valueOf(week))) {
                            builder.append(WEEK[week] + " ");
                        }
                    }
                    alarmHolder.repeat.setText(builder.toString());
                }
            }
        }

        @Override
        public int getItemCount() {
            return cursor.getCount();
        }

        private int stringToTime(String timeString) {
            String[] values = timeString.split(":");
            int hour = Integer.valueOf(values[0]);
            int minute = Integer.valueOf(values[1]);
            return hour * 60 + minute;
        }

        private String timeToString(int time) {
            int hour = time / 60;
            int minute = time % 60;
            return String.format("%02d:%02d", hour, minute);
        }

        public int getSettingItemPosition() {
            return settingItemPosition;
        }

        public void setSettingItemPosition(int position) {
            settingItemPosition = position;
        }

        public class AlarmHolder extends RecyclerView.ViewHolder {
            public TextView time;
            public Switch enable;
            public TextView repeat;
            private int id;

            public AlarmHolder(View itemView) {
                super(itemView);
                time = (TextView) itemView.findViewById(R.id.text_view_alarm_time);
                enable = (Switch) itemView.findViewById(R.id.switch_alarm);
                repeat = (TextView) itemView.findViewById(R.id.text_view_alarm_repeat);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAlarmPosition(id);
                        setSettingItemPosition(position);
                        notifyItemChanged(position);
                    }
                });

                time.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alarmAdapter.setSettingItemPosition(getAlarmPosition(id));
                        alarmAdapter.notifyItemChanged(getAlarmPosition(id));
                        TextView timeView = (TextView) v;
                        String[] time = timeView.getText().toString().split(":");
                        int hourOfDay = Integer.valueOf(time[0]);
                        int minute = Integer.valueOf(time[1]);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        int fromPosition = getAlarmPosition(id);
                                        Clock.AlarmEntry.update(databaseHelper,
                                                id,
                                                hourOfDay * 60 + minute,
                                                null, null, null, null, null);
                                        refreshData();
                                        int toPosition = getAlarmPosition(id);
                                        alarmAdapter.setSettingItemPosition(toPosition);
                                        alarmAdapter.notifyItemMoved(fromPosition, toPosition);
                                        alarmAdapter.notifyItemChanged(toPosition);
                                    }
                                }, hourOfDay, minute, true);
                        timePickerDialog.show();
                    }
                });

                enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Clock.AlarmEntry.update(databaseHelper, id, null, isChecked, null, null,
                                null, null);
                        refreshData();
                    }
                });
            }
        }

        public class AlarmSettingsHolder extends RecyclerView.ViewHolder {
            public TextView time;
            public Switch enable;
            public View[] weeks;
            public boolean[] weekSelected;
            public TextView ringtone;
            public CheckBox vibrate;
            public TextView label;
            public ImageButton delete;
            private int id;

            public AlarmSettingsHolder(View itemView) {
                super(itemView);
                time = (TextView) itemView.findViewById(R.id.text_view_alarm_settings_time);
                enable = (Switch) itemView.findViewById(R.id.switch_alarm_settings);
                weeks = new View[7];
                weeks[0] = (View) itemView.findViewById(R.id.include_alarm_settings_sunday);
                weeks[1] = (View) itemView.findViewById(R.id.include_alarm_settings_monday);
                weeks[2] = (View) itemView.findViewById(R.id.include_alarm_settings_tuesday);
                weeks[3] = (View) itemView.findViewById(R.id.include_alarm_settings_wednesday);
                weeks[4] = (View) itemView.findViewById(R.id.include_alarm_settings_thursday);
                weeks[5] = (View) itemView.findViewById(R.id.include_alarm_settings_friday);
                weeks[6] = (View) itemView.findViewById(R.id.include_alarm_settings_saturday);
                weekSelected = new boolean[7];
                ringtone = (TextView) itemView
                        .findViewById(R.id.text_view_alarm_settings_ringtone);
                vibrate = (CheckBox) itemView.findViewById(R.id.check_box_alarm_settings);
                label = (TextView) itemView.findViewById(R.id.edit_text_alarm_settings);
                delete = (ImageButton) itemView
                        .findViewById(R.id.image_button_alarm_settings_delete);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setSettingItemPosition(-1);
                        notifyItemChanged(getAlarmPosition(id));
                    }
                });

                for (int week = 0; week < 7; week++) {
                    final int finalWeek = week;
                    weeks[week].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setWeeks(finalWeek, !weekSelected[finalWeek]);
                            StringBuilder builder = new StringBuilder();
                            for (int week = 0; week < 7; week++) {
                                if (weekSelected[week] == true) {
                                    builder.append(week);
                                }
                            }
                            Clock.AlarmEntry.update(databaseHelper, id, null, null,
                                    builder.toString(), null, null, null);
                            if (builder.toString().contains("6")) {
                                Log.d("lynn", "contains");
                            }
                            refreshData();
                        }
                    });
                }

                time.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView timeView = (TextView) v;
                        String[] time = timeView.getText().toString().split(":");
                        int hourOfDay = Integer.valueOf(time[0]);
                        int minute = Integer.valueOf(time[1]);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        int fromPosition = getAlarmPosition(id);
                                        Clock.AlarmEntry.update(databaseHelper,
                                                id,
                                                hourOfDay * 60 + minute,
                                                null, null, null, null, null);
                                        refreshData();
                                        int toPosition = getAlarmPosition(id);
                                        alarmAdapter.setSettingItemPosition(toPosition);
                                        alarmAdapter.notifyItemMoved(fromPosition, toPosition);
                                        alarmAdapter.notifyItemChanged(toPosition);
                                    }
                                }, hourOfDay, minute, true);
                        timePickerDialog.show();
                    }
                });

                enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Clock.AlarmEntry.update(databaseHelper, id, null, isChecked, null, null,
                                null, null);
                        refreshData();
                    }
                });

                vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Clock.AlarmEntry.update(databaseHelper, id, null, null, null, null,
                                isChecked, null);
                        refreshData();
                    }
                });

                label.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Clock.AlarmEntry.update(databaseHelper, id, null, null, null, null,
                                null, s.toString());
                        refreshData();
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAlarmPosition(id);
                        Clock.AlarmEntry.delete(databaseHelper, id);
                        refreshData();
                        setSettingItemPosition(-1);
                        notifyItemRemoved(position);
                    }
                });
            }

            public void setWeeks(final int week, boolean selected) {
                weekSelected[week] = selected;
                TextDrawable drawable = null;
                int accentColor = ContextCompat.getColor(getActivity(), R.color.colorAccent);
                if (selected) {
                    drawable = TextDrawable.builder()
                            .buildRound(WEEK[week].substring(0, 2), accentColor);
                } else {
                    drawable = TextDrawable.builder()
                            .beginConfig()
                            .withBorder(4)
                            .textColor(accentColor)
                            .toUpperCase()
                            .endConfig()
                            .buildRound(WEEK[week].substring(0, 2), Color.WHITE);

                }
                ImageView imageView = (ImageView) weeks[week].findViewById(R.id.image_view_week);
                imageView.setImageDrawable(drawable);
            }
        }
    }
}
