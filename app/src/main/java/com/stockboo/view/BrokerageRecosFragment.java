package com.stockboo.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.stockboo.R;
import com.stockboo.model.BrokerageRecos;
import com.stockboo.model.db.DatabaseHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by prsn0001 on 10/10/2015.
 */
public class BrokerageRecosFragment extends RSSFeedFragment {

    private static final String LINK_BROKERAGE_RECOS = "http://www.moneycontrol.com/rss/brokeragerecos.xml";

    private DatabaseHelper dbHelper;


    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private CursorAdapter mAdapter;


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

    private Cursor getCursor(){
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String SqlQuery = "SELECT * FROM BrokerageRecos";
        Cursor cursor = database.rawQuery(SqlQuery, null);
        return cursor;
        // TODO: Change Adapter to display your content

    }
    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        long lastUpdatedTime = preferences.getLong("brokerage_recos_last_update", 0);
        if((System.currentTimeMillis() - lastUpdatedTime) > UPDATE_INTERVAL)
            new UpdateBrokageRecosTask().execute(LINK_BROKERAGE_RECOS);
    }

    private class UpdateBrokageRecosTask extends AsyncTask<String, ArrayList<BrokerageRecos>, ArrayList<BrokerageRecos>> {

        ProgressDialog dialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
                DeleteBuilder<BrokerageRecos, Integer> deleteBuilder = dbHelper.getBrokerageRecosRuntimeDao().deleteBuilder();
                deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected ArrayList<BrokerageRecos> doInBackground(String... params) {
            try {
                for(String url: params)
                    updateBrokerageRecos(new URL(url));
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<BrokerageRecos> headlines) {
            super.onPostExecute(headlines);
            SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("brokerage_recos_last_update", System.currentTimeMillis());
            editor.apply();
            mAdapter.changeCursor(getCursor());
            mAdapter.notifyDataSetChanged();
            dialog.hide();
        }
    }

    private ArrayList<BrokerageRecos> updateBrokerageRecos(URL url) throws IOException, XmlPullParserException {
        //StringBuilder builder=new StringBuilder();
        ArrayList<BrokerageRecos> headlines = new ArrayList<BrokerageRecos>();
        //URL url = new URL("http://www.moneycontrol.com/rss/MCtopnews.xml");
        //URL url = new URL("http://news.google.co.in/news?pz=1&cf=all&ned=in&hl=en&topic=b&output=rss");
        //URL url = new URL("http://economictimes.indiatimes.com/rssfeedsdefault.cms");
        //URL url = new URL("http://www.livemint.com/rss/money");

        RuntimeExceptionDao<BrokerageRecos, Integer> brokerageRecosDao = dbHelper.getBrokerageRecosRuntimeDao();
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
        BrokerageRecos item = new BrokerageRecos();

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
                    XmlPullParserFactory factory1 = XmlPullParserFactory.newInstance();
                    XmlPullParser xpp1 = factory1.newPullParser();

                    xpp1.setInput( new StringReader( desc ) );
                    int eventType1 = xpp1.getEventType();
                    while (eventType1 != XmlPullParser.END_DOCUMENT) {
                        if(eventType1 == XmlPullParser.START_TAG && "img".equals(xpp1.getName())) {
                            String nextText = xpp1.getAttributeValue(null, "src");
                            item.setThumbnailLink(nextText);
                            break;
                        }
                        eventType1 = xpp1.next();
                    }
                    int index = desc.indexOf("/>");
                    if( index > 0 )
                        desc = desc.substring(index + 2).trim();
                    item.setDescription(desc);
                }
                else if(xpp.getName().equalsIgnoreCase("thumbnail"))
                    item.setThumbnailLink(xpp.nextText());
                else if(xpp.getName().equalsIgnoreCase("pubDate"))
                    item.setPubDate(xpp.nextText());
            }else if(eventType==XmlPullParser.END_TAG){
                if(xpp.getName().equalsIgnoreCase("item")){
                    brokerageRecosDao.create(item);
                    item = new BrokerageRecos();
                }
            }
            //mAdapter.changeCursor(getCursor());
            eventType=xpp.next();
        }
        //brokerageRecosDao.create(item);
        return headlines;
    }
}
