package com.stockboo.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stockboo.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by narpr05 on 6/28/2015.
 */
public class NewsHeadlineAdapter extends BaseAdapter {

    private final ArrayList<String> mHeadlines;
    private final Context mContext;

    public NewsHeadlineAdapter(Context context, ArrayList<String> headlines) {
        mContext = context;
        mHeadlines = headlines;
    }

    @Override
    public int getCount() {
        return mHeadlines.size();
    }

    @Override
    public Object getItem(int position) {
        return mHeadlines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView !=null )
            return convertView;
        LinearLayout layout = (LinearLayout) ((Activity) mContext).getLayoutInflater().inflate(R.layout.headlines_tv, null);
        TextView tv = (TextView) layout.getChildAt(0);
        tv.setText(mHeadlines.get(position + 2));
        return layout;
    }
}