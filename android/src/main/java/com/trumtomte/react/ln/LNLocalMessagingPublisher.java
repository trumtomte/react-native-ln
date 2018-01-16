/**
 * Credits to: https://github.com/evollu/react-native-fcm
 */
package com.trumtomte.react.ln;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LNLocalMessagingPublisher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new LNLocalMessagingHelper((Application) context.getApplicationContext()).sendNotification(intent.getExtras());
    }
}
