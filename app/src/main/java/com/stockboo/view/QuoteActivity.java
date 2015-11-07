package com.stockboo.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.stockboo.R;
import com.stockboo.model.StockList;
import com.stockboo.model.WatchList;
import com.stockboo.network.StockBooRequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuoteActivity extends AppCompatActivity {

    private WatchList mWatchList;
    private NewsAdapter newsAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("B1166B5E58D7D8172322BE3B3D50EC00")
                .build();
        mAdView.loadAd(adRequest);
        Intent intent = new Intent(this, StockListSearchActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mListView = (ListView) findViewById(R.id.listView2);
        LinearLayout layout = (LinearLayout) findViewById(R.id.heading_quote);
        ((TextView)layout.getChildAt(1)).setText("Show Quote");
        layout = (LinearLayout) findViewById(R.id.heading_market_news);
        ((TextView)layout.getChildAt(1)).setText("Market News");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        StockList stockList = data.getParcelableExtra("Stock");
        mWatchList = new WatchList(stockList.getSYMBOL(), stockList.getScriptName(), stockList.getStatus(), stockList.getISINNO(), stockList.getIndustry(), stockList.getGroup(), null, stockList.getScriptID(), null, null);
        StringBuffer reqParamBuffer = new StringBuffer();
            if(mWatchList.getGroup() == null)
                return;
            reqParamBuffer.append(mWatchList.getGroup().equals("A") ? "NSE:" + mWatchList.getScriptID() : "BOM:" + mWatchList.getScriptID());
        String strUrl = "https://www.google.com/finance/company_news?q=" + mWatchList.getScriptID() + "&output=json";
        MarketNewsListener listener = new MarketNewsListener();
        StringRequest bseNseRequest = new StringRequest(StringRequest.Method.GET, strUrl, listener, listener);
        StockBooRequestQueue.getInstance(this).getRequestQueue().add(bseNseRequest);
        loadData(reqParamBuffer.toString());
    }

    public void updateView(Context context){
        LinearLayout layout = (LinearLayout) findViewById(R.id.include3);
        TextView stockNameTv = (TextView) layout.findViewById(R.id.scriptName);
        TextView stockIdTv = (TextView) layout.findViewById(R.id.scriptID);
        TextView cTv = (TextView) layout.findViewById(R.id.c);
        TextView priceTv = (TextView) layout.findViewById(R.id.price);
        TextView cpFixTv = (TextView) layout.findViewById(R.id.c_fix);

        stockNameTv.setText(mWatchList.getScriptName());
        stockIdTv.setText(mWatchList.getScriptID());
        priceTv.setText(mWatchList.getPrice());
        Double c = null;
        Double cp_fix = null;
        if(mWatchList.getC()!=null && !mWatchList.getC().isEmpty()){
            c = new Double(mWatchList.getC());
            if(c!=null && c.doubleValue() < 0)
                cTv.setBackgroundResource(R.drawable.oval_background_red);
            else
                cTv.setBackgroundResource(R.drawable.oval_background_green);
            cTv.setText(c.toString());
        } else
            cTv.setVisibility(View.INVISIBLE);
        if(mWatchList.getC_fix()!=null && !mWatchList.getC_fix().isEmpty()) {
            cp_fix = new Double(mWatchList.getC_fix());
            if (cp_fix != null && cp_fix.doubleValue() < 0)
                cpFixTv.setBackgroundResource(R.drawable.oval_background_red);
            else
                cpFixTv.setBackgroundResource(R.drawable.oval_background_green);
            cpFixTv.setText(mWatchList.getC_fix() + " %");
        } else
            cpFixTv.setVisibility(View.INVISIBLE);
    }

    private void loadData(String reqParamBuffer){
        Listener listener = new Listener();
        String request = "http://finance.google.com/finance/info?client=ig&q=" + reqParamBuffer;
        StringRequest bseNseRequest = new StringRequest(StringRequest.Method.GET, request, listener, listener);
        StockBooRequestQueue.getInstance(this).getRequestQueue().add(bseNseRequest);
    }

    private class MarketNewsListener implements Response.Listener<String>, Response.ErrorListener{

        @Override
        public void onErrorResponse(VolleyError error) {

        }

        @Override
        public void onResponse(String response) {
            ArrayList<String> newsTitleList = new ArrayList<>();
            ArrayList<String> newsLinkList = new ArrayList<>();
            try {
                JSONObject object = new JSONObject(response);
                JSONArray array = object.getJSONArray("clusters");
                for(int i = 0; i<array.length(); i++){
                    JSONObject object1 = array.getJSONObject(i);
                    JSONArray array1 = object1.getJSONArray("a");
                    JSONObject object2 = array1.getJSONObject(0);
                    newsTitleList.add((String) object2.get("t"));
                    newsLinkList.add((String) object2.get("u"));
                    Log.d(StockDetailFragment.class.getName(), array1.length() + "");
                }
                Log.d(StockDetailFragment.class.getName(), array.length() + "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            newsAdapter = new NewsAdapter(newsTitleList, newsLinkList);
            mListView.setAdapter(newsAdapter);
        }
    }

    private class Listener implements Response.Listener<String>, Response.ErrorListener{

        public Listener(){
        }
        @Override
        public void onErrorResponse(VolleyError error) {

        }

        @Override
        public void onResponse(String response) {
            try {
                if(response.startsWith("\n// "))
                    response = response.substring(4);
                JSONArray array = new JSONArray(response);
                //List<WatchList> list = dbHelper.getWatchListRuntimeDao().queryForAll();
                JSONObject jsonObject = array.getJSONObject(0);
                String cValue = jsonObject.getString("c_fix");
                String c_fixValue = jsonObject.getString("cp_fix");
                String price = jsonObject.getString("l_cur");
                mWatchList.setC(cValue);
                mWatchList.setC_fix(c_fixValue);
                mWatchList.setPrice(price);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            updateView(getApplicationContext());
        }
    }

    private class NewsAdapter extends BaseAdapter {

        private ArrayList<String> mList;
        private ArrayList<String> mNewsLinkList;

        private NewsAdapter(ArrayList<String> list, ArrayList<String> newsLinkList){
            mList = list;
            mNewsLinkList = newsLinkList;
        }
        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.headlines_tv, null);
            TextView tv = (TextView) layout.getChildAt(1);
            tv.setText(mList.get(position));
            if(mNewsLinkList != null)
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(QuoteActivity.this, MarketNewsActivity.class);
                        intent.putExtra("link", mNewsLinkList.get(position));
                        startActivity(intent);
                    }
                });
            return layout;
        }
    }

}
