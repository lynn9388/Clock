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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmActivity extends AppCompatActivity {
    private KenBurnsView background;
    private TextView dateView;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        background = (KenBurnsView) findViewById(R.id.ken_burns_alarm_background);
        dateView = (TextView) findViewById(R.id.text_view_alarm_date);
        cancelButton = (Button) findViewById(R.id.button_alarm);

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String name = "time_" + String.valueOf(hour / 2 * 2);
        int resourceId = getResources().getIdentifier(name, "drawable", getPackageName());

        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(configuration);
        String imageUri = "drawable://" + resourceId;
        ImageLoader.getInstance().displayImage(imageUri, background);


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMMM d");
        final Date date = new Date();
        dateView.setText(simpleDateFormat.format(date));

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
