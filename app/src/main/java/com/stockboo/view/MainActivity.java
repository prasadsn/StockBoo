package com.stockboo.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.stockboo.R;
import com.stockboo.model.BrokerageRecos;
import com.stockboo.network.StockBooRequestQueue;
import com.stockboo.scheduler.SampleAlarmReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    InterstitialAd mInterstitialAd;
    private MenuItem mSearchMenuItem;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private SearchView mAddView;
    private SearchView mSearchView;
    private Menu mMenu;
    private enum FRAGMENTS {HOME, CURRENT_SUGGESTION, PAST_PERFORMANCE, MY_WATCH_LIST, PORTFOLIO, BROKERAGE_RECOS, MARKET_NEWS, ABOUT_US, FEEDBACK};

    private FRAGMENTS mCurrentFragment;

    private static final String PREF_INSERTIAL_AD_INTERVAL = "pref_insertial_ad_intervl";
    private static final long INSERTIAL_AD_INTERVAL = 3 * 60 * 60 * 1000;

    final static int WATCH_STOCK_LIST_REQUEST_CODE = 1;
    final static int PORTFOLIO_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCurrentFragment = FRAGMENTS.HOME;
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        restoreActionBar();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-9023139403489240/8056856614");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                updateInertAdTime();
            }
        });

        if(canPlayInsertAd())
            requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("B1166B5E58D7D8172322BE3B3D50EC00")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void updateInertAdTime(){
        SharedPreferences preferences = getSharedPreferences(PREF_INSERTIAL_AD_INTERVAL, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(PREF_INSERTIAL_AD_INTERVAL, System.currentTimeMillis());
        editor.commit();
    }

    private boolean canPlayInsertAd(){
        SharedPreferences preferences = getSharedPreferences(PREF_INSERTIAL_AD_INTERVAL, MODE_PRIVATE);
        long time = preferences.getLong(PREF_INSERTIAL_AD_INTERVAL, 0);
        return (System.currentTimeMillis() - time) > INSERTIAL_AD_INTERVAL;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if(mMenu!=null) {
            mMenu.findItem(R.id.action_search).setVisible(true);
            //mMenu.findItem(R.id.action_add).setVisible(false);
            invalidateOptionsMenu();
        }
        // update the main content by replacing fragments
        switch (position){
            case 0:
                mCurrentFragment = FRAGMENTS.HOME;
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
            case 1:
                mCurrentFragment = FRAGMENTS.CURRENT_SUGGESTION;
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, CurrentSuggestionFragment.newInstance("", ""))
                        .commit();
                break;
            case 2:
                mCurrentFragment = FRAGMENTS.PAST_PERFORMANCE;
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, PastPerformanceFragment.newInstance("", ""))
                        .commit();
                break;
            case 3:
                //mMenu.findItem(R.id.action_search).setVisible(false);
                //mMenu.findItem(R.id.action_add).setVisible(true);
                if (mInterstitialAd.isLoaded() && canPlayInsertAd())
                    mInterstitialAd.show();
                mCurrentFragment = FRAGMENTS.MY_WATCH_LIST;
                invalidateOptionsMenu();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, WatchListFragment.newInstance("", ""))
                        .commit();
                break;
            case 4:
                mCurrentFragment = FRAGMENTS.PORTFOLIO;
                if (mInterstitialAd.isLoaded() && canPlayInsertAd())
                    mInterstitialAd.show();
                invalidateOptionsMenu();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, PortfolioFragment.newInstance("", ""))
                        .commit();
                break;
            case 5:
                mCurrentFragment = FRAGMENTS.BROKERAGE_RECOS;
                invalidateOptionsMenu();
                getFragmentManager().beginTransaction().replace(R.id.container, new BrokerageRecosFragment()).commit();
                break;
            case 6:
                //mMenu.findItem(R.id.action_search).setVisible(false);
                //mMenu.findItem(R.id.action_add).setVisible(true);
                mCurrentFragment = FRAGMENTS.MARKET_NEWS;
                invalidateOptionsMenu();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, MarketNewsFragment.newInstance("", ""))
                        .commit();
                break;
            case 7:
                mCurrentFragment = FRAGMENTS.ABOUT_US;
                invalidateOptionsMenu();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, AboutUsFragment.newInstance("", ""))
                        .commit();
                break;
            case 8:
                mCurrentFragment = FRAGMENTS.FEEDBACK;
                invalidateOptionsMenu();
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "info@stockboo.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;
        }
    }


    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //actionBar.setTitle(mTitle);
        View view = getLayoutInflater().inflate(R.layout.stockboo_action_bar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(view);
        //actionBar.setIcon(R.drawable.logo_header);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNavigationDrawerFragment.isDrawerOpen())
                    mNavigationDrawerFragment.closeDrawer();
                else
                    mNavigationDrawerFragment.openDrawer();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*mMenu = menu;
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            //mAddView = (SearchView) menu.findItem(R.id.action_add).getActionView();
            //mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            return true;
        }*/
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_quote:
                Intent intent1 = new Intent(MainActivity.this, QuoteActivity.class);
                startActivity(intent1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode ,data);
        switch (requestCode){
            case WATCH_STOCK_LIST_REQUEST_CODE:
                WatchListFragment f = (WatchListFragment) getFragmentManager().findFragmentById(R.id.container);
                f.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private ProgressDialog dialog;

        private static String mNseBseData;
        private static ArrayList<String> marketNews;
        private SwipeRefreshLayout swipeView;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            LinearLayout stockMsgLayout = (LinearLayout) getActivity().findViewById(R.id.heading_stock_messages);
            ((ImageView) stockMsgLayout.getChildAt(0)).setImageResource(R.drawable.stockmessage_screen);
            ((TextView) stockMsgLayout.getChildAt(1)).setText(R.string.heading_stock_message);

            AdView mAdView = (AdView) getActivity().findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    //.addTestDevice("B1166B5E58D7D8172322BE3B3D50EC00")
                    .build();
            mAdView.loadAd(adRequest);
            LinearLayout marketNewsLayout = (LinearLayout) getActivity().findViewById(R.id.heading_market_news);
            ((ImageView) marketNewsLayout.getChildAt(0)).setImageResource(R.drawable.newsicon);
            ((TextView) marketNewsLayout.getChildAt(1)).setText(R.string.heading_market_news);
            swipeView = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe);
            swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeView.setRefreshing(true);
                    loadData();
                }
            });

            updateStockMessages();
            loadData();
        }

        private void loadData(){
            SharedPreferences preferences = getActivity().getSharedPreferences("NSE_BSE_DATA", MODE_PRIVATE);
            mNseBseData = preferences.getString("NSE_BSE_DATA", null);
            if(mNseBseData == null)
                marketNews = null;
            DashboardResponseListener listener = new DashboardResponseListener();
            StringRequest bseNseRequest = new StringRequest(StringRequest.Method.GET, "http://finance.google.com/finance/info?client=ig&q=INDEXBOM:SENSEX,NSE:NIFTY", listener, listener);
            //StringRequest marketNewsRequest = new StringRequest(StringRequest.Method.GET, "http://www.moneycontrol.com/rss/MCtopnews.xml", listener, listener);
            StockBooRequestQueue.getInstance(getActivity()).getRequestQueue().add(bseNseRequest);
            //StockBooRequestQueue.getRequestQueue(this).add(marketNewsRequest);
            new UpdateMarketNewsTask().execute();
        }
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
        private class UpdateMarketNewsTask extends AsyncTask<Void, ArrayList<String>, ArrayList<String>> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(getActivity());
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
            }

            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                if(marketNews == null)
                    try {
                        marketNews =  updateMarketNews();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                return marketNews;
            }

            @Override
            protected void onPostExecute(final ArrayList<String> headlines) {
                super.onPostExecute(headlines);
                try {
                    if (((MainActivity) getActivity()).mCurrentFragment != FRAGMENTS.HOME)
                        return;
                }catch (NullPointerException e){
                    return;
                }
                //ListView lv = (ListView) findViewById(R.id.listView);
                //lv.setAdapter(new NewsHeadlineAdapter(MainActivity.this, headlines));
                LinearLayout mainLayout = (LinearLayout) getActivity().findViewById(R.id.main_layout);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(20, 5, 10, 5);
                if(headlines == null)
                    return;
                for(int i = 4; i < headlines.size(); i = i+2) {
                    final int linkPosition = i + 1;
                    LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.headlines_tv, null);
                    TextView tv = (TextView) layout.getChildAt(1);
                    tv.setText(headlines.get(i));
                    mainLayout.addView(layout, params);
                    layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), MarketNewsActivity.class);
                            intent.putExtra("link", headlines.get(linkPosition));
                            startActivity(intent);
                        }
                    });
                }
                if(dialog.isShowing())
                    dialog.dismiss();
                if(swipeView != null)
                    swipeView.setRefreshing(false);
            }
        }

        private void updateStockMessages(){
            SharedPreferences preferences = getActivity().getSharedPreferences("stock_messages", MODE_PRIVATE);
            String msgs = preferences.getString("stock_messages", null);
            if(msgs == null)
                return;
            try {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(20, 5, 10, 5);
                    LinearLayout stockMsgsLayout = (LinearLayout) getActivity().findViewById(R.id.layout_stock_msgs);
                    JSONArray array = new JSONArray(msgs);
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.headlines_tv, null);
                        TextView tv = (TextView) layout.getChildAt(1);
                        tv.setText(obj.getString("content"));
                        stockMsgsLayout.addView(layout, params);
                    }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private ArrayList<String> updateMarketNews() throws IOException, XmlPullParserException {
            //StringBuilder builder=new StringBuilder();
            ArrayList<String> headlines = new ArrayList<String>();
            URL url = new URL("http://www.moneycontrol.com/rss/MCtopnews.xml");
            //URL url = new URL("http://news.google.co.in/news?pz=1&cf=all&ned=in&hl=en&topic=b&output=rss");
            //URL url = new URL("http://economictimes.indiatimes.com/rssfeedsdefault.cms");
            //URL url = new URL("http://www.livemint.com/rss/money");

            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp=factory.newPullParser();

            xpp.setInput(url.openConnection().getInputStream(), "UTF_8");

            int eventType=xpp.getEventType();
            while(eventType!=XmlPullParser.END_DOCUMENT){
                // Looking for a start tag
                if(eventType==XmlPullParser.START_TAG){
                    //We look for "title" tag in XML response
                    if(xpp.getName().equalsIgnoreCase("title")||xpp.getName().equalsIgnoreCase("link")){
                        //Once we found the "title" tag, add the text it contains to our builder
                        //builder.append(xpp.nextText()+"\n");
                        headlines.add(xpp.nextText());
                    }
                }

                eventType=xpp.next();
            }
            return headlines;
        }

        private class DashboardResponseListener implements Response.Listener<String>, Response.ErrorListener{

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(String response) {
                try {
                    mNseBseData = response;
                    SharedPreferences preferences = getActivity().getSharedPreferences("NSE_BSE_DATA", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("NSE_BSE_DATA", response);
                    editor.commit();

                    processDashboardData(mNseBseData);
                } catch (Exception e){

                }
            }

        }

        private void processDashboardData(String response) {
            {
                try {
                    if(response.startsWith("\n// "))
                        response = response.substring(4);
                    else if(!response.trim().startsWith("["))
                        response = response.substring(response.indexOf("["));
                    JSONArray array = new JSONArray(response);
                    JSONObject bseJsonObj = array.getJSONObject(0);
                    LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.bse_layout);
                    double cp = new Double(bseJsonObj.getString("cp")).doubleValue();
                    if(cp<0){
                        //((TextView) layout.getChildAt(1)).setTextColor(Color.RED);
                        ((TextView) layout.getChildAt(2)).setTextColor(Color.RED);
                    }
                    ((TextView) layout.getChildAt(1)).setText(bseJsonObj.getString("l_fix"));
                    ((TextView) layout.getChildAt(2)).setText(bseJsonObj.getString("c") + " (" + bseJsonObj.getString("cp") + "%)");
                    JSONObject nseJsonObj = array.getJSONObject(1);
                    layout = (LinearLayout) getActivity().findViewById(R.id.nse_layout);
                    cp = new Double(nseJsonObj.getString("cp")).doubleValue();
                    if(cp<0){
                        // ((TextView) layout.getChildAt(1)).setTextColor(Color.RED);
                        ((TextView) layout.getChildAt(2)).setTextColor(Color.RED);
                    }
                    ((TextView) layout.getChildAt(0)).setText("NSE");
                    ((TextView) layout.getChildAt(1)).setText(nseJsonObj.getString("l_fix"));
                    ((TextView) layout.getChildAt(2)).setText(nseJsonObj.getString("c") + " (" + nseJsonObj.getString("cp") + "%)");
                    String lastUpdated = new String().format(getString(R.string.last_updated_label), nseJsonObj.getString("ltt"));
                    ((TextView)getActivity().findViewById(R.id.last_updated_tv)).setText(lastUpdated);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}