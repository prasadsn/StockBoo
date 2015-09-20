package com.stockboo.view;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.stockboo.R;

import com.stockboo.network.StockBooRequestQueue;
import com.stockboo.view.dummy.DummyContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class CurrentSuggestionFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static CurrentSuggestionFragment newInstance(String param1, String param2) {
        CurrentSuggestionFragment fragment = new CurrentSuggestionFragment();
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
    public CurrentSuggestionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        //mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
        //        android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        //((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        LinearLayout currentSuggestionHeading = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.stockboo_heading_layout, mListView, false);
        ((ImageView) currentSuggestionHeading.getChildAt(0)).setImageResource(R.drawable.voice_icon);
        ((TextView) currentSuggestionHeading.getChildAt(1)).setText(R.string.heading_current_suggestions);
        ((ListView) mListView).addHeaderView(currentSuggestionHeading);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("stock");
        query.include("Advisor");
        query.whereEqualTo("status", new Integer(0));
        query.addDescendingOrder("createdAt");
        //ParseUser user = ParseUser.getCurrentUser();
        //query.whereEqualTo("owners", user);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                if (e == null) {
                    StringBuffer reqParamBuffer = new StringBuffer();
                    for (ParseObject parseObject : objects) {
                        String scriptCode = parseObject.get("scriptCode").toString();
                        reqParamBuffer.append(scriptCode).append(",");
                    }
                    reqParamBuffer.substring(0, reqParamBuffer.length()-2);
                    Listener listener = new Listener(objects);
                    String request = "http://finance.google.com/finance/info?client=ig&q=" + reqParamBuffer;
                    StringRequest bseNseRequest = new StringRequest(StringRequest.Method.GET, request, listener, listener);
                    StockBooRequestQueue.getRequestQueue(getActivity()).add(bseNseRequest);
               } else {
                }
            }
        });
        //setListAdapter(new SuggestionAdapter());
        return view;
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

    private class SuggestionAdapter implements ListAdapter{

        List<ParseObject> objects;
        JSONArray array;
        private SuggestionAdapter(List<ParseObject> objects, JSONArray array){
            this.objects = objects;
            this.array = array;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public Object getItem(int position) {
            ArrayList<String> list = new ArrayList<>();
            ParseObject object = objects.get(position);
            JSONObject jsonObject = null;
            try {
                jsonObject = array.getJSONObject(position);
                String cpValue = jsonObject.getString("l_fix");
                String cValue = jsonObject.getString("c_fix");
                String c_fixValue = jsonObject.getString("cp_fix");
                list.add(cpValue);
                list.add(cValue + " [ "+ c_fixValue + "% ]");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            list.add(((String)object.get("stockId")).replace("\n", "").trim());
            Date updatedAt = object.getUpdatedAt();
            SimpleDateFormat format = new SimpleDateFormat("dd MMMM");

            Calendar c = Calendar.getInstance();
            c.setTime(updatedAt);
            c.add(Calendar.MONTH, -3);

            Date d = c.getTime();
            String res = format.format(d);
            list.add(res);
            list.add((String) object.get("buyPrice"));
            list.add((String) object.get("targetPrice"));
            list.add((String) object.get("scriptCode"));
            return list;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout layout = null;
            if(convertView !=null )
                layout = (RelativeLayout) convertView;
            else
                layout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.current_suggestion_list_item, null);
            ArrayList<String> list = (ArrayList<String>) getItem(position);
            ((TextView) layout.findViewById(R.id.tvCpValue)).setText(list.get(0));
            ((TextView) layout.findViewById(R.id.tvc_c_fix_value)).setText(list.get(1));
            ((TextView) layout.findViewById(R.id.textView6)).setText(list.get(2));
            ((TextView) layout.findViewById(R.id.tvUpdatedAt)).setText(list.get(3));
            ((TextView) layout.findViewById(R.id.tvrBuyPrice)).setText(list.get(4));
            ((TextView) layout.findViewById(R.id.tvTargetPrice)).setText(list.get(5));
            return layout;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return objects.size();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private class Listener implements Response.Listener<String>, Response.ErrorListener{
        List<ParseObject> objects;

        public Listener(List<ParseObject> objects){
            this.objects = objects;
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
                ((AdapterView<ListAdapter>) mListView).setAdapter(new SuggestionAdapter(objects, array));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArrayList<String> list = (ArrayList<String>) mListView.getAdapter().getItem(position);
        Intent intent = new Intent(getActivity(), StockDetailActivity.class);
        intent.putStringArrayListExtra("param", list);
        getActivity().startActivity(intent);
        /*getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.container, StockDetailFragment.newInstance(list))
                .commit();*/
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

}
