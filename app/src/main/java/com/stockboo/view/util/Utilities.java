package com.stockboo.view.util;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by prsn0001 on 10/24/2015.
 */
public class Utilities {
    public static final boolean checkInternetConnection(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
        // ARE WE CONNECTED TO THE NET
        if (conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {

            return true;
        } else {
            return false;

        }
    }

}
