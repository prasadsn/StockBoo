package com.stockboo.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.stockboo.R;
import com.stockboo.model.Portfolio;
import com.stockboo.model.db.DatabaseHelper;
import com.stockboo.network.StockBooRequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
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
        mAdapter = new PortfolioAdapter(portfolioList);
        mListView.setAdapter(mAdapter);

        LinearLayout totalInvestementlayout = (LinearLayout) view.findViewById(R.id.portfolio_total_investement);
        LinearLayout netWorthlayout = (LinearLayout) view.findViewById(R.id.portfolio_networth);
        LinearLayout totalInvestementlayout = (LinearLayout) view.findViewById(R.id.portfolio_total_investement);

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
            Portfolio portfolio = mList.get(position);
            RelativeLayout layout = (RelativeLayout) convertView;
            TextView scriptName = (TextView) layout.getChildAt(0);
            TextView currentPriceTv = (TextView) layout.getChildAt(1);
            TextView buyingCostTv = (TextView) layout.getChildAt(3);
            TextView investementTv = (TextView) layout.getChildAt(2);
            TextView currentValueTv = (TextView) layout.getChildAt(4);
            currentPriceTv.setText(String.format(getString(R.string.label_portfolio_current_price), portfolio.getCurrentPrice()));
            buyingCostTv.setText(String.format(getString(R.string.label_portfolio_buying_cost), portfolio.getPrice()));
            investementTv.setText(String.format(getString(R.string.label_portfolio_investement), portfolio.getPrice() * portfolio.getQuantity()));
            currentValueTv.setText(String.format(getString(R.string.label_portfolio_current_price), portfolio.getCurrentPrice() * portfolio.getQuantity()));
            scriptName.setText(portfolio.getScriptName() + " (" + portfolio.getQuantity() +" Units)");
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
                    portfolio.setCurrentPrice((int) Float.parseFloat(price));
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

}
