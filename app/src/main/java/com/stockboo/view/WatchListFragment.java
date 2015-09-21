package com.stockboo.view;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.parse.ParseObject;
import com.stockboo.R;

import com.stockboo.model.StockList;
import com.stockboo.model.WatchList;
import com.stockboo.model.db.DatabaseHelper;
import com.stockboo.network.StockBooRequestQueue;
import com.stockboo.view.dummy.DummyContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class WatchListFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
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

    // TODO: Rename and change types of parameters
    public static WatchListFragment newInstance(String param1, String param2) {
        WatchListFragment fragment = new WatchListFragment();
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
    public WatchListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        dbHelper = OpenHelperManager.getHelper(getActivity().getApplicationContext(), DatabaseHelper.class);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String SqlQuery = "SELECT * FROM WatchList";
        Cursor cursor = database.rawQuery(SqlQuery, null);
        // TODO: Change Adapter to display your content
        mAdapter = new WatchListAdapter(getActivity().getApplicationContext(), cursor, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item2, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        loadData();
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
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
    private class WatchListAdapter extends CursorAdapter {

        public WatchListAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            RelativeLayout layout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.watchlist_item, null);
            return layout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            updateView(context, view, cursor);
        }

        public void updateView(Context context, View view, Cursor cursor){
            RelativeLayout layout = (RelativeLayout) view;
            TextView stockNameTv = (TextView) layout.getChildAt(0);
            TextView stockIdTv = (TextView) layout.getChildAt(1);
            TextView cTv = (TextView) layout.getChildAt(2);
            TextView priceTv = (TextView) layout.getChildAt(3);
            TextView cpFixTv = (TextView) layout.getChildAt(4);

            stockNameTv.setText(cursor.getString(5));
            stockIdTv.setText(cursor.getString(4));
            cTv.setText(cursor.getString(8));
            priceTv.setText(cursor.getString(10));
            cpFixTv.setText(cursor.getString(9));
        }
    }

    private void loadData(){
        Listener listener = new Listener();
        dbHelper = OpenHelperManager.getHelper(getActivity().getApplicationContext(), DatabaseHelper.class);
        List<WatchList> list = dbHelper.getWatchListRuntimeDao().queryForAll();
        StringBuffer reqParamBuffer = new StringBuffer();
        for(WatchList watchList: list){
            if(watchList.getGroup() == null)
                continue;
            reqParamBuffer.append(watchList.getGroup().equals("A") ? "NSE:"+watchList.getScriptID() : "BOM:"+watchList.getScriptID()).append(",");
        }
        reqParamBuffer.substring(0, reqParamBuffer.length()-2);
        String request = "http://finance.google.com/finance/info?client=ig&q=" + reqParamBuffer;
        StringRequest bseNseRequest = new StringRequest(StringRequest.Method.GET, request, listener, listener);
        StockBooRequestQueue.getRequestQueue(getActivity()).add(bseNseRequest);

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
                List<WatchList> list = dbHelper.getWatchListRuntimeDao().queryForAll();
                StringBuffer reqParamBuffer = new StringBuffer();
                RuntimeExceptionDao<WatchList, Integer> watchListDao = dbHelper.getWatchListRuntimeDao();
                for(int i = 0; i<list.size(); i ++){
                    WatchList watchList = list.get(i);
                    JSONObject jsonObject = array.getJSONObject(i);
                    String cValue = jsonObject.getString("c_fix");
                    String c_fixValue = jsonObject.getString("cp_fix");
                    String price = jsonObject.getString("l_cur");
                    watchList.setC(cValue);
                    watchList.setC_fix(c_fixValue);
                    watchList.setPrice(price);
                    watchListDao.update(watchList);
                }
                //((AdapterView<ListAdapter>) mListView).setAdapter(new SuggestionAdapter(objects, array));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mAdapter.notifyDataSetChanged();;
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

}
