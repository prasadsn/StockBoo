package com.stockboo.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
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

import java.sql.SQLException;
import java.util.HashMap;
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
    BackgroundContainer mBackgroundContainer;

    private OnFragmentInteractionListener mListener;
    private DatabaseHelper dbHelper;

    boolean mSwiping = false;
    boolean mItemPressed = false;
    HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();

    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;
    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        new WatchListTask().execute();
        mAdapter = new WatchListAdapter(getWatchList());
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
    }

    /*private Cursor getCursor(){
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String SqlQuery = "SELECT * FROM WatchList";
        Cursor cursor = database.rawQuery(SqlQuery, null);
        return cursor;
    }*/

    private List<WatchList> getWatchList(){
        RuntimeExceptionDao<WatchList, Integer> watchListDao = dbHelper.getWatchListRuntimeDao();
        return watchListDao.queryForAll();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!=getActivity().RESULT_OK)
            return;
        StockList stockList = data.getParcelableExtra("Stock");
        WatchList watchList = new WatchList(stockList.getSYMBOL(), stockList.getScriptName(), stockList.getStatus(), stockList.getISINNO(), stockList.getIndustry(), stockList.getGroup(), null, stockList.getScriptID(), null, null);
        RuntimeExceptionDao<WatchList, Integer> watchListDao = dbHelper.getWatchListRuntimeDao();
        watchListDao.create(watchList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item2, container, false);

        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        LinearLayout watchListHeading = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.stockboo_heading_layout, mListView, false);
        ((ImageView) watchListHeading.getChildAt(0)).setImageResource(R.drawable.mywatch_icon);
        ((TextView) watchListHeading.getChildAt(1)).setText(R.string.title_demo_5);
        ((ImageView) watchListHeading.getChildAt(2)).setImageResource(android.R.drawable.ic_menu_add);
        watchListHeading.getChildAt(2).setVisibility(View.VISIBLE);
        watchListHeading.getChildAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StockListSearchActivity.class);
                startActivityForResult(intent, MainActivity.WATCH_STOCK_LIST_REQUEST_CODE);
            }
        });
        ((ListView) mListView).addHeaderView(watchListHeading);
        mBackgroundContainer = (BackgroundContainer) view.findViewById(R.id.listViewBackground);
        AdView mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("B1166B5E58D7D8172322BE3B3D50EC00")
                .build();
        mAdView.loadAd(adRequest);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dbHelper = OpenHelperManager.getHelper(getActivity().getApplicationContext(), DatabaseHelper.class);
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
    private class WatchListAdapter extends BaseAdapter {

        List<WatchList> mList = null;

        public WatchListAdapter(List<WatchList> list){
            mList = list;
        }
        public void updateView(Context context, View view, WatchList watchList){
            LinearLayout layout = (LinearLayout) view;
            TextView stockNameTv = (TextView) layout.findViewById(R.id.scriptName);
            TextView stockIdTv = (TextView) layout.findViewById(R.id.scriptID);
            TextView cTv = (TextView) layout.findViewById(R.id.c);
            TextView priceTv = (TextView) layout.findViewById(R.id.price);
            TextView cpFixTv = (TextView) layout.findViewById(R.id.c_fix);

            stockNameTv.setText(watchList.getScriptName());
            stockIdTv.setText(watchList.getScriptID());
            priceTv.setText(watchList.getPrice());
            Double c = null;
            Double cp_fix = null;
            if(watchList.getC()!=null && !watchList.getC().isEmpty()){
            c = new Double(watchList.getC());
            if(c!=null && c.doubleValue() < 0)
                cTv.setBackgroundResource(R.drawable.oval_background_red);
            else
                cTv.setBackgroundResource(R.drawable.oval_background_green);
            cTv.setText(c.toString());
            } else
                cTv.setVisibility(View.INVISIBLE);
            if(watchList.getC_fix()!=null && !watchList.getC_fix().isEmpty()) {
                cp_fix = new Double(watchList.getC_fix());
                if (cp_fix != null && cp_fix.doubleValue() < 0)
                    cpFixTv.setBackgroundResource(R.drawable.oval_background_red);
                else
                    cpFixTv.setBackgroundResource(R.drawable.oval_background_green);
                cpFixTv.setText(watchList.getC_fix() + " %");
            } else
                cpFixTv.setVisibility(View.INVISIBLE);
            view.setTag(watchList.get_Id());
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.watchlist_item, null);
            convertView.setOnTouchListener(mTouchListener);
            updateView(getActivity(), convertView, mList.get(position));
            return convertView;
        }
    }

    private class WatchListTask extends AsyncTask<Void, String, String>{

        @Override
        protected String doInBackground(Void... params) {
            List<WatchList> list = dbHelper.getWatchListRuntimeDao().queryForAll();
            if(list.size()<1)
                return null;
            StringBuffer reqParamBuffer = new StringBuffer();
            for(WatchList watchList: list){
                if(watchList.getGroup() == null)
                    continue;
                reqParamBuffer.append(watchList.getGroup().equals("A") ? "NSE:"+watchList.getScriptID() : "BOM:"+watchList.getScriptID()).append(",");
            }
            if(reqParamBuffer.length()<1)
                return null;
            reqParamBuffer.substring(0, reqParamBuffer.length()-2);
            return reqParamBuffer.toString();
        }

        @Override
        protected void onPostExecute(String reqParamBuffer) {
            super.onPostExecute(reqParamBuffer);
            if(reqParamBuffer!=null)
                loadData(reqParamBuffer);
        }
    }
    private void loadData(String reqParamBuffer){
        Listener listener = new Listener();
        String request = "http://finance.google.com/finance/info?client=ig&q=" + reqParamBuffer;
        StringRequest bseNseRequest = new StringRequest(StringRequest.Method.GET, request, listener, listener);
        StockBooRequestQueue.getInstance(getActivity()).getRequestQueue().add(bseNseRequest);
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
                StringBuffer reqParamBuffer = new StringBuffer();
                RuntimeExceptionDao<WatchList, Integer> watchListDao = dbHelper.getWatchListRuntimeDao();
                for(int i = 0; i<array.length(); i ++){
                    JSONObject jsonObject = array.getJSONObject(i);
                    List<WatchList> list = watchListDao.queryBuilder().where().eq("ScriptID", jsonObject.getString("t")).query();
                    if(list.size() == 0)
                        list = watchListDao.queryBuilder().where().eq("SYMBOL", jsonObject.getString("t")).query();
                    if(list.size() == 0)
                        continue;
                    WatchList watchList = list.get(0);
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
            mAdapter = new WatchListAdapter(getWatchList());
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
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

    /**
     * Handle touch events to fade/move dragged items as they are swiped out
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        float mDownX;
        private int mSwipeSlop = -1;

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if (mSwipeSlop < 0) {
                mSwipeSlop = ViewConfiguration.get(getActivity()).
                        getScaledTouchSlop();
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mItemPressed) {
                        // Multi-item swipes not handled
                        return false;
                    }
                    mItemPressed = true;
                    mDownX = event.getX();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1);
                    v.setTranslationX(0);
                    mItemPressed = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                {
                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - mDownX;
                    float deltaXAbs = Math.abs(deltaX);
                    if (!mSwiping) {
                        if (deltaXAbs > mSwipeSlop) {
                            mSwiping = true;
                            mListView.requestDisallowInterceptTouchEvent(true);
                            mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
                        }
                    }
                    if (mSwiping) {
                        v.setTranslationX((x - mDownX));
                        v.setAlpha(1 - deltaXAbs / v.getWidth());
                    }
                }
                break;
                case MotionEvent.ACTION_UP:
                {
                    // User let go - figure out whether to animate the view out, or back into place
                    if (mSwiping) {
                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - mDownX;
                        float deltaXAbs = Math.abs(deltaX);
                        float fractionCovered;
                        float endX;
                        float endAlpha;
                        final boolean remove;
                        if (deltaXAbs > v.getWidth() / 4) {
                            // Greater than a quarter of the width - animate it out
                            fractionCovered = deltaXAbs / v.getWidth();
                            endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                            endAlpha = 0;
                            remove = true;
                        } else {
                            // Not far enough - animate it back
                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
                            endX = 0;
                            endAlpha = 1;
                            remove = false;
                        }
                        // Animate position and alpha of swiped item
                        // NOTE: This is a simplified version of swipe behavior, for the
                        // purposes of this demo about animation. A real version should use
                        // velocity (via the VelocityTracker class) to send the item off or
                        // back at an appropriate speed.
                        long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                        mListView.setEnabled(false);
                        v.animate().setDuration(duration).
                                alpha(endAlpha).translationX(endX).
                                withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Restore animated values
                                        v.setAlpha(1);
                                        v.setTranslationX(0);
                                        if (remove) {
                                            animateRemoval(mListView, v);
                                        } else {
                                            mBackgroundContainer.hideBackground();
                                            mSwiping = false;
                                            mListView.setEnabled(true);
                                        }
                                    }
                                });
                    }
                }
                mItemPressed = false;
                break;
                default:
                    return false;
            }
            return true;
        }
    };

    /**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    private void animateRemoval(final ListView listview, View viewToRemove) {
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = mAdapter.getItemId(position);
                mItemIdTopMap.put(itemId, child.getTop());
            }
        }
        // Delete the item from the adapter
        RuntimeExceptionDao<WatchList, Integer> dao = dbHelper.getWatchListRuntimeDao();
//get your Dao
        DeleteBuilder<WatchList, Integer> deleteBuilder = dao.deleteBuilder();
        try {
            deleteBuilder.where().eq("_id", viewToRemove.getTag());
        deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mAdapter = new WatchListAdapter(getWatchList());
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        //int position = mListView.getPositionForView(viewToRemove);
        //mAdapter.remove(mAdapter.getItem(position));

        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listview.getFirstVisiblePosition();
                for (int i = 0; i < listview.getChildCount(); ++i) {
                    final View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = mAdapter.getItemId(position);
                    Integer startTop = mItemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if (firstAnimation) {
                                child.animate().withEndAction(new Runnable() {
                                    public void run() {
                                        mBackgroundContainer.hideBackground();
                                        mSwiping = false;
                                        mListView.setEnabled(true);
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    } else {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                public void run() {
                                    mBackgroundContainer.hideBackground();
                                    mSwiping = false;
                                    mListView.setEnabled(true);
                                }
                            });
                            firstAnimation = false;
                        }
                    }
                }
                mItemIdTopMap.clear();
                return true;
            }
        });
    }

}
