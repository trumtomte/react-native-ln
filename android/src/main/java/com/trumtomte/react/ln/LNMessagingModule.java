/**
 * Credits to: https://github.com/evollu/react-native-fcm
 */
package com.trumtomte.react.ln;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


public class LNMessagingModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {

    private final static String TAG = LNMessagingModule.class.getCanonicalName();
    private LNLocalMessagingHelper mLNLocalMessagingHelper;
    private BadgeHelper mBadgeHelper;

    public LNMessagingModule(ReactApplicationContext reactContext) {
        super(reactContext);

        mLNLocalMessagingHelper = new LNLocalMessagingHelper((Application) reactContext.getApplicationContext());
        mBadgeHelper = new BadgeHelper(reactContext.getApplicationContext());

        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);

        registerLocalMessageHandler();
    }

    @Override
    public String getName() {
        // Used when importing via NativeModules @ js
        return "RNLNMessaging";
    }

    @ReactMethod
    public void getInitialNotification(Promise promise) {
        Activity activity = getCurrentActivity();

        if (activity == null) {
            promise.resolve(null);
            return;
        }

        promise.resolve(parseIntent(activity.getIntent()));
    }

    @ReactMethod
    public void presentLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        mLNLocalMessagingHelper.sendNotification(bundle);
    }

    @ReactMethod
    public void scheduleLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        mLNLocalMessagingHelper.sendNotificationScheduled(bundle);
    }

    @ReactMethod
    public void cancelLocalNotification(String notificationID) {
        mLNLocalMessagingHelper.cancelLocalNotification(notificationID);
    }

    @ReactMethod
    public void cancelAllLocalNotifications() {
        mLNLocalMessagingHelper.cancelAllLocalNotifications();
    }

    @ReactMethod
    public void getScheduledLocalNotifications(Promise promise) {
        ArrayList<Bundle> bundles = mLNLocalMessagingHelper.getScheduledLocalNotifications();
        WritableArray array = Arguments.createArray();

        for (Bundle bundle:bundles) {
            array.pushMap(Arguments.fromBundle(bundle));
        }

        promise.resolve(array);
    }

    @ReactMethod
    public void removeDeliveredNotification(String notificationID) {
        mLNLocalMessagingHelper.removeDeliveredNotification(notificationID);
    }

    @ReactMethod
    public void removeAllDeliveredNotifications(){
        mLNLocalMessagingHelper.removeAllDeliveredNotifications();
    }


    @ReactMethod
    public void setBadgeNumber(int badgeNumber) {
        mBadgeHelper.setBadgeCount(badgeNumber);
    }

    @ReactMethod
    public void getBadgeNumber(Promise promise) {
        promise.resolve(mBadgeHelper.getBadgeCount());
    }

    private void sendEvent(String eventName, Object params) {
        getReactApplicationContext()
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    private void registerLocalMessageHandler() {
        IntentFilter intentFilter = new IntentFilter("com.trumtomte.react.ln.ReceiveLocalNotification");

        LocalBroadcastManager.getInstance(getReactApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getReactApplicationContext().hasActiveCatalystInstance()) {
                    sendEvent("LNNotificationReceived", Arguments.fromBundle(intent.getExtras()));
                }
            }
        }, intentFilter);
    }

    private WritableMap parseIntent(Intent intent){
        WritableMap params;
        Bundle extras = intent.getExtras();

        if (extras != null) {
            try {
                params = Arguments.fromBundle(extras);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                params = Arguments.createMap();
            }
        } else {
            params = Arguments.createMap();
        }

        WritableMap ln = Arguments.createMap();
        ln.putString("action", intent.getAction());

        params.putMap("ln", ln);
        params.putInt("openedFromTray", 1);

        return params;
    }

    @Override
    public void onHostResume() {
        mLNLocalMessagingHelper.setApplicationForeground(true);
    }

    @Override
    public void onHostPause() {
        mLNLocalMessagingHelper.setApplicationForeground(false);
    }

    @Override
    public void onHostDestroy() {}

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {}

    @Override
    public void onNewIntent(Intent intent){
        sendEvent("LNNotificationReceived", parseIntent(intent));
    }
}
