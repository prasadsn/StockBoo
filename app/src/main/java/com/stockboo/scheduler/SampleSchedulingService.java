package com.stockboo.scheduler;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.stockboo.R;
import com.stockboo.view.MainActivity;
import com.stockboo.view.util.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class SampleSchedulingService extends IntentService {
    public SampleSchedulingService() {
        super("SchedulingService");
    }
    
    public static final String TAG = "Scheduling Demo";
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;
    // The string the app searches for in the Google home page content. If the app finds 
    // the string, it indicates the presence of a doodle.  
    public static final String SEARCH_STRING = "doodle";
    // The Google home page URL from which the app fetches content.
    // You can find a list of other Google domains with possible doodles here:
    // http://en.wikipedia.org/wiki/List_of_Google_domains
    public static final String URL = "http://finance.google.com/finance/info?client=ig&q=INDEXBOM:SENSEX,NSE:NIFTY";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    @Override
    protected void onHandleIntent(Intent intent) {
        // BEGIN_INCLUDE(service_onhandle)
        // The URL from which to fetch content.

        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        boolean isWeekday = ((dow >= Calendar.MONDAY) && (dow <= Calendar.FRIDAY));

        if(!isWeekday ||  cal.get(Calendar.HOUR_OF_DAY) < 9 || cal.get(Calendar.HOUR_OF_DAY) > 15)
            return;

        if(cal.get(Calendar.HOUR_OF_DAY) == 9 && cal.get(Calendar.MINUTE) == 0) {
            sendNotification("Market likely to open  in 15 Minutes", "", true);
            return;
        } else if(cal.get(Calendar.HOUR_OF_DAY) == 9 && cal.get(Calendar.MINUTE) == 15) {
            sendNotification("Market trading session started", "", true);
            return;
        } else if(cal.get(Calendar.HOUR_OF_DAY) == 9 && cal.get(Calendar.MINUTE) == 30) {
        } else if(cal.get(Calendar.HOUR_OF_DAY) == 15 && cal.get(Calendar.MINUTE) == 15) {
            sendNotification("Market Will close in 15 Minutes", "", true);
            return;
        } else if(cal.get(Calendar.HOUR_OF_DAY) == 15 && cal.get(Calendar.MINUTE) == 30) {
            //sendNotification("Market is closed", "", true);
            //return;
        } else if(cal.get(Calendar.MINUTE) > 0){
            return;
        }
        String message = "";
        String title = "";
        boolean stockMesg = intent.getBooleanExtra("stock_update", false);

        if(stockMesg & Utilities.checkInternetConnection(getApplicationContext())) {
            String urlString = URL;

            String response = "";

            // Try to connect to the Google homepage and download content.
            try {
                response = loadFromNetwork(urlString);
            } catch (IOException e) {
                Log.i(TAG, getString(R.string.alert_dialog_no_internet));
            }
            try {
                StringBuffer buffer = new StringBuffer();
                if(response.startsWith("\n// "))
                    response = response.substring(4);
                else if(!response.trim().startsWith("["))
                    response = response.substring(response.indexOf("["));
                JSONArray array = new JSONArray(response);
                JSONObject sensexJsonObj = array.getJSONObject(0);
                double sensexchange = new Double(sensexJsonObj.getString("c")).doubleValue();
                JSONObject niftyJsonObj = array.getJSONObject(1);
                double niftychange = new Double(sensexJsonObj.getString("c")).doubleValue();
                if(isMarketClosed()){
                    title = "Market is closed";
                    if(sensexchange > 0)
                        buffer.append("Sensex is up by " + sensexchange +" Points " );
                    else
                        buffer.append("Sensex is down by " + sensexchange +" Points " );
                    buffer.append(":");
                    if(niftychange > 0)
                        buffer.append(" Nifty is up by " + niftychange +" Points");
                    else
                        buffer.append(" Nifty is down by " + niftychange +" Points");

                } else {
                    if(sensexchange > 30 && niftychange > 10){
                        title = "Market is in Positive mode.";
                        buffer.append("Sensex up by ").append(sensexchange).append(" Points ");
                        buffer.append(": Nifty up by ").append(niftychange).append(" Points");
                    }
                    else if(sensexchange < -30 && niftychange < -10){
                        title = "Market is in Negative mode.";
                        buffer.append("Sensex down by ").append(sensexchange).append(" Points ");
                        buffer.append(": Nifty down by ").append(niftychange).append(" Points");
                    } else {
                        title = "Market is flat";
                        if(sensexchange > 0)
                            buffer.append("Sensex is up by " + sensexchange +" Points " );
                        else
                            buffer.append("Sensex is down by " + sensexchange +" Points " );
                        buffer.append(":");
                        if(niftychange > 0)
                            buffer.append(": Nifty is up by " + niftychange +" Points");
                        else
                            buffer.append(": Nifty is down by " + niftychange +" Points");
                    }

                }
                message = buffer.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else
            message = intent.getStringExtra("message");
        if(message != null && !message.isEmpty())
            sendNotification(title, message, false);
        Log.i(TAG, "No doodle found. :-(");
        // Release the wake lock provided by the BroadcastReceiver.
        SampleAlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }

    private boolean isMarketClosed(){
        Calendar calendar =Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY) == 15 && calendar.get(Calendar.MINUTE) == 30;
    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String title, String msg, boolean sound) {
        mNotificationManager = (NotificationManager)
               this.getSystemService(Context.NOTIFICATION_SERVICE);
    
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);
        if(sound)
            mBuilder.setSound(soundUri);
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

//
// The methods below this line fetch content from the specified URL and return the
// content as a string.
//
    /** Given a URL string, initiate a fetch operation. */
    private String loadFromNetwork(String urlString) throws IOException {
        InputStream stream = null;
        String str ="";
      
        try {
            stream = downloadUrl(urlString);
            str = readIt(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }      
        }
        return str;
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets
     * an input stream.
     * @param urlString A string representation of a URL.
     * @return An InputStream retrieved from a successful HttpURLConnection.
     * @throws IOException
     */
    private InputStream downloadUrl(String urlString) throws IOException {
    
        java.net.URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Start the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    /** 
     * Reads an InputStream and converts it to a String.
     * @param stream InputStream containing HTML from www.google.com.
     * @return String version of InputStream.
     * @throws IOException
     */
    private String readIt(InputStream stream) throws IOException {
      
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        for(String line = reader.readLine(); line != null; line = reader.readLine())
            builder.append(line);
        reader.close();
        return builder.toString();
    }
}
