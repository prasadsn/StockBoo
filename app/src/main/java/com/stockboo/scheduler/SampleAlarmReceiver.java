package com.stockboo.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;
import java.util.Date;

/**
 * When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent 
 * and then starts the IntentService {@code SampleSchedulingService} to do some work.
 */
public class SampleAlarmReceiver extends WakefulBroadcastReceiver {
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;
  
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, SampleSchedulingService.class);
        boolean stockUpdate = intent.getBooleanExtra("stock_update", false);
        service.putExtra("stock_update", stockUpdate);
        if(intent.getStringExtra("message") != null)
            service.putExtra("message", intent.getStringExtra("message"));
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
        // END_INCLUDE(alarm_onreceive)
    }

    // BEGIN_INCLUDE(set_alarm)
    /**
     * Sets a repeating alarm that runs once a day at approximately 8:30 a.m. When the
     * alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     * @param context
     */
    public void setAlarm(Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        setStockUpdateNotificationAlarm(context);
        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void setStockUpdateNotificationAlarm(Context context){
        Intent intent = new Intent(context, SampleAlarmReceiver.class);
        intent.putExtra("stock_update", true);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        //Calendar calendar = getCalendar(9, 0);
        Calendar calendar = getCalendar();
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
        //alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
        //       Calendar.getInstance().getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
    }
    private Calendar getCalendar(int hour, int minutes){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if(calendar.get(Calendar.HOUR_OF_DAY) > hour)
            calendar.roll(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        return calendar;
    }

    private Calendar getCalendar(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int minute = calendar.get(Calendar.MINUTE);
        if(minute > 0)
            minute = minute + (15 - (minute % 15));
        minute = minute == 60 ? 0 : minute;
        if(minute == 0)
            calendar.roll(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }


    /**
     * Cancels the alarm.
     * @param context
     */
    // BEGIN_INCLUDE(cancel_alarm)
    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).cancel(alarmIntent);

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the 
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
    // END_INCLUDE(cancel_alarm)
}
