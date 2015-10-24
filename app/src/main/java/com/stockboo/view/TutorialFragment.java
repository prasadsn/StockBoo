package com.stockboo.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stockboo.R;
import com.stockboo.view.util.PreferenceManager;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TutorialFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TutorialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TutorialFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TutorialFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TutorialFragment newInstance() {
        TutorialFragment fragment = new TutorialFragment();
        return fragment;
    }

    public TutorialFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private class DemoAdapter extends PagerAdapter{

        private int[] icons = new int[]{R.drawable.ic_launcher, R.drawable.demo_dashboard, R.drawable.demo_recommenations, R.drawable.demo_recommendations, R.drawable.demo_watchlist, R.drawable.demo_portfolio, R.drawable.demo_market_news };
        private int[] titles = new int[]{R.string.title_demo_1, R.string.title_demo_2, R.string.title_demo_3, R.string.title_demo_4, R.string.title_demo_5, R.string.title_demo_6, R.string.title_demo_7 };
        private int[] messages = new int[]{R.string.message_demo_1, R.string.message_demo_2, R.string.message_demo_3, R.string.message_demo_4, R.string.message_demo_5, R.string.message_demo_6, R.string.message_demo_7 };
        @Override
        public int getCount() {
            return icons.length;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            RelativeLayout layout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.demo_template, null);
            ImageView icon = (ImageView) layout.getChildAt(0);
            TextView title = (TextView) layout.getChildAt(1);
            TextView detail = (TextView) layout.getChildAt(2);
            icon.setImageResource(icons[position]);
            title.setText(titles[position]);
            detail.setText(messages[position]);
            container.addView(layout);
            return layout;
        }
        @Override
        public void destroyItem(View view, int arg1, Object object) {
            ((ViewPager) view).removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tutorial, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewPager pager = (ViewPager)getActivity().findViewById(R.id.pager);
        pager.setAdapter(new DemoAdapter());

        //Bind the title indicator to the adapter
        CirclePageIndicator titleIndicator = (CirclePageIndicator)getActivity().findViewById(R.id.indicator);
        titleIndicator.setViewPager(pager);
        getActivity().findViewById(R.id.skip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.setDemoWatched(getActivity());
                Intent intent = new Intent(getActivity() ,MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

}
