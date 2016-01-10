package com.stockboo.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.stockboo.R;
import com.stockboo.network.StockBooRequestQueue;
import com.stockboo.view.custom.StockBooBoldTextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PastPerformanceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PastPerformanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PastPerformanceFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<ParseObject> mLastMonthList = new ArrayList<ParseObject>();
    private List<ParseObject> m3MonthList = new ArrayList<ParseObject>();
    private List<ParseObject> m6MonthList = new ArrayList<ParseObject>();
    private List<ParseObject> m1YearList = new ArrayList<ParseObject>();

    private PerformanceAdapter mAdapter;

    private ProgressDialog mProgressDialog;
    private OnFragmentInteractionListener mListener;

    private static final int[] PP_SUMMARY_ICONS = new int[]{R.drawable.profitablecalls, R.drawable.loss_calls, R.drawable.total_profitbooked, R.drawable.accuracy};

    private static final String[] PP_SUMMARY_LABEL = new String[]{"Profitable calls", "Loss calls", "Total Profit Booked", "Accuracy"};
    private ListView mListView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PastPerformanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PastPerformanceFragment newInstance(String param1, String param2) {
        PastPerformanceFragment fragment = new PastPerformanceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PastPerformanceFragment() {
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
        View view = inflater.inflate(R.layout.fragment_past_performance, container, false);
        mListView = (ListView) view.findViewById(android.R.id.list);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.past_performance_butons);
        for(int i = 0; i<layout.getChildCount(); i++)
            layout.getChildAt(i).setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgressDialog = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        getData();
    }

    private void getData(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("stock");
        query.include("Advisor");
        Calendar cal = Calendar.getInstance();
        cal.roll(Calendar.YEAR, -1);
        query.whereGreaterThan("updatedAt", new Date(cal.getTimeInMillis()));
        query.whereEqualTo("status", new Integer(1));
        query.addDescendingOrder("updatedAt");
        //ParseUser user = ParseUser.getCurrentUser();
        //query.whereEqualTo("owners", user);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                if (e == null) {
                   List<ParseObject> localList = new ArrayList<ParseObject>();
                    for (ParseObject parseObject : objects) {
                        Date updatedAt = parseObject.getUpdatedAt();
                        long updatedTime = updatedAt.getTime();

                        Calendar cal = Calendar.getInstance();
                        Calendar cal1 = Calendar.getInstance();
                        cal1.setTimeInMillis(updatedTime);
                        cal.roll(Calendar.MONTH, -1);
                        if(cal.get(Calendar.MONTH) == Calendar.DECEMBER)
                            cal.roll(Calendar.YEAR, -1);
                        int equal = cal.compareTo(cal1);
                        if(equal == -1)
                            mLastMonthList.add(parseObject);
                        cal.roll(Calendar.MONTH, -2);
                        if(cal.get(Calendar.MONTH) == Calendar.NOVEMBER || cal.get(Calendar.MONTH) == Calendar.DECEMBER)
                            cal.roll(Calendar.YEAR, -1);
                        equal = cal.compareTo(cal1);
                        if(equal == -1)
                            m3MonthList.add(parseObject);
                        cal.roll(Calendar.MONTH, -3);
                        if(cal.get(Calendar.MONTH) == Calendar.APRIL || cal.get(Calendar.MONTH) == Calendar.MAY || cal.get(Calendar.MONTH) == Calendar.JUNE)
                            cal.roll(Calendar.YEAR, -1);
                        equal = cal.compareTo(cal1);
                        if(equal == -1)
                            m6MonthList.add(parseObject);
                        m1YearList.add(parseObject);
                        localList.add(parseObject);
                    }
                    initiPastPerformanceSummary(mLastMonthList);
                    mAdapter = new PerformanceAdapter(mLastMonthList);
                    mListView.setAdapter(mAdapter);
                    if(mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                } else {
                }

            }
        });

    }
    double truncate(double number, int precision)
    {
        double prec = Math.pow(10, precision);
        int integerPart = (int) number;
        double fractionalPart = number - integerPart;
        fractionalPart *= prec;
        int fractPart = (int) fractionalPart;
        fractionalPart = (double) (integerPart) + (double) (fractPart)/prec;
        return fractionalPart;
    }

    private void initiPastPerformanceSummary(List<ParseObject> list){
        int profitableCalls = 0;
        int lossCalls = 0;
        double accuracy;
        double totalProfit = 0;
        int total_calls = list.size();
        for (ParseObject parseObject : list) {
            String scriptCode = parseObject.get("scriptCode").toString();
            String bookingPrice = parseObject.get("bookingPrice").toString();
            String buyPrice = parseObject.get("buyPrice").toString();
            Date updatedAt = parseObject.getUpdatedAt();
            long updatedTime = updatedAt.getTime();
            long currentTime = System.currentTimeMillis();

            Float profit = new Float(bookingPrice).floatValue() - new Float(buyPrice).floatValue();
            if (profit > 0)
                profitableCalls++;
            else
                lossCalls++;
            totalProfit += (profit / new Double(buyPrice).doubleValue()) * 100;
            totalProfit = truncate(totalProfit, 2);
        }
        accuracy = (profitableCalls * 100) / (double)total_calls;
        accuracy = truncate(accuracy, 2);
        LinearLayout layout4 = (LinearLayout) getActivity().findViewById(R.id.layout1);
        ((StockBooBoldTextView) layout4.getChildAt(1)).setText(new Integer(total_calls).toString());
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.layout2);
        for(int i = 0; i < layout.getChildCount(); i++){
            LinearLayout layout1 = (LinearLayout) layout.getChildAt(i);
            LinearLayout layout2 = (LinearLayout) layout1.getChildAt(0);
            ImageView imageView = (ImageView) layout2.getChildAt(0);
            imageView.setImageResource(PP_SUMMARY_ICONS[i]);
            TextView textView = (TextView) layout2.getChildAt(1);
            textView.setText(PP_SUMMARY_LABEL[i]);
            TextView textView1 = (TextView) layout1.getChildAt(1);
            switch (i){
                case 0:
                    textView1.setText(new Integer(profitableCalls).toString());
                    break;
                case 1:
                    textView1.setText(new Integer(lossCalls).toString());
                    break;
                case 2:
                    textView1.setText(new Double(totalProfit).toString() + " %");
                    break;
                case 3:
                    textView1.setText(new Double(accuracy).toString() + " %");
                    break;
            }
        }
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        LinearLayout layout = (LinearLayout) v.getParent();
        for(int i = 0;i<layout.getChildCount(); i++) {
            ((TextView) layout.getChildAt(i)).setTextColor(getResources().getColor(R.color.stockboo_color_light_blue));
            switch (i){
                case 0:
                    ((TextView) layout.getChildAt(i)).setBackgroundResource(R.drawable.button_left_sided_round_corner_white_fill);
                    break;
                case 1:
                case 2:
                    ((TextView) layout.getChildAt(i)).setBackgroundResource(R.drawable.button_sqare_white_fill);
                    break;
                case 3:
                    ((TextView) layout.getChildAt(i)).setBackgroundResource(R.drawable.button_right_sided_round_corner_white_fill);
                    break;
            }
        }
        switch (v.getId()){
            case R.id.textView21:
                ((TextView)v).setTextColor(Color.WHITE);
                v.setBackgroundResource(R.drawable.button_left_sided_round_corner);
                initiPastPerformanceSummary(mLastMonthList);
                mAdapter.setData(mLastMonthList);
                break;
            case R.id.textView22:
                mAdapter.setData(m3MonthList);
                ((TextView)v).setTextColor(Color.WHITE);
                initiPastPerformanceSummary(m3MonthList);
                v.setBackgroundResource(R.drawable.button_square);
                break;
            case R.id.textView23:
                initiPastPerformanceSummary(m6MonthList);
                mAdapter.setData(m6MonthList);
                ((TextView)v).setTextColor(Color.WHITE);
                v.setBackgroundResource(R.drawable.button_square);
                break;
            case R.id.textView24:
                initiPastPerformanceSummary(m1YearList);
                mAdapter.setData(m1YearList);
                ((TextView)v).setTextColor(Color.WHITE);
                v.setBackgroundResource(R.drawable.button_right_sided_round_corner);
                break;
        }
        int dp = (int)(5 *getResources().getDisplayMetrics().density);
        for(int i = 0;i<layout.getChildCount(); i++) {
            layout.getChildAt(i).setPadding(dp,dp,dp,dp);
        }
        mAdapter.notifyDataSetChanged();
    }
    private class PerformanceAdapter extends BaseAdapter {

        private List<ParseObject> list;

        public PerformanceAdapter(List<ParseObject> list){
            this.list = list;
        }

        public void setData(List<ParseObject> list){
            this.list = list;
        }
        @Override
        public int getCount() {
            return list.size();
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
                convertView = getActivity().getLayoutInflater().inflate(R.layout.past_perfrorm_list_item, null);
            ParseObject parseObject = list.get(position);
            LinearLayout layout = (LinearLayout) convertView;
            String stockId = parseObject.get("stockId").toString();
            String bookingPrice = parseObject.get("bookingPrice").toString();
            String buyPrice = parseObject.get("buyPrice").toString();
            Date createdAt = parseObject.getCreatedAt();
            Date updatedAt = parseObject.getUpdatedAt();

            SimpleDateFormat format = new SimpleDateFormat("dd MMMM");

            Float profit = new Float(bookingPrice).floatValue() - new Float(buyPrice).floatValue();
            double profit_percentage = (profit.floatValue() * 100)/new Float(buyPrice).floatValue();
            profit_percentage = truncate(profit_percentage, 2);
            ((TextView) convertView.findViewById(R.id.title)).setText(stockId);
            ((TextView) convertView.findViewById(R.id.exit_price)).setText(bookingPrice);
            ((TextView) convertView.findViewById(R.id.entry_price)).setText(buyPrice);
            ((TextView) convertView.findViewById(R.id.gain_price)).setText(profit.toString());
            ((TextView) convertView.findViewById(R.id.gain_percentage)).setText(profit_percentage + " %");
            ((TextView) convertView.findViewById(R.id.created_date)).setText(format.format(createdAt));
            ((TextView) convertView.findViewById(R.id.updated_date)).setText(format.format(updatedAt));
            return convertView;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
