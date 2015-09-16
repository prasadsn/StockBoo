package com.stockboo.view;

import android.app.DialogFragment;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.Parse;
import com.stockboo.R;
import com.stockboo.model.StockListIntentService;
import com.stockboo.view.util.PreferenceManager;

public class SplashActivity extends ActionBarActivity {

    private static final long SPLASH_DISPLAY_LENGHT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intentService = new Intent(this, StockListIntentService.class);
        startService(intentService);
        getSupportActionBar().hide();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "CJtGKzUduXCdZye1VLd9J0HZT7KfwXyMlJMBmR2I", "f0q6rKX9FLOfgfm4zAx7pwzhjWk3T245dDod002i");
        if(checkInternetConnection())
        new Handler().postDelayed(new Runnable() {

            public void run() {
                if(!PreferenceManager.isDemoWatched(SplashActivity.this))
                launchTutorial();
                else{
                    Intent intent = new Intent(SplashActivity.this ,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_DISPLAY_LENGHT);
        else{
            DialogFragment newFragment = AlertFragment.newInstance(
                    R.string.alert_dialog_no_internet);
            newFragment.show(getFragmentManager(), "dialog");
        }
    }

    private boolean checkInternetConnection() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        // ARE WE CONNECTED TO THE NET
        if (conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {

            return true;
       } else {
            return false;

        }
    }

    private void launchTutorial(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, TutorialFragment.newInstance())
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
