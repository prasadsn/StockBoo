package com.stockboo.view;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.stockboo.R;

import java.util.ArrayList;

public class StockDetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ArrayList<String> list = getIntent().getStringArrayListExtra("param");
        getFragmentManager().beginTransaction()
                .replace(R.id.container, StockDetailFragment.newInstance(list))
                .commit();
    }
}
