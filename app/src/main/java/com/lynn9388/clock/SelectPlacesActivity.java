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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.lynn9388.clock.util.Clock;
import com.lynn9388.clock.util.DatabaseHelper;

import java.util.TimeZone;

public class SelectPlacesActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_select_places);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_select_places);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        PlacesAdapter adapter = new PlacesAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {
        private Cursor cursor;

        public PlacesAdapter() {
            cursor = Clock.ClockEntry.queryAll(databaseHelper);
            if (cursor.getCount() == 0) {
                String[] places = TimeZone.getAvailableIDs();
                for (String place : places) {
                    Clock.ClockEntry.insert(databaseHelper, place);
                }
                cursor = Clock.ClockEntry.queryAll(databaseHelper);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_place, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            cursor.moveToPosition(position);
            holder.place.setText(cursor.getString(cursor.getColumnIndex(Clock.ClockEntry.COLUMN_NAME_PLACE)));
            holder.place.setChecked(cursor.getInt(cursor.getColumnIndex(Clock.ClockEntry.COLUMN_NAME_CHECKED)) == 1 ? true : false);

            holder.place.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v;
                    String place = checkBox.getText().toString();
                    boolean checked = checkBox.isChecked();
                    Clock.ClockEntry.update(databaseHelper, place, checked);
                }
            });
        }

        @Override
        public int getItemCount() {
            return cursor.getCount();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CheckBox place;

            public ViewHolder(View itemView) {
                super(itemView);
                place = (CheckBox) itemView.findViewById(R.id.check_box_place);
            }
        }
    }

}
