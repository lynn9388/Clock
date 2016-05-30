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

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TimePicker;

import com.lynn9388.clock.Fragments.AlarmFragment;
import com.lynn9388.clock.Fragments.ChronometerFragment;
import com.lynn9388.clock.Fragments.ClockFragment;
import com.lynn9388.clock.Fragments.StopwatchFragment;
import com.lynn9388.clock.Fragments.TimerFragment;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener,
        View.OnClickListener {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ClockPagerAdapter adapter;
    private ImageButton leftButton;
    private ImageButton rightButton;
    private FloatingActionButton fab;
    private int lastFabImageResource;
    private int currentFabImageResource;
    private TimerFragment.State timerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new ClockPagerAdapter(getSupportFragmentManager());
        leftButton = (ImageButton) findViewById(R.id.left_button);
        rightButton = (ImageButton) findViewById(R.id.right_button);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_alarm)
                .setContentDescription(R.string.tab_alarm);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_clock)
                .setContentDescription(R.string.tab_clock);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_stopwatch)
                .setContentDescription(R.string.tab_stopwatch);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_timer)
                .setContentDescription(R.string.tab_timer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        long startTime = preferences.getLong(ChronometerFragment.START_TIME, 0);
        long elapsedTime = preferences.getLong(ChronometerFragment.ELAPSED_TIME, 0);
        if (startTime == 0) {
            if (elapsedTime == 0) {
                timerState = TimerFragment.State.UNSTARTED;
            } else {
                timerState = TimerFragment.State.PAUSED;
            }
        } else {
            timerState = TimerFragment.State.RUNNING;
        }
        setImageResource(viewPager.getCurrentItem());
        fab.setOnClickListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        final int position = tab.getPosition();
        viewPager.setCurrentItem(position);
        setImageResource(position);
        setFabAnimation();
        setImageButtonOnClickListener();
        fab.setOnClickListener(this);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        lastFabImageResource = currentFabImageResource;
        if (tab.getPosition() == 3) {
            if (timerState == TimerFragment.State.UNSTARTED) {
                TimerFragment fragment = (TimerFragment) findFragmentByPosition(3);
                fragment.saveData();
            } else {
                ChronometerFragment fragment = (ChronometerFragment) findFragmentByPosition(3);
                fragment.saveData();
            }
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onClick(View v) {
        int position = tabLayout.getSelectedTabPosition();
        if (position == 0) {
            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            AlarmFragment fragment = (AlarmFragment) findFragmentByPosition(0);
                            fragment.addAlarm(hourOfDay, minute);
                        }
                    }, hourOfDay, minute, true);
            timePickerDialog.show();
        } else if (position == 1) {
            Intent intent = new Intent(MainActivity.this, SelectPlacesActivity.class);
            startActivity(intent);
        } else if (position == 2) {
            StopwatchFragment fragment = (StopwatchFragment) findFragmentByPosition(2);
            StopwatchFragment.State state = fragment.getState();
            if (state == StopwatchFragment.State.UNSTARTED
                    || state == StopwatchFragment.State.PAUSED) {
                fragment.startStopwatch();
                leftButton.setImageResource(R.drawable.ic_lap);
                leftButton.setVisibility(ImageButton.VISIBLE);
                rightButton.setVisibility(ImageButton.INVISIBLE);
                lastFabImageResource = currentFabImageResource;
                currentFabImageResource = R.drawable.ic_pause;
            } else if (state == StopwatchFragment.State.RUNNING) {
                fragment.pauseStopwatch();
                leftButton.setImageResource(R.drawable.ic_reset);
                leftButton.setVisibility(ImageButton.VISIBLE);
                rightButton.setImageResource(R.drawable.ic_share);
                rightButton.setVisibility(ImageButton.VISIBLE);
                lastFabImageResource = currentFabImageResource;
                currentFabImageResource = R.drawable.ic_start;
            }
            setFabAnimation();
            setImageButtonOnClickListener();
        } else if (position == 3) {
            if (timerState == TimerFragment.State.UNSTARTED) {
                timerState = TimerFragment.State.RUNNING;
                getSupportFragmentManager().beginTransaction()
                        .remove(findFragmentByPosition(3))
                        .commit();
                adapter.notifyDataSetChanged();
                lastFabImageResource = currentFabImageResource;
                currentFabImageResource = R.drawable.ic_pause;
            } else if (timerState == TimerFragment.State.PAUSED) {
                timerState = TimerFragment.State.RUNNING;
                ((ChronometerFragment) findFragmentByPosition(3)).startChronometer();
                lastFabImageResource = currentFabImageResource;
                currentFabImageResource = R.drawable.ic_pause;
            } else if (timerState == TimerFragment.State.RUNNING) {
                timerState = TimerFragment.State.PAUSED;
                ((ChronometerFragment) findFragmentByPosition(3)).pauseChronometer();
                lastFabImageResource = currentFabImageResource;
                currentFabImageResource = R.drawable.ic_start;
            }
            leftButton.setImageResource(R.drawable.ic_reset);
            leftButton.setVisibility(ImageButton.VISIBLE);
            rightButton.setImageResource(R.drawable.ic_delete);
            rightButton.setVisibility(ImageButton.VISIBLE);
            setFabAnimation();
            setImageButtonOnClickListener();
        }
    }

    private Fragment findFragmentByPosition(int position) {
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + viewPager.getId() + ":" + adapter.getItemId(position));
    }

    private void setImageResource(int position) {
        currentFabImageResource = -1;
        if (position == 0) {
            leftButton.setVisibility(ImageButton.INVISIBLE);
            rightButton.setVisibility(ImageButton.INVISIBLE);
            currentFabImageResource = R.drawable.ic_add;
        } else if (position == 1) {
            leftButton.setVisibility(ImageButton.INVISIBLE);
            rightButton.setVisibility(ImageButton.INVISIBLE);
            currentFabImageResource = R.drawable.ic_place;
        } else if (position == 2) {
            StopwatchFragment fragment = (StopwatchFragment) findFragmentByPosition(2);
            StopwatchFragment.State state = fragment.getState();
            if (state == StopwatchFragment.State.UNSTARTED) {
                leftButton.setVisibility(ImageButton.INVISIBLE);
                rightButton.setVisibility(ImageButton.INVISIBLE);
                currentFabImageResource = R.drawable.ic_start;
            } else if (state == StopwatchFragment.State.PAUSED) {
                leftButton.setImageResource(R.drawable.ic_reset);
                leftButton.setVisibility(ImageButton.VISIBLE);
                rightButton.setImageResource(R.drawable.ic_share);
                rightButton.setVisibility(ImageButton.VISIBLE);
                currentFabImageResource = R.drawable.ic_start;
            } else if (state == StopwatchFragment.State.RUNNING) {
                leftButton.setImageResource(R.drawable.ic_lap);
                leftButton.setVisibility(ImageButton.VISIBLE);
                rightButton.setVisibility(ImageButton.INVISIBLE);
                currentFabImageResource = R.drawable.ic_pause;
            }
        } else if (position == 3) {
            if (timerState == TimerFragment.State.UNSTARTED) {
                leftButton.setVisibility(ImageButton.INVISIBLE);
                rightButton.setVisibility(ImageButton.INVISIBLE);
                SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
                String time = preferences.getString(TimerFragment.TIME, "00000");
                if (!time.equals("00000")) {
                    currentFabImageResource = R.drawable.ic_start;
                }
            } else if (timerState == TimerFragment.State.PAUSED) {
                leftButton.setImageResource(R.drawable.ic_reset);
                leftButton.setVisibility(ImageButton.VISIBLE);
                rightButton.setImageResource(R.drawable.ic_delete);
                rightButton.setVisibility(ImageButton.VISIBLE);
                currentFabImageResource = R.drawable.ic_start;
            } else if (timerState == TimerFragment.State.RUNNING) {
                leftButton.setImageResource(R.drawable.ic_reset);
                leftButton.setVisibility(ImageButton.VISIBLE);
                rightButton.setImageResource(R.drawable.ic_delete);
                rightButton.setVisibility(ImageButton.VISIBLE);
                currentFabImageResource = R.drawable.ic_pause;
            }
        }
    }

    private void setFabAnimation() {
        if (lastFabImageResource >= 0) {
            if (currentFabImageResource > 0) {
                if (lastFabImageResource != currentFabImageResource) {
                    if (fab.getVisibility() == FloatingActionButton.VISIBLE) {
                        fab.clearAnimation();
                        Animation outAnimation = AnimationUtils
                                .loadAnimation(this, R.anim.scale_fab_out);
                        outAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                fab.setImageResource(currentFabImageResource);
                                Animation inAnimation = AnimationUtils
                                        .loadAnimation(MainActivity.this, R.anim.scale_fab_in);
                                fab.startAnimation(inAnimation);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        fab.startAnimation(outAnimation);

                    } else {
                        fab.setImageResource(currentFabImageResource);
                        fab.show();
                    }
                }
            } else {
                fab.hide();
            }
        } else {
            fab.setImageResource(currentFabImageResource);
            fab.show();
        }
    }

    private void setImageButtonOnClickListener() {
        ImageButtonOnClickListener imageButtonOnClickListener = new ImageButtonOnClickListener();
        leftButton.setOnClickListener(imageButtonOnClickListener);
        rightButton.setOnClickListener(imageButtonOnClickListener);
    }

    private class ImageButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int position = tabLayout.getSelectedTabPosition();
            if (position == 2) {
                StopwatchFragment fragment = (StopwatchFragment) findFragmentByPosition(2);
                StopwatchFragment.State state = fragment.getState();
                if (state == StopwatchFragment.State.PAUSED) {
                    if (v.getId() == leftButton.getId()) {
                        fragment.resetStopwatch();
                        fab.show();
                    } else if (v.getId() == rightButton.getId()) {
                        fragment.shareRecords();
                    }
                } else if (state == StopwatchFragment.State.RUNNING) {
                    if (v.getId() == leftButton.getId()) {
                        fragment.addRecord();
                    }
                }
            } else if (position == 3) {
                if (v.getId() == leftButton.getId()) {
                    ((ChronometerFragment) findFragmentByPosition(3)).resetChronometer();
                } else if (v.getId() == rightButton.getId()) {
                    timerState = TimerFragment.State.UNSTARTED;
                    ((ChronometerFragment) findFragmentByPosition(3)).deleteChronometer();
                    getSupportFragmentManager().beginTransaction()
                            .remove(findFragmentByPosition(3))
                            .commit();
                    adapter.notifyDataSetChanged();
                    lastFabImageResource = currentFabImageResource;
                    setImageResource(3);
                    setFabAnimation();
                }
            }
        }
    }

    private class ClockPagerAdapter extends FragmentPagerAdapter {

        public ClockPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment = new AlarmFragment();
            } else if (position == 1) {
                fragment = new ClockFragment();
            } else if (position == 2) {
                fragment = new StopwatchFragment();
            } else if (position == 3) {
                if (timerState == TimerFragment.State.UNSTARTED) {
                    fragment = new TimerFragment();
                } else {
                    fragment = new ChronometerFragment();
                }
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}