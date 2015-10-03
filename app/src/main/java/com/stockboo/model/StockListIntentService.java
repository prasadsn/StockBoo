package com.stockboo.model;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.google.gson.Gson;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.stockboo.model.db.DatabaseHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class StockListIntentService extends IntentService {


    private DatabaseHelper dbHelper;

    public StockListIntentService() {
        super("StockListIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        dbHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        String jsonStr = getJsonString();
        StockList[] startUpData = new Gson().fromJson(jsonStr, StockList[].class);
        RuntimeExceptionDao<StockList, Integer> stockListDao = dbHelper.getStockListDao();
        long recordCount = stockListDao.countOf();
        long length = startUpData.length;
        if(recordCount == length)
            return;
        for(int i = 0;i<length; i++){
            if(!startUpData[i].getStatus().equalsIgnoreCase("Delisted"))
                stockListDao.create(startUpData[i]);
        }

    }

    private String getJsonString(){
        StringBuffer buffer = new StringBuffer();
        try {
            InputStream is = getAssets().open("stocklist.json");
            byte[] data = new byte[1024];
            int bytesRead = 0;
            while((bytesRead = is.read(data)) > 0){
                buffer.append(new String(data, 0, bytesRead));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
