package com.stockboo.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import com.stockboo.R;

/**
 * Created by narpr05 on 6/21/2015.
 */
public class NavigationDrawerAdapter extends BaseAdapter {

    private String[] items;
    private Context mContext;
    private int currentPosition;
    private final static int[] NAVIGATION_ITEMS_ICONS = {R.drawable.home_menu, R.drawable.currensuggestion_icon, R.drawable.past_performance, R.drawable.mywathlist_menu_icon, R.drawable.myportfolio_menu, R.drawable.brokeragerecos, R.drawable.marketnews, R.drawable.aboutus};
    public NavigationDrawerAdapter(Context context){
        mContext = context;
        items = context.getResources().getStringArray(R.array.navigation_drawer_items);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setSelectedPosition(int position){
        currentPosition = position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView != null)
            return convertView;
        LinearLayout layout = (LinearLayout) ((Activity)mContext).getLayoutInflater().inflate(R.layout.navigation_drawer_item, null);
        ImageView icon = (ImageView) layout.getChildAt(0);
        TextView title = (TextView) layout.getChildAt(1);
        icon.setImageResource(NAVIGATION_ITEMS_ICONS[position]);
        title.setText(items[position]);
        if(position == currentPosition)
            title.setTextColor(Color.WHITE);
        else
            title.setTextColor(Color.LTGRAY);
        return layout;
    }
}
