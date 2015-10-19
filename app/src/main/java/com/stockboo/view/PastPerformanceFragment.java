package com.stockboo.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private OnFragmentInteractionListener mListener;

    private static final int[] PP_SUMMARY_ICONS = new int[]{R.drawable.profitablecalls, R.drawable.loss_calls, R.drawable.total_profitbooked, R.drawable.accuracy};

    private static final String[] PP_SUMMARY_LABEL = new String[]{"Profitable calls", "Loss calls", "Total Profit Booked", "Accuracy"};
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
        return inflater.inflate(R.layout.fragment_past_performance, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        getData();
    }

    private void getData(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("stock");
        query.include("Advisor");
        query.whereLessThan("updatedAt", new java.sql.Date(System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000));
        query.whereEqualTo("status", new Integer(1));
        query.addDescendingOrder("createdAt");
        //ParseUser user = ParseUser.getCurrentUser();
        //query.whereEqualTo("owners", user);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                if (e == null) {
                    StringBuffer reqParamBuffer = new StringBuffer();
                    int profitableCalls = 0;
                    int lossCalls = 0;
                    double accuracy;
                    double totalProfit = 0;
                    int total_calls = objects.size();
                    for (ParseObject parseObject : objects) {
                        String scriptCode = parseObject.get("scriptCode").toString();
                        String bookingPrice = parseObject.get("bookingPrice").toString();
                        String buyPrice = parseObject.get("buyPrice").toString();
                        Date createdAt = parseObject.getCreatedAt();
                        Date updatedAt = parseObject.getUpdatedAt();
                        Float profit = new Float(bookingPrice).floatValue() - new Float(buyPrice).floatValue();
                        if (profit > 0)
                            profitableCalls++;
                        else
                            lossCalls++;
                        totalProfit += (profit / new Double(buyPrice).doubleValue()) * 100;
                        reqParamBuffer.append(scriptCode).append(",");
                    }
                    accuracy = (profitableCalls * 100) / total_calls;

                    reqParamBuffer.substring(0, reqParamBuffer.length() - 2);
                    initiPastPerformanceSummary(total_calls, profitableCalls, lossCalls, truncate(totalProfit, 2), accuracy);
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

    private void initiPastPerformanceSummary(int total_calls_given, int profitable_calls, int loss_calls, double total_profit_booked, double accuracy){
        LinearLayout layout4 = (LinearLayout) getActivity().findViewById(R.id.layout1);
        ((StockBooBoldTextView) layout4.getChildAt(1)).setText(new Integer(total_calls_given).toString());
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
                    textView1.setText(new Integer(profitable_calls).toString());
                    break;
                case 1:
                    textView1.setText(new Integer(loss_calls).toString());
                    break;
                case 2:
                    textView1.setText(new Float(total_profit_booked).toString() + " %");
                    break;
                case 3:
                    textView1.setText(new Float(accuracy).toString() + " %");
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
                    v.setBackgroundResource(R.drawable.button_left_sided_round_corner_white_fill);
                    break;
                case 1:
                case 2:
                    v.setBackgroundResource(R.drawable.button_sqare_white_fill);
                    break;
                case 3:
                    v.setBackgroundResource(R.drawable.button_right_sided_round_corner_white_fill);
                    break;
            }
        }
        switch (v.getId()){
            case R.id.textView21:
                v.setBackgroundResource(R.drawable.button_left_sided_round_corner);
                break;
            case R.id.textView22:
            case R.id.textView23:
                v.setBackgroundResource(R.drawable.button_square);
                break;
            case R.id.textView24:
                v.setBackgroundResource(R.drawable.button_right_sided_round_corner);
                break;
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
