package com.stockboo.view;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
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
import com.stockboo.R;
import com.stockboo.network.StockBooRequestQueue;

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

    private MenuItem mSearchMenuItem;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private SearchView mAddView;
    private SearchView mSearchView;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
            case 1:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, CurrentSuggestionFragment.newInstance("", ""))
                        .commit();
                break;
            case 3:
                mMenu.findItem(R.id.action_search).setVisible(false);
                //mMenu.findItem(R.id.action_add).setVisible(true);
                invalidateOptionsMenu();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, WatchListFragment.newInstance("", ""))
                        .commit();
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
        actionBar.setDisplayShowTitleEnabled(false);
        //actionBar.setTitle(mTitle);
        View view = getLayoutInflater().inflate(R.layout.stockboo_action_bar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(view);
        //actionBar.setIcon(R.drawable.logo_header);
        findViewById(R.id.btn_navigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNavigationDrawerFragment.isDrawerOpen())
                    mNavigationDrawerFragment.closeDrawer();
                else
                    mNavigationDrawerFragment.openDrawer();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            //mAddView = (SearchView) menu.findItem(R.id.action_add).getActionView();
            //mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            Intent intent = new Intent(MainActivity.this, StockListSearchActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            LinearLayout stockMsgLayout = (LinearLayout) getActivity().findViewById(R.id.heading_stock_messages);
            ((ImageView) stockMsgLayout.getChildAt(0)).setImageResource(R.drawable.stockmessage_screen);
            ((TextView) stockMsgLayout.getChildAt(1)).setText(R.string.heading_stock_message);

            LinearLayout marketNewsLayout = (LinearLayout) getActivity().findViewById(R.id.heading_market_news);
            ((ImageView) marketNewsLayout.getChildAt(0)).setImageResource(R.drawable.newsicon);
            ((TextView) marketNewsLayout.getChildAt(1)).setText(R.string.heading_market_news);
            DashboardResponseListener listener = new DashboardResponseListener();
            StringRequest bseNseRequest = new StringRequest(StringRequest.Method.GET, "http://finance.google.com/finance/info?client=ig&q=INDEXBOM:SENSEX,NSE:NIFTY", listener, listener);
            //StringRequest marketNewsRequest = new StringRequest(StringRequest.Method.GET, "http://www.moneycontrol.com/rss/MCtopnews.xml", listener, listener);
            StockBooRequestQueue.getRequestQueue(getActivity()).add(bseNseRequest);
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
            protected ArrayList<String> doInBackground(Void... params) {
                try {
                    return updateMarketNews();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(final ArrayList<String> headlines) {
                super.onPostExecute(headlines);
                //ListView lv = (ListView) findViewById(R.id.listView);
                //lv.setAdapter(new NewsHeadlineAdapter(MainActivity.this, headlines));
                LinearLayout mainLayout = (LinearLayout) getActivity().findViewById(R.id.main_layout);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(20, 10, 10, 10);
                for(int i = 4; i < headlines.size(); i = i+2) {
                    final int linkPosition = i + 1;
                    LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.headlines_tv, null);
                    TextView tv = (TextView) layout.getChildAt(0);
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
            }
        }
        private ArrayList<String> updateMarketNews() throws IOException, XmlPullParserException {
            //StringBuilder builder=new StringBuilder();
            ArrayList<String> headlines = new ArrayList<String>();
            URL url = new URL("http://www.moneycontrol.com/rss/MCtopnews.xml");

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
                    if(response.startsWith("\n// "))
                        response = response.substring(4);
                    JSONArray array = new JSONArray(response);
                    JSONObject bseJsonObj = array.getJSONObject(0);
                    LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.bse_layout);
                    ((TextView) layout.getChildAt(1)).setText(bseJsonObj.getString("l_fix"));
                    ((TextView) layout.getChildAt(2)).setText(bseJsonObj.getString("c") + " (" + bseJsonObj.getString("cp") + "%)");
                    JSONObject nseJsonObj = array.getJSONObject(1);
                    layout = (LinearLayout) getActivity().findViewById(R.id.nse_layout);
                    ((TextView) layout.getChildAt(0)).setText("NSE");
                    ((TextView) layout.getChildAt(1)).setText(nseJsonObj.getString("l_fix"));
                    ((TextView) layout.getChildAt(2)).setText(nseJsonObj.getString("c") + " (" + nseJsonObj.getString("cp") + "%)");
                    String lastUpdated = new String().format(getString(R.string.last_updated_label), nseJsonObj.getString("ltt"));
                    ((TextView)getActivity().findViewById(R.id.last_updated_tv)).setText(lastUpdated);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
