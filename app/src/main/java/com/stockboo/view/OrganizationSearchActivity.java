package com.stockboo.view;

import com.google.gson.Gson;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.stockboo.model.Stock;
import com.stockboo.model.StockList;
import com.stockboo.model.db.DatabaseHelper;
import com.stockboo.view.util.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.stockboo.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class OrganizationSearchActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_organization_search);
        getActionBar().hide();
        StringBuffer buffer = new StringBuffer();
        try {
            InputStream is = getAssets().open("stocklist.json");
            byte[] data = new byte[1024];
            int bytesRead = 0;
            while((bytesRead = is.read(data)) > 0){
                buffer.append(new String(data, 0, bytesRead));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        dbHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        String jsonStr = buffer.toString();
        StockList[] startUpData = new Gson().fromJson(jsonStr, StockList[].class);

        RuntimeExceptionDao<StockList, Integer> stockListDao = dbHelper.getStockListDao();
        int length = startUpData.length;
        for(int i = 0;i<length; i++){
            stockListDao.create(startUpData[i]);
        }
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String SqlQuery = "SELECT * FROM StockList";
        Cursor cursor = database.rawQuery(SqlQuery, null);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new StockListAdapter(this, cursor, true));
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private class StockListAdapter extends CursorAdapter {


        public StockListAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            TextView view = new TextView(context);
            view.setText(cursor.getString(1));
            view.setTextColor(getResources().getColor(android.R.color.black));
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView)view).setText(cursor.getString(1));
            ((TextView)view).setTextColor(getResources().getColor(android.R.color.black));
        }
    }


}
