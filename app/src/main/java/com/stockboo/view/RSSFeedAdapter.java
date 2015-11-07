package com.stockboo.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.stockboo.R;
import com.stockboo.network.StockBooRequestQueue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by prsn0001 on 10/9/2015.
 */
public class RSSFeedAdapter extends CursorAdapter {

    Context mContext;
    public RSSFeedAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        RelativeLayout layout = (RelativeLayout) ((Activity) mContext).getLayoutInflater().inflate(R.layout.market_news_list_item, null);
        return layout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        updateView(view, context, cursor);
    }
    private void updateView(View view, Context context, Cursor cursor){
        RelativeLayout layout = (RelativeLayout) view;
        final String link = cursor.getString(cursor.getColumnIndex("link"));

        if( link!=null && link.contains("news.google.com"))
            layout.getChildAt(1).setVisibility(View.GONE);
        else
            layout.getChildAt(1).setVisibility(View.VISIBLE);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MarketNewsActivity.class);
                intent.putExtra("link", link);
                mContext.startActivity(intent);
            }
        });

        TextView titleView = (TextView) layout.getChildAt(0);
        TextView descriptionView = (TextView) layout.findViewById(R.id.textView11);
        TextView updatedDateView = (TextView) layout.getChildAt(2);
        NetworkImageView imageView = (NetworkImageView) layout.findViewById(R.id.networkImageView);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        Date pubDate = null;
        try {
            pubDate = simpleDateFormat.parse(cursor.getString(cursor.getColumnIndex("pubDate")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(pubDate);
        int difference = (int) (System.currentTimeMillis() - calendar1.getTimeInMillis()) + (5 * 60 * 60 * 1000);
        int days = (int) (difference / (1000*60*60*24));
        int hours = 0;
        hours = (int) ((difference - (1000*60*60*24) * days) / (1000*60*60));
        if(days>1)
            hours = hours % 24;
        String time = days > 1? days + " day ago" : hours + " hours ago";
        titleView.setText(cursor.getString(cursor.getColumnIndex("title")));
        descriptionView.setText(cursor.getString(cursor.getColumnIndex("description")));
        updatedDateView.setText(time);
        imageView.setImageUrl(cursor.getString(cursor.getColumnIndex("thumbnailLink")), StockBooRequestQueue.getInstance(mContext).getImageLoader());
    }
}