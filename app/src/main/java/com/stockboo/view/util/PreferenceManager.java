package com.stockboo.view.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by narpr05 on 7/9/2015.
 */
public class PreferenceManager {

    public static void setDemoWatched(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PreferenceManager.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("demo_watched", true);
        editor.apply();
    }

    public static boolean isDemoWatched(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PreferenceManager.class.getName(), Context.MODE_PRIVATE);
        return preferences.getBoolean("demo_watched", false);
    }
}
