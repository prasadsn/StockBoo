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
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.table.TableUtils;
import com.stockboo.R;

import com.stockboo.model.NewsItem;
import com.stockboo.model.StockList;
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
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MarketNewsFragment extends Fragment implements AbsListView.OnItemClickListener {

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

    private class NewsListAdapter extends CursorAdapter {

        public NewsListAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            RelativeLayout layout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.market_news_list_item, null);
            return layout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            updateView(view, context, cursor);
        }
    }

    private void updateView(View view, Context context, Cursor cursor){
        RelativeLayout layout = (RelativeLayout) view;
        final String link = cursor.getString(1);

        if( link!=null && link.contains("news.google.com"))
            layout.getChildAt(1).setVisibility(View.GONE);
        else
            layout.getChildAt(1).setVisibility(View.VISIBLE);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MarketNewsActivity.class);
                intent.putExtra("link", link);
                startActivity(intent);
            }
        });

        TextView titleView = (TextView) layout.getChildAt(0);
        TextView descriptionView = (TextView) layout.findViewById(R.id.textView11);
        TextView updatedDateView = (TextView) layout.getChildAt(2);
        NetworkImageView imageView = (NetworkImageView) layout.findViewById(R.id.networkImageView);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        Date pubDate = null;
        try {
            pubDate = simpleDateFormat.parse(cursor.getString(2));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(pubDate);
        int difference = (int) (System.currentTimeMillis() - calendar1.getTimeInMillis()) + (5 * 60 * 60 * 1000);
        int days = (int) (difference / (1000*60*60*24));
        int hours = 0;
        hours = (int) ((difference - (1000*60*60*24) * days) / (1000*60*60));
        if(days>1)
            hours = hours % 24;
        String time = days > 1? days + " day ago" : hours + " hours ago";
        titleView.setText(cursor.getString(4));
        descriptionView.setText(cursor.getString(0));
        updatedDateView.setText(time);
        imageView.setImageUrl(cursor.getString(3), StockBooRequestQueue.getInstance(getActivity().getApplicationContext()).getImageLoader());
   }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item3, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

            // TODO: Change Adapter to display your content
            mAdapter = new NewsListAdapter(getActivity(), getCursor(), true);
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
            new UpdateMarketNewsTask().execute();
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

    private class UpdateMarketNewsTask extends AsyncTask<Void, ArrayList<NewsItem>, ArrayList<NewsItem>> {

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
        protected ArrayList<NewsItem> doInBackground(Void... params) {
            try {
                updateMarketNews(new URL(RSS_LIVEMINT));
                updateMarketNews(new URL(RSS_ECONOMIC_TIMES));
                updateMarketNews(new URL(RSS_GOOGLE_NEWS));
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
