package com.stockboo.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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
import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StockDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StockDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StockDetailFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private ArrayList<String> mList = null;

    private OnFragmentInteractionListener mListener;
    private ListView mListView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StockDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StockDetailFragment newInstance(ArrayList<String> list) {
        StockDetailFragment fragment = new StockDetailFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_PARAM1, list);
        fragment.setArguments(args);
        return fragment;
    }

    public StockDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mList = getArguments().getStringArrayList(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stock_detail, container, false);
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.stock_detail);
        ((TextView) layout.findViewById(R.id.tvCpValue)).setText(mList.get(0));
        String tvc_c_fix_value = mList.get(1);
        if(tvc_c_fix_value.trim().startsWith("-")){
            ((TextView) layout.findViewById(R.id.tvc_c_fix_value)).setTextColor(Color.RED);
        }
        else
            ((TextView) layout.findViewById(R.id.tvc_c_fix_value)).setTextColor(Color.GREEN);
        ((TextView) layout.findViewById(R.id.tvc_c_fix_value)).setText(tvc_c_fix_value);
        ((TextView) layout.findViewById(R.id.textView6)).setText(mList.get(2));
        ((TextView) layout.findViewById(R.id.tvUpdatedAt)).setText(mList.get(3));
        ((TextView) layout.findViewById(R.id.tvrBuyPrice)).setText(mList.get(4));
        ((TextView) layout.findViewById(R.id.tvTargetPrice)).setText(mList.get(5));
        mListView = (ListView) view.findViewById(R.id.listView2);
        String strUrl = "https://www.google.com/finance/company_news?q=" + mList.get(6) + "&output=json";
        Listener listener = new Listener();
        StringRequest bseNseRequest = new StringRequest(StringRequest.Method.GET, strUrl, listener, listener);
        StockBooRequestQueue.getInstance(getActivity()).getRequestQueue().add(bseNseRequest);
        view.findViewById(R.id.button_messages).setOnClickListener(this);
        view.findViewById(R.id.button_news).setOnClickListener(this);
        return view;
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_messages:
                Button msgButton = (Button) v;
                msgButton.setBackgroundResource(R.drawable.button_left_sided_round_corner_white_fill);
                msgButton.setTextColor(getResources().getColor(R.color.stockboo_color_light_blue));

                Button newsButton = (Button) getActivity().findViewById(R.id.button_news);
                newsButton.setBackgroundResource(R.drawable.button_right_sided_round_corner);
                newsButton.setTextColor(Color.WHITE);
                break;
            case R.id.button_news:
                Button msgButton1 = (Button) getActivity().findViewById(R.id.button_messages);
                msgButton1.setBackgroundResource(R.drawable.button_left_sided_round_corner);
                msgButton1.setTextColor(Color.WHITE);

                Button newsButton1 = (Button) v;
                newsButton1.setBackgroundResource(R.drawable.button_right_sided_round_corner_white_fill);
                newsButton1.setTextColor(getResources().getColor(R.color.stockboo_color_light_blue));
                break;
        }
    }

    private class Listener implements Response.Listener<String>, Response.ErrorListener{

        @Override
        public void onResponse(String response) {
            ArrayList<String> newsTitleList = new ArrayList<>();
            ArrayList<String> newsLinkList = new ArrayList<>();
            try {
                JSONObject object = new JSONObject(response);
                JSONArray array = object.getJSONArray("clusters");
                for(int i = 0; i<array.length(); i++){
                    JSONObject object1 = array.getJSONObject(i);
                    JSONArray array1 = object1.getJSONArray("a");
                    JSONObject object2 = array1.getJSONObject(0);
                    newsTitleList.add((String) object2.get("t"));
                    newsLinkList.add((String) object2.get("u"));
                    Log.d(StockDetailFragment.class.getName(), array1.length()+"");
                }
                Log.d(StockDetailFragment.class.getName(), array.length()+"");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mListView.setAdapter(new NewsAdapter(newsTitleList, newsLinkList));
        }

        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }
    private ArrayList<String> updateMarketNews(String scriptCode) throws IOException, XmlPullParserException {
        //StringBuilder builder=new StringBuilder();
        ArrayList<String> headlines = new ArrayList<String>();
        String strUrl = "https://www.google.com/finance/company_news?q=" + scriptCode + "&output=rss";
        URL url = new URL(strUrl);

        return headlines;
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

    private class NewsAdapter extends BaseAdapter{

        private ArrayList<String> mList;
        private ArrayList<String> mNewsLinkList;

        private NewsAdapter(ArrayList<String> list, ArrayList<String> newsLinkList){
            mList = list;
            mNewsLinkList = newsLinkList;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.headlines_tv, null);
            TextView tv = (TextView) layout.getChildAt(0);
            tv.setText(mList.get(position));
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), MarketNewsActivity.class);
                    intent.putExtra("link", mNewsLinkList.get(position));
                    startActivity(intent);
                }
            });
            return layout;
        }
    }
}
