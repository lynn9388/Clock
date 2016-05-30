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


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lynn9388.clock.R;
import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChronometerFragment extends Fragment {
    public static final String START_TIME
            = "com.lynn9388.clock.Fragments.ChronometerFragment.START_TIME";
    public static final String ELAPSED_TIME
            = "com.lynn9388.clock.Fragments.ChronometerFragment.ELAPSED_TIME";

    private TextView timeView;
    private HoloCircularProgressBar progressBar;
    private TextView chronometer;
    private ObjectAnimator animator;

    private long targetTime;
    private long startTime;
    private long elapsedTime;

    private Handler handler = new Handler();

    private Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            TimerFragment.State state = getState();
            if (state == TimerFragment.State.RUNNING) {
                elapsedTime = SystemClock.elapsedRealtime() - startTime;
                setProgressBar();
                setTimeView(chronometer, elapsedTime);
                if (elapsedTime > targetTime) {
                    pauseChronometer();
                }

                handler.postDelayed(this, 1000);
            } else if (state == TimerFragment.State.PAUSED) {
                if (chronometer.getText().equals("")) {
                    setTimeView(chronometer, elapsedTime);
                } else {
                    chronometer.setText("");
                }
                handler.postDelayed(this, 500);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chronometer, container, false);
        timeView = (TextView) view.findViewById(R.id.text_view_chronometer_time);
        progressBar = (HoloCircularProgressBar) view
                .findViewById(R.id.circular_progress_bar_chronometer);
        chronometer = (TextView) view.findViewById(R.id.text_view_chronometer);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        setTimeView(timeView, targetTime);
        TimerFragment.State state = getState();
        if (state == TimerFragment.State.UNSTARTED || state == TimerFragment.State.RUNNING) {
            startChronometer();
        } else {
            setProgressBar();
            setTimeView(chronometer, elapsedTime);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    public void saveData() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(START_TIME, startTime);
        editor.putLong(ELAPSED_TIME, elapsedTime);
        editor.commit();
    }

    private void loadData() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        String savedTime = preferences.getString(TimerFragment.TIME, "00000");
        int hour = Integer.valueOf(savedTime.substring(0, 1));
        int minute = Integer.valueOf(savedTime.substring(1, 3));
        int second = Integer.valueOf(savedTime.substring(3));
        targetTime = ((((hour * 60) + minute) * 60) + second) * 1000;
        startTime = preferences.getLong(START_TIME, 0);
        elapsedTime = preferences.getLong(ELAPSED_TIME, 0);
    }

    private void setTimeView(TextView textView, long time) {
        int hour = (int) (time / 3600000);
        time %= 3600000;
        int minute = (int) (time / 60000);
        time %= 60000;
        int second = (int) (time / 1000);

        if (hour == 0) {
            textView.setText(String.format("%d:%02d", minute, second));
        } else {
            textView.setText(String.format("%d:%02d:%02d", hour, minute, second));
        }
    }

    private void setProgressBar() {
        if (animator != null) {
            animator.cancel();
        }
        float progress = (float) elapsedTime / targetTime;
        progress = progress > 1 ? 1f : progress;
        animate(progressBar, null, progress, 1000);
        progressBar.setMarkerProgress(0);
    }

    private void animate(final HoloCircularProgressBar progressBar,
                         final Animator.AnimatorListener listener,
                         final float progress, final int duration) {
        animator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
        animator.setDuration(duration);

        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(final Animator animation) {
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                progressBar.setProgress(progress);
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

            @Override
            public void onAnimationStart(final Animator animation) {
            }
        });
        if (listener != null) {
            animator.addListener(listener);
        }
        animator.reverse();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                progressBar.setProgress((Float) animation.getAnimatedValue());
            }
        });
        progressBar.setMarkerProgress(progress);
        animator.start();
    }

    public TimerFragment.State getState() {
        TimerFragment.State state = null;
        if (startTime == 0) {
            if (elapsedTime == 0) {
                state = TimerFragment.State.UNSTARTED;
            } else {
                state = TimerFragment.State.PAUSED;
            }
        } else {
            state = TimerFragment.State.RUNNING;
        }
        return state;
    }

    public void startChronometer() {
        if (getState() == TimerFragment.State.UNSTARTED) {
            startTime = SystemClock.elapsedRealtime();
        } else if (getState() == TimerFragment.State.PAUSED) {
            startTime = SystemClock.elapsedRealtime() - elapsedTime;
        }
        handler.removeCallbacks(timerTask);
        handler.post(timerTask);
    }

    public void pauseChronometer() {
        startTime = 0;
    }

    public void resetChronometer() {
        TimerFragment.State state = getState();
        startTime = 0;
        elapsedTime = 0;
        if (state == TimerFragment.State.PAUSED) {
            setProgressBar();
            setTimeView(chronometer, elapsedTime);
        } else if (state == TimerFragment.State.RUNNING) {
            startChronometer();
        }
    }

    public void deleteChronometer() {
        handler.removeCallbacks(timerTask);
        startTime = 0;
        elapsedTime = 0;
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TimerFragment.TIME, "00000");
        editor.commit();
    }
}
