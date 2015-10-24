package com.stockboo.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import com.stockboo.R;
import com.stockboo.model.Portfolio;
import com.stockboo.model.StockList;
import com.stockboo.model.db.DatabaseHelper;
import com.stockboo.network.StockBooRequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PortfolioFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PortfolioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortfolioFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    BackgroundContainer mBackgroundContainer;
    boolean mSwiping = false;
    boolean mItemPressed = false;
    HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();

    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;

    private DatabaseHelper dbHelper;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PortfolioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PortfolioFragment newInstance(String param1, String param2) {
        PortfolioFragment fragment = new PortfolioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PortfolioFragment() {
        // Required empty public constructor
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);
        List<Portfolio> portfolioList = dbHelper.getPortfolioRuntimeDao().queryForAll();
        //mAdapter = new PortfolioAdapter(portfolioList);
        //mListView.setAdapter(mAdapter);

        LinearLayout portfolioHeading = (LinearLayout) view.findViewById(R.id.portfolio_heading);
        ((ImageView) portfolioHeading.getChildAt(0)).setImageResource(R.drawable.myportfolio);
        ((TextView) portfolioHeading.getChildAt(1)).setText(R.string.title_demo_6);
        ((ImageView) portfolioHeading.getChildAt(2)).setImageResource(android.R.drawable.ic_menu_add);
        portfolioHeading.getChildAt(2).setVisibility(View.VISIBLE);
        portfolioHeading.getChildAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddStockActivity.class);
                startActivity(intent);
            }
        });
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new PortfolioTask().execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dbHelper = OpenHelperManager.getHelper(getActivity().getApplicationContext(), DatabaseHelper.class);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        public void onFragmentInteraction(Uri uri);
    }

    private class PortfolioAdapter extends BaseAdapter {

        private List<Portfolio> mList;

        public PortfolioAdapter (List<Portfolio> list){
            mList = list;
            updatePortFolioBoard();
        }
        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = getActivity().getLayoutInflater().inflate(R.layout.portfolio_list_item, null);
            convertView.setOnTouchListener(mTouchListener);
            Portfolio portfolio = mList.get(position);
            RelativeLayout layout = (RelativeLayout) convertView;
            TextView scriptName = (TextView) layout.getChildAt(0);
            TextView currentPriceTv = (TextView) layout.findViewById(R.id.textView17);
            TextView buyingCostTv = (TextView) layout.findViewById(R.id.textView19);
            TextView investementTv = (TextView) layout.findViewById(R.id.textView18);
            TextView currentValueTv = (TextView) layout.findViewById(R.id.textView20);
            currentPriceTv.setText(String.format(getString(R.string.label_portfolio_current_price), portfolio.getCurrentPrice()));
            buyingCostTv.setText(String.format(getString(R.string.label_portfolio_buying_cost), portfolio.getPrice()));
            investementTv.setText(String.format(getString(R.string.label_portfolio_investement), formatK(portfolio.getPrice() * portfolio.getQuantity())));
            currentValueTv.setText(String.format(getString(R.string.label_portfolio_current_value), formatK(portfolio.getCurrentPrice() * portfolio.getQuantity())));
            scriptName.setText(portfolio.getScriptName() + " (" + portfolio.getQuantity() +" Units)");
            convertView.setTag(portfolio.get_Id());
            return convertView;
        }
    }
    private class PortfolioTask extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... params) {
            List<Portfolio> list = dbHelper.getPortfolioRuntimeDao().queryForAll();
            if(list.size()<1)
                return null;
            StringBuffer reqParamBuffer = new StringBuffer();
            for(Portfolio Portfolio: list){
                if(Portfolio.getGroup() == null)
                    continue;
                reqParamBuffer.append(Portfolio.getGroup().equals("A") ? "NSE:"+Portfolio.getScriptID() : "BOM:"+Portfolio.getScriptID()).append(",");
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

    private List<Portfolio> getPortfolio(){
        return dbHelper.getPortfolioRuntimeDao().queryForAll();
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
                //List<Portfolio> list = dbHelper.getPortfolioRuntimeDao().queryForAll();
                StringBuffer reqParamBuffer = new StringBuffer();
                RuntimeExceptionDao<Portfolio, Integer> portfolioDao = dbHelper.getPortfolioRuntimeDao();
                for(int i = 0; i<array.length(); i ++){
                    JSONObject jsonObject = array.getJSONObject(i);
                    List<Portfolio> list = portfolioDao.queryBuilder().where().eq("ScriptID", jsonObject.getString("t")).query();
                    if(list.size() == 0)
                        list = portfolioDao.queryBuilder().where().eq("SYMBOL", jsonObject.getString("t")).query();
                    if(list.size() == 0)
                        continue;
                    Portfolio portfolio = list.get(0);
                    String price = jsonObject.getString("l_fix");
                    String change = jsonObject.getString("c");
                    portfolio.setCurrentPrice((int) Float.parseFloat(price));
                    portfolio.setChange(Double.valueOf(change));
                    portfolioDao.update(portfolio);
                }
                //((AdapterView<ListAdapter>) mListView).setAdapter(new SuggestionAdapter(objects, array));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            mAdapter = new PortfolioAdapter(getPortfolio());
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        }
    }

    private void updatePortFolioBoard(){

        int totalInvestement = 0;
        int netWorth = 0;
        double totDayChange = 0;
        List<Portfolio> list = dbHelper.getPortfolioRuntimeDao().queryForAll();
        for(Portfolio portfolio:list){
            totalInvestement += portfolio.getPrice() * portfolio.getQuantity();
            netWorth += portfolio.getCurrentPrice() * portfolio.getQuantity();
            totDayChange += portfolio.getChange() * portfolio.getQuantity();
        }
        LinearLayout totalInvestementlayout = (LinearLayout) getActivity().findViewById(R.id.portfolio_total_investement);
        LinearLayout netWorthlayout = (LinearLayout) getActivity().findViewById(R.id.portfolio_networth);
        LinearLayout totalChangelayout = (LinearLayout) getActivity().findViewById(R.id.portfolio_total_change);
        totalInvestementlayout.setBackgroundColor(getResources().getColor(R.color.light_gray));
        netWorthlayout.setBackgroundColor(getResources().getColor(R.color.light_gray));
        ((TextView) totalInvestementlayout.getChildAt(0)).setText("TOTAL INVESTMENT");
        ((TextView) totalInvestementlayout.getChildAt(1)).setText("\u20B9" + formatK(totalInvestement));
        ((TextView) netWorthlayout.getChildAt(0)).setText("MY NETWORTH");
        ((TextView) netWorthlayout.getChildAt(1)).setText("\u20B9" + formatK(netWorth));
        ((TextView) totalChangelayout.getChildAt(0)).setText("TOTDAY CHANGE");
        ((TextView) totalChangelayout.getChildAt(0)).setTextColor(Color.WHITE);
        ((TextView) totalChangelayout.getChildAt(1)).setText("\u20B9" + formatK((int) totDayChange));
        ((TextView) totalChangelayout.getChildAt(1)).setTextColor(Color.WHITE);
    }

    public static String formatK(int number) {
        if (number < 999) {
            return String.valueOf(number);
        }

        if (number < 9999) {
            String strNumber = String.valueOf(number);
            String str1 = strNumber.substring(0, 1);
            String str2 = strNumber.substring(1, 2);
            if (str2.equals("0")) {
                return str1 + "k";
            } else {
                return str1 + "." + str2 + "k";
            }
        }

        if (number < 99999) {
            String strNumber = String.valueOf(number);
            String str1 = strNumber.substring(0, 2);
            return str1 + "k";
        }

        if (number < 999999) {
            String strNumber = String.valueOf(number);
            String str1 = strNumber.substring(0, 3);
            return str1 + "k";
        }

        if (number < 9999999) {
            String strNumber = String.valueOf(number);
            String str1 = strNumber.substring(0, 1);
            String str2 = strNumber.substring(1, 2);
            if (str2.equals("0")) {
                return str1 + "m";
            } else {
                return str1 + "." + str2 + "m";
            }
        }

        if (number < 99999999) {
            String strNumber = String.valueOf(number);
            String str1 = strNumber.substring(0, 2);
            return str1 + "m";
        }

        if (number < 999999999) {
            String strNumber = String.valueOf(number);
            String str1 = strNumber.substring(0, 3);
            return str1 + "m";
        }

        NumberFormat formatterHasDigi = new DecimalFormat("###,###,###");
        return formatterHasDigi.format(number);
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
        RuntimeExceptionDao<Portfolio, Integer> dao = dbHelper.getPortfolioRuntimeDao();
//get your Dao
        DeleteBuilder<Portfolio, Integer> deleteBuilder = dao.deleteBuilder();
        try {
            deleteBuilder.where().eq("_id", viewToRemove.getTag());
            deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mAdapter = new PortfolioAdapter(getPortfolio());
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
