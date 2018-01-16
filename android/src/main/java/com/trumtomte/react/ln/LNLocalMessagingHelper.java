/**
 * Credits to:
 * https://github.com/zo0r/react-native-push-notification
 */
package com.trumtomte.react.ln;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LNLocalMessagingHelper {

    private static final String PREFERENCES_KEY = "ReactNativeSystemNotification";
    private static final String TAG = LNLocalMessagingHelper.class.getSimpleName();
    private static boolean mIsForeground = false; // tmp

    private Context mContext;
    private SharedPreferences sharedPreferences = null;

    public LNLocalMessagingHelper(Application context) {
        mContext = context;
        sharedPreferences = (SharedPreferences) mContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public String getMainActivityClassName() {
        String packageName = mContext.getPackageName();
        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        return className;
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public void sendNotification(Bundle bundle) {
        new SendNotificationTask(mContext, sharedPreferences, mIsForeground, bundle).execute();
    }

    public void sendNotificationScheduled(Bundle bundle) {
        String intentClassName = getMainActivityClassName();

        if (intentClassName == null) {
            return;
        }

        String notificationId = bundle.getString("id");

        if (notificationId == null) {
            Log.e(TAG, "failed to schedule notification because id is missing");
            return;
        }

        Long fireDate = Math.round(bundle.getDouble("fireDate"));

        if (fireDate == 0) {
            Log.e(TAG, "failed to schedule notification because fire date is missing");
            return;
        }

        Intent notificationIntent = new Intent(mContext, LNLocalMessagingPublisher.class);
        notificationIntent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, notificationId.hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Long interval = null;

        switch (bundle.getString("repeatInterval", "")) {
          case "minute":
              interval = (long) 60000;
              break;
          case "hour":
              interval = AlarmManager.INTERVAL_HOUR;
              break;
          case "day":
              interval = AlarmManager.INTERVAL_DAY;
              break;
          case "week":
              interval = AlarmManager.INTERVAL_DAY * 7;
              break;
        }

        if (interval != null) {
            getAlarmManager().setRepeating(AlarmManager.RTC_WAKEUP, fireDate, interval, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        } else {
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        }

        // Store intent
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            JSONObject json = BundleJSONConverter.convertToJSON(bundle);
            editor.putString(notificationId, json.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void cancelLocalNotification(String notificationId) {
        cancelAlarm(notificationId);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(notificationId);
        editor.apply();
    }

    public void cancelAllLocalNotifications() {
        java.util.Map<String, ?> keyMap = sharedPreferences.getAll();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (java.util.Map.Entry<String, ?> entry:keyMap.entrySet()) {
            cancelAlarm(entry.getKey());
        }

        editor.clear();
        editor.apply();
    }

    public void removeDeliveredNotification(String notificationId) {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId.hashCode());
    }

    public void removeAllDeliveredNotifications() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public ArrayList<Bundle> getScheduledLocalNotifications() {
        ArrayList<Bundle> array = new ArrayList<Bundle>();
        java.util.Map<String, ?> keyMap = sharedPreferences.getAll();

        for (java.util.Map.Entry<String, ?> entry:keyMap.entrySet()) {
            try {
                JSONObject json = new JSONObject((String) entry.getValue());
                Bundle bundle = BundleJSONConverter.convertToBundle(json);
                array.add(bundle);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return array;
    }

    public void cancelAlarm(String notificationId) {
        Intent notificationIntent = new Intent(mContext, LNLocalMessagingPublisher.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext,
                notificationId.hashCode(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        getAlarmManager().cancel(pendingIntent);
    }

    public void setApplicationForeground(boolean foreground) {
        mIsForeground = foreground;
    }
}
