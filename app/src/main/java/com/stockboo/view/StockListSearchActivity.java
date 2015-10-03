package com.stockboo.view;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.stockboo.model.StockList;
import com.stockboo.model.WatchList;
import com.stockboo.model.db.DatabaseHelper;
import com.stockboo.view.util.util.SystemUiHider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.stockboo.R;

import java.sql.SQLException;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class StockListSearchActivity extends Activity implements TextWatcher, AdapterView.OnItemClickListener{
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

    private StockListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stocklist_search);
        getActionBar().hide();
        EditText searchEditText = (EditText) findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(this);
        dbHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String SqlQuery = "SELECT * FROM StockList";
        Cursor cursor = database.rawQuery(SqlQuery, null);
        ListView listView = (ListView) findViewById(R.id.listView);
        adapter = new StockListAdapter(this, cursor, true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String query = null;
        query = "SELECT * FROM StockList where ScriptName like '" + s.toString() + "%'";
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String symbol = (String) view.getTag();
        QueryBuilder<StockList, Integer> queryBuilder =
                dbHelper.getStockListDao().queryBuilder();
        // list all of the accounts that have the same
        // name and password field
        try {
            queryBuilder.where().eq("SYMBOL",
                    symbol);
            List<StockList> results = queryBuilder.query();
            StockList stockList = results.get(0);
            Intent data = getIntent();
            data.putExtra("Stock", stockList);
            setResult(RESULT_OK, data);
            finish();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class StockListAdapter extends CursorAdapter {

        public StockListAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.stock_search_item, null);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            loadData(view, context, cursor);
            view.setTag(cursor.getString(1));
        }

        private void loadData(View view, Context context, Cursor cursor){
            LinearLayout layout = (LinearLayout) view;
                    ((TextView) ((LinearLayout) view).getChildAt(0)).setText(cursor.getString(2));
            ((TextView) ((LinearLayout) view).getChildAt(1)).setText(cursor.getString(7));
        }
    }


}
