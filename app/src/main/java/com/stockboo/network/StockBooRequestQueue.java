package com.stockboo.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by prasad on 16-05-2015.
 */
public class StockBooRequestQueue {

    private static RequestQueue mRequestQueue;

    public static RequestQueue getRequestQueue(Context context){
        if(mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(context);
        return mRequestQueue;
    }

    public static void cancelRequests(){
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
    }
}
