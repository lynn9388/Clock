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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lynn9388.clock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimerFragment extends Fragment {
    public static final String TIME = "com.lynn9388.clock.Fragments.TimerFragment.TIME";
    private TextView hourView;
    private TextView minuteView;
    private TextView secondView;
    private char[] time;

    public TimerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        hourView = (TextView) view.findViewById(R.id.text_view_timer_hour);
        minuteView = (TextView) view.findViewById(R.id.text_view_timer_minute);
        secondView = (TextView) view.findViewById(R.id.text_view_timer_second);

        OnClickNumberListener onClickNumberListener = new OnClickNumberListener();
        ((Button) view.findViewById(R.id.button_timer_0)).setOnClickListener(onClickNumberListener);
        ((Button) view.findViewById(R.id.button_timer_1)).setOnClickListener(onClickNumberListener);
        ((Button) view.findViewById(R.id.button_timer_2)).setOnClickListener(onClickNumberListener);
        ((Button) view.findViewById(R.id.button_timer_3)).setOnClickListener(onClickNumberListener);
        ((Button) view.findViewById(R.id.button_timer_4)).setOnClickListener(onClickNumberListener);
        ((Button) view.findViewById(R.id.button_timer_5)).setOnClickListener(onClickNumberListener);
        ((Button) view.findViewById(R.id.button_timer_6)).setOnClickListener(onClickNumberListener);
        ((Button) view.findViewById(R.id.button_timer_7)).setOnClickListener(onClickNumberListener);
        ((Button) view.findViewById(R.id.button_timer_8)).setOnClickListener(onClickNumberListener);
        ((Button) view.findViewById(R.id.button_timer_9)).setOnClickListener(onClickNumberListener);

        ((ImageButton) view.findViewById(R.id.button_timer_settings))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

        ((ImageButton) view.findViewById(R.id.button_timer_backspace))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 4; i > 0; i--) {
                            time[i] = time[i - 1];
                        }
                        time[0] = '0';
                        setTimeView();
                        setFab();
                    }
                });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        setTimeView();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    public void saveData() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TIME, String.valueOf(time));
        editor.commit();
    }

    private void loadData() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        time = preferences.getString(TIME, "00000").toCharArray();
    }

    private void setTimeView() {
        hourView.setText(String.valueOf(time[0]));
        minuteView.setText(time, 1, 2);
        secondView.setText(time, 3, 2);
    }

    private void setFab() {
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (String.valueOf(time).equals("00000")) {
            fab.hide();
        } else {
            fab.setImageResource(R.drawable.ic_start);
            fab.show();
        }
    }

    public static enum State {UNSTARTED, PAUSED, RUNNING}

    private class OnClickNumberListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (time[0] == '0') {
                for (int i = 0; i < 4; i++) {
                    time[i] = time[i + 1];
                }
                time[4] = ((Button) v).getText().charAt(0);
                setTimeView();
                setFab();
            }
        }
    }
}
