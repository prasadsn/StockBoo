package com.stockboo.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.stockboo.R;

import com.stockboo.model.NewsItem;
import com.stockboo.model.db.DatabaseHelper;
import com.stockboo.network.StockBooRequestQueue;
import com.stockboo.view.dummy.DummyContent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MarketNewsFragment extends RSSFeedFragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String RSS_GOOGLE_NEWS = "http://news.google.co.in/news?pz=1&cf=all&ned=in&hl=en&topic=b&output=rss";
    private static final String RSS_ECONOMIC_TIMES = "http://economictimes.indiatimes.com/rssfeedsdefault.cms";
    private static final String RSS_LIVEMINT = "http://www.livemint.com/rss/money";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final long UPDATE_INTERVAL = 3 * 60 * 60 * 1000;

    private DatabaseHelper dbHelper;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private CursorAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static MarketNewsFragment newInstance(String param1, String param2) {
        MarketNewsFragment fragment = new MarketNewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MarketNewsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    private Cursor getCursor(){
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String SqlQuery = "SELECT * FROM NewsItem";
        Cursor cursor = database.rawQuery(SqlQuery, null);
        return cursor;
        // TODO: Change Adapter to display your content

    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item3, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

            // TODO: Change Adapter to display your content
            mAdapter = new RSSFeedAdapter(getActivity(), getCursor(), true);
            mListView.setAdapter(mAdapter);
        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dbHelper = OpenHelperManager.getHelper(getActivity().getApplicationContext(), DatabaseHelper.class);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        long lastUpdatedTime = preferences.getLong("last_update", 0);
        if((System.currentTimeMillis() - lastUpdatedTime) > UPDATE_INTERVAL)
            new UpdateMarketNewsTask().execute(RSS_LIVEMINT, RSS_ECONOMIC_TIMES, RSS_GOOGLE_NEWS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    private class UpdateMarketNewsTask extends AsyncTask<String, ArrayList<NewsItem>, ArrayList<NewsItem>> {

        ProgressDialog dialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
                DeleteBuilder<NewsItem, Integer> deleteBuilder = dbHelper.getNewsListDao().deleteBuilder();
                deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected ArrayList<NewsItem> doInBackground(String... params) {
            try {
                for(String url: params)
                    updateMarketNews(new URL(url));
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<NewsItem> headlines) {
            super.onPostExecute(headlines);
            SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("last_update", System.currentTimeMillis());
            editor.apply();
            mAdapter.changeCursor(getCursor());
            mAdapter.notifyDataSetChanged();
            dialog.hide();
        }
   }
    private ArrayList<NewsItem> updateMarketNews(URL url) throws IOException, XmlPullParserException {
        //StringBuilder builder=new StringBuilder();
        ArrayList<NewsItem> headlines = new ArrayList<NewsItem>();
        //URL url = new URL("http://www.moneycontrol.com/rss/MCtopnews.xml");
        //URL url = new URL("http://news.google.co.in/news?pz=1&cf=all&ned=in&hl=en&topic=b&output=rss");
        //URL url = new URL("http://economictimes.indiatimes.com/rssfeedsdefault.cms");
        //URL url = new URL("http://www.livemint.com/rss/money");

        RuntimeExceptionDao<NewsItem, Integer> newsListDao = dbHelper.getNewsListRuntimeDao();
        XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp=factory.newPullParser();

        xpp.setInput(url.openConnection().getInputStream(), "UTF_8");

        int eventType = xpp.getEventType();
        String name = null;
        while(name == null || !name.equalsIgnoreCase("item")) {
            eventType = xpp.next();
            name = xpp.getName();
        }
            NewsItem item = new NewsItem();

        while(eventType!=XmlPullParser.END_DOCUMENT) {
            // Looking for a start tag
            if(eventType==XmlPullParser.START_TAG){
                //We look for "title" tag in XML response
                if(xpp.getName().equalsIgnoreCase("title"))
                    item.setTitle(xpp.nextText());
                else if(xpp.getName().equalsIgnoreCase("link"))
                    item.setLink(xpp.nextText());
                else if(xpp.getName().equalsIgnoreCase("description")) {
                    String desc = xpp.nextText();
                    int index = desc.indexOf("\">");
                    if( index > 0 )
                    desc = desc.substring(index + 2);
                    item.setDescription(desc);
                }
                else if(xpp.getName().equalsIgnoreCase("thumbnail"))
                    item.setThumbnailLink(xpp.nextText());
                else if(xpp.getName().equalsIgnoreCase("pubDate"))
                    item.setPubDate(xpp.nextText());
            }else if(eventType==XmlPullParser.END_TAG){
                if(xpp.getName().equalsIgnoreCase("item")){
                    newsListDao.create(item);
                    item = new NewsItem();
                }
            }
            //mAdapter.changeCursor(getCursor());
            eventType=xpp.next();
        }
        //newsListDao.create(item);
        return headlines;
    }
}
