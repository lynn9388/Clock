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


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lynn9388.clock.R;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class StopwatchFragment extends Fragment {
    public static final String START_TIME
            = "com.lynn9388.clock.Fragments.StopwatchFragment.START_TIME";
    public static final String ELAPSED_TIME
            = "com.lynn9388.clock.Fragments.StopwatchFragment.ELAPSED_TIME";
    public static final String RECORDS
            = "com.lynn9388.clock.Fragments.StopwatchFragment.RECORDS";
    private static String SPLIT_STRING = "\t";
    private TextView mainTimeView;
    private TextView millisecondView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private StopwatchAdapter adapter;
    private long startTime;
    private long elapsedTime;
    private List<String> records;
    private long lastRecordTime;
    private Handler handler = new Handler();
    private Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            elapsedTime = SystemClock.elapsedRealtime() - startTime;
            setTimeView();
            handler.postDelayed(this, 10);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        mainTimeView = (TextView) view.findViewById(R.id.text_view_stopwatch_main_time);
        millisecondView = (TextView) view.findViewById(R.id.text_view_stopwatch_millisecond);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_stopwatch);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new StopwatchAdapter();

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        if (getState() == State.RUNNING) {
            startStopwatch();
        } else {
            setTimeView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(timerTask);
        saveData();
    }

    private void saveData() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(START_TIME, startTime);
        editor.putLong(ELAPSED_TIME, elapsedTime);
        StringBuilder builder = new StringBuilder();
        for (String record : records) {
            builder.append(record + "/");
        }
        editor.putString(RECORDS, builder.toString().trim());
        editor.commit();
    }

    private void loadData() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        startTime = preferences.getLong(START_TIME, 0);
        elapsedTime = preferences.getLong(ELAPSED_TIME, 0);
        String recordsString = preferences.getString(RECORDS, "");
        records = new ArrayList<String>();
        for (String record : recordsString.split("/")) {
            records.add(record);
        }
        if (records.size() == 1 && records.get(0).equals("")) {
            records.clear();
            lastRecordTime = 0;
        } else {
            lastRecordTime = recordToTime(records.get(records.size() - 1).split(SPLIT_STRING)[1]);
        }
    }

    private void setTimeView() {
        String[] timeStrings = timeToString(elapsedTime);
        mainTimeView.setText(timeStrings[0]);
        millisecondView.setText(timeStrings[1]);
    }

    private String[] timeToString(long time) {
        String[] timeStrings = new String[2];
        int hour = (int) (time / 3600000);
        time %= 3600000;
        int minute = (int) (time / 60000);
        time %= 60000;
        int second = (int) (time / 1000);
        int millisecond = (int) (time % 1000);
        if (hour > 0) {
            timeStrings[0] = String.format("%d:%02d:%02d", hour, minute, second);
        } else {
            timeStrings[0] = String.format("%02d:%02d", minute, second);
        }
        timeStrings[1] = String.format("%02d", millisecond / 10);
        return timeStrings;
    }

    private String timeToRecord(long time) {
        String[] record = timeToString(time);
        return record[0] + "." + record[1];
    }

    private long recordToTime(String record) {
        long time = 0;
        String[] values = record.split("\\.");
        String[] main = values[0].split(":");
        if (main.length == 3) {
            time = Integer.valueOf(main[0]) * 3600000
                    + Integer.valueOf(main[1]) * 60000
                    + Integer.valueOf(main[2]) * 1000
                    + Integer.valueOf(values[1]) * 10;
        } else {
            time = Integer.valueOf(main[0]) * 60000
                    + Integer.valueOf(main[1]) * 1000
                    + Integer.valueOf(values[1]) * 10;
        }
        return time;
    }

    public State getState() {
        State state = null;
        if (startTime == 0) {
            if (elapsedTime == 0) {
                state = State.UNSTARTED;
            } else {
                state = State.PAUSED;
            }
        } else {
            state = State.RUNNING;
        }
        return state;
    }

    public void startStopwatch() {
        if (getState() == State.UNSTARTED) {
            startTime = SystemClock.elapsedRealtime();
        } else if (getState() == State.PAUSED) {
            startTime = SystemClock.elapsedRealtime() - elapsedTime;
        }
        handler.removeCallbacks(timerTask);
        handler.post(timerTask);
    }

    public void pauseStopwatch() {
        handler.removeCallbacks(timerTask);
        startTime = 0;
    }

    public void resetStopwatch() {
        startTime = 0;
        elapsedTime = 0;
        records.clear();
        lastRecordTime = 0;
        setTimeView();
        adapter.notifyDataSetChanged();
    }

    public void addRecord() {
        long increaseTime = elapsedTime - lastRecordTime;
        lastRecordTime = elapsedTime;
        records.add("+" + timeToRecord(increaseTime) + SPLIT_STRING + timeToRecord(elapsedTime));
        adapter.notifyItemInserted(0);
        layoutManager.scrollToPosition(0);
    }

    public void shareRecords() {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < records.size(); index++) {
            builder.append(String.format("%02d", index + 1)
                    + SPLIT_STRING
                    + records.get(index)
                    + "\n");
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        intent.setType("text/plain");
        startActivity(intent);
    }

    public static enum State {UNSTARTED, PAUSED, RUNNING}

    public class StopwatchAdapter extends RecyclerView.Adapter<StopwatchAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_stopwatch, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            int index = getItemCount() - position - 1;
            String[] record = records.get(index).split(SPLIT_STRING);
            holder.position.setText(String.format("%02d", index + 1));
            holder.increaseTime.setText(record[0]);
            holder.elapsedTime.setText(record[1]);
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView position;
            public TextView increaseTime;
            public TextView elapsedTime;

            public ViewHolder(View itemView) {
                super(itemView);
                position = (TextView) itemView.findViewById(R.id.text_view_stopwatch_position);
                increaseTime = (TextView) itemView
                        .findViewById(R.id.text_view_stopwatch_increase_time);
                elapsedTime = (TextView) itemView
                        .findViewById(R.id.text_view_stopwatch_elapsed_time);
            }
        }
    }
}
