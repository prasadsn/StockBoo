package com.stockboo.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.stockboo.R;
import com.stockboo.model.StockList;
import com.stockboo.model.db.DatabaseHelper;
import com.stockboo.model.Portfolio;

public class AddStockActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseHelper dbHelper;
    private EditText quantityEditText;
    private EditText priceEditText;
    private StockList mStockList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);
        findViewById(R.id.button).setOnClickListener(this);
        quantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
        priceEditText = (EditText) findViewById(R.id.price_edit_text);
        dbHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_stock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK)
            return;
        mStockList = data.getParcelableExtra("Stock");
        LinearLayout layout = (LinearLayout) findViewById(R.id.stock_selection_button);
        ((TextView) layout.getChildAt(1)).setText(mStockList.getScriptName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.stock_selection_button:
                Intent intent = new Intent(this, StockListSearchActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.button:
                if(quantityEditText.getText().toString().isEmpty() || quantityEditText.getText().equals("0"))
                    Toast.makeText(this, "Please enter proper Quantity", Toast.LENGTH_LONG).show();
                else if(priceEditText.getText().toString().isEmpty() || priceEditText.getText().equals("0"))
                    Toast.makeText(this, "Please enter proper Price", Toast.LENGTH_LONG).show();
                else if(mStockList == null)
                    Toast.makeText(this, "Please select Stock", Toast.LENGTH_LONG).show();
                else {
                    Portfolio portfolio = new Portfolio(mStockList, Integer.parseInt(quantityEditText.getText().toString()), Integer.parseInt(priceEditText.getText().toString()));
                    dbHelper.getPortfolioRuntimeDao().create(portfolio);
                    finish();
                }
                break;
        }
    }
}
