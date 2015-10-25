package com.stockboo.application;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by prsn0001 on 10/25/2015.
 */
public class StockBooApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "CJtGKzUduXCdZye1VLd9J0HZT7KfwXyMlJMBmR2I", "f0q6rKX9FLOfgfm4zAx7pwzhjWk3T245dDod002i");
    }
}
