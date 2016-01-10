package com.stockboo.view;

import android.app.DialogFragment;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.toolbox.StringRequest;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.stockboo.R;
import com.stockboo.model.StockListIntentService;
import com.stockboo.network.StockBooRequestQueue;
import com.stockboo.view.util.PreferenceManager;
import com.stockboo.view.util.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_LENGHT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intentService = new Intent(this, StockListIntentService.class);
        startService(intentService);
        getSupportActionBar().hide();
        if(Utilities.checkInternetConnection(this)) {
            updateStockMessages();
            new Handler().postDelayed(new Runnable() {

                public void run() {
                    if (!PreferenceManager.isDemoWatched(SplashActivity.this))
                        launchTutorial();
                    else {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }, SPLASH_DISPLAY_LENGHT);
        } else{
            DialogFragment newFragment = AlertFragment.newInstance(
                    R.string.alert_dialog_no_internet);
            newFragment.show(getFragmentManager(), "dialog");
        }
        initApp();
    }

    private void initApp(){
        SharedPreferences preferences = getSharedPreferences("NSE_BSE_DATA", MODE_PRIVATE);
        preferences.edit().putString("NSE_BSE_DATA", null).commit();
    }

    private void launchTutorial(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, TutorialFragment.newInstance())
                .commit();

    }

    private void updateStockMessages(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Messages");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                if (e == null) {
                    JSONArray jsonArray = new JSONArray();
                    for (ParseObject parseObject : objects) {
                        JSONObject object = new JSONObject();
                        ParseObject stockObject = (ParseObject) parseObject.get("stock");
                        String msg = parseObject.get("content").toString();
                        try {
                            object.put("content", msg);
                            if(stockObject.getObjectId() !=null )
                                object.put("stock", stockObject.getObjectId());
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        jsonArray.put(object);
                    }

                    SharedPreferences preferences = getSharedPreferences("stock_messages", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.putString("stock_messages", jsonArray.toString());
                    editor.commit();
                } else {
                }
            }
        });

    }
}
