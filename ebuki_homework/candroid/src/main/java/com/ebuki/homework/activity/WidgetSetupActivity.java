/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ebuki.homework.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.CompoundButton;

import com.ebuki.homework.R;
import com.ebuki.homework.util.ApplicationManager;
import com.ebuki.homework.widget.CanvasWidgetProvider;

public class WidgetSetupActivity extends AppCompatActivity {

    public static final String WIDGET_BACKGROUND_COLOR_KEY = "widgetBackgroundColorKey";
    public static final String WIDGET_BACKGROUND_COLOR_LIGHT = "widgetBackgroundColorLight";
    public static final String WIDGET_BACKGROUND_COLOR_DARK = "widgetBackgroundColorDark";
    public static final String WIDGET_BACKGROUND_PREFIX = "widgetBackground__";
    public static final String WIDGET_DETAILS_PREFIX = "widgetDetails__";

    protected CardView mCardDark, mCardLight;
    protected AppCompatCheckBox mCheckBox;

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets the result canceled so if the user decides not to setup the widget it does not get added
        setResult(Activity.RESULT_CANCELED);
        setContentView(R.layout.widget_setup_activity);

        mCardDark = (CardView)findViewById(R.id.cardDark);
        mCardLight = (CardView)findViewById(R.id.cardLight);
        mCardDark.setOnClickListener(mCardClickListener);
        mCardLight.setOnClickListener(mCardClickListener);

        mCheckBox = (AppCompatCheckBox)findViewById(R.id.check_box);
        mCheckBox.setOnCheckedChangeListener(mWidgetDetailsCheckChangeListener);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            //get the widget id we are adding/updating
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        //If our appWidgetId is bad or does not exist we exit the activity
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    private View.OnClickListener mCardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            switch (v.getId()) {
                case R.id.cardDark: {
                    resultValue.putExtra(WIDGET_BACKGROUND_COLOR_KEY, WIDGET_BACKGROUND_COLOR_DARK);
                    ApplicationManager.getPrefs(getApplicationContext()).save(WIDGET_BACKGROUND_PREFIX + appWidgetId, WIDGET_BACKGROUND_COLOR_DARK);
                    break;
                }
                case R.id.cardLight: {
                    resultValue.putExtra(WIDGET_BACKGROUND_COLOR_KEY, WIDGET_BACKGROUND_COLOR_LIGHT);
                    ApplicationManager.getPrefs(getApplicationContext()).save(WIDGET_BACKGROUND_PREFIX + appWidgetId, WIDGET_BACKGROUND_COLOR_LIGHT);
                    break;
                }
            }
            setResult(RESULT_OK, resultValue);
            finish();

            sendBroadcast(new Intent(CanvasWidgetProvider.REFRESH_ALL));
        }
    };

    private CompoundButton.OnCheckedChangeListener mWidgetDetailsCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ApplicationManager.getPrefs(getApplicationContext()).save(WIDGET_DETAILS_PREFIX + appWidgetId, isChecked);
        }
    };
}
