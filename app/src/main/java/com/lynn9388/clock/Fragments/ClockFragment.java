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


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;

import com.lynn9388.clock.R;
import com.lynn9388.clock.util.Clock;
import com.lynn9388.clock.util.DatabaseHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClockFragment extends Fragment {
    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ClockAdapter clockAdapter;
    private Cursor cursor;

    public ClockFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        databaseHelper = new DatabaseHelper(getActivity());

        View view = inflater.inflate(R.layout.fragment_clock, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_clock);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        clockAdapter = new ClockAdapter();
        recyclerView.setAdapter(clockAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cursor = Clock.ClockEntry.queryChecked(databaseHelper);
        clockAdapter.notifyDataSetChanged();
    }

    public class ClockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int LOCAL_CLOCK_VIEW_HOLDER_TYPE = 1;
        private static final int CLOCK_VIEW_HOLDER_TYPE = 2;

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? LOCAL_CLOCK_VIEW_HOLDER_TYPE : CLOCK_VIEW_HOLDER_TYPE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            RecyclerView.ViewHolder viewHolder = null;
            if (viewType == LOCAL_CLOCK_VIEW_HOLDER_TYPE) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_clock_local, parent, false);
                viewHolder = new LocalClockViewHolder(view);
            } else if (viewType == CLOCK_VIEW_HOLDER_TYPE) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_clock, parent, false);
                viewHolder = new ClockViewHolder(view);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMMM d");
            final Date date = new Date();
            if (position == 0) {
                final LocalClockViewHolder viewHolder = (LocalClockViewHolder) holder;
                viewHolder.localTime.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        viewHolder.localDate.setText(simpleDateFormat.format(date));
                    }
                });
                viewHolder.localDate.setText(simpleDateFormat.format(date));
            } else {
                cursor.moveToPosition(position - 1);
                final ClockViewHolder viewHolder = (ClockViewHolder) holder;
                String place = cursor
                        .getString(cursor.getColumnIndex(Clock.ClockEntry.COLUMN_NAME_PLACE));
                String[] places = place.split("/");
                viewHolder.place.setText(places.length == 2 ? places[1] : places[0]);

                DateFormat dateFormat = DateFormat.getDateInstance();
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(place));
                viewHolder.date.setText(simpleDateFormat.format(date));

                viewHolder.time.setTimeZone(place);
                viewHolder.time.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        viewHolder.date.setText(simpleDateFormat.format(date));
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return cursor.getCount() + 1;
        }

        public class LocalClockViewHolder extends RecyclerView.ViewHolder {
            public TextClock localTime;
            public TextView localDate;

            public LocalClockViewHolder(View itemView) {
                super(itemView);
                localTime = (TextClock) itemView.findViewById(R.id.text_clock_clock_local_time);
                localDate = (TextView) itemView.findViewById(R.id.text_view_clock_local_date);
            }
        }

        public class ClockViewHolder extends RecyclerView.ViewHolder {
            public TextView place;
            public TextView date;
            public TextClock time;

            public ClockViewHolder(View itemView) {
                super(itemView);
                place = (TextView) itemView.findViewById(R.id.text_view_clock_place);
                date = (TextView) itemView.findViewById(R.id.text_view_clock_date);
                time = (TextClock) itemView.findViewById(R.id.text_clock_clock_time);
            }
        }
    }
}
