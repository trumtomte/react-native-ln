/**
 * Credits to: https://github.com/evollu/react-native-fcm
 */
package com.trumtomte.react.ln;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

/**
 * Set alarms for scheduled notification after system reboot.
 */
public class LNSystemBootEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("LNSystemBootReceiver", "Received reboot event");

        LNLocalMessagingHelper helper = new LNLocalMessagingHelper((Application) context.getApplicationContext());
        ArrayList<Bundle> bundles = helper.getScheduledLocalNotifications();

        for (Bundle bundle: bundles) {
            helper.sendNotificationScheduled(bundle);
        }
    }
}
