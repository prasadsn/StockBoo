package com.stockboo.application;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.parse.Parse;
import com.stockboo.scheduler.SampleAlarmReceiver;

/**
 * Created by prsn0001 on 10/25/2015.
 */
public class StockBooApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "CJtGKzUduXCdZye1VLd9J0HZT7KfwXyMlJMBmR2I", "f0q6rKX9FLOfgfm4zAx7pwzhjWk3T245dDod002i");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(preferences.getBoolean("enable_notification", true)) {
            if(preferences.getBoolean("alarm_set", false))
                return;
            new SampleAlarmReceiver().setAlarm(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("alarm_set", true);
            editor.commit();
            editor.apply();
        }
        else
            new SampleAlarmReceiver().cancelAlarm(this);
    }
}
