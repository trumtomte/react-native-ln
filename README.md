# react-native-ln

Local notifications for Android (React Native). Taken from [react-native-fcm](https://github.com/evollu/react-native-fcm), that only utilizes the local notification part of that project.

## Usage

> This package hasn't been published on NPM (yet)

Download and extract the source into your `node_modules` library. Then `react-native link react-native-ln` should work as expected.

## API

**Note:** This is only for Android, for iOS you'll want to use [PushNotificationIOS](https://facebook.github.io/react-native/docs/pushnotificationios.html).

```
import { Platform, PushNotificationIOS } from 'react-native'
import LocalNotification from 'react-native-ln'

if (Platform.os === 'ios') {
    // Use: PushNotificationIOS
} else {
   // Present a local notification
   LocalNotification.presentLocalNotification({
        id: "",
        title: "",
        body: "",
        number: 1,
        bigText "",
        subText: "",
        color: "red",
        vibrate: 300,
        wakeScreen: true,
        lights: true
   })

    // Schedule a local notification
    LocalNotification.scheduleLocalNotification({
        fireDate: (new Date).getTime(),
        id: "",
        title: "",
        body: "",
        number: 1,
        repeatInterval: "week",
        data: {
            customData: {}
        }
   })

    // Get scheduled local notifications
    LocalNotification.getScheduledLocalNotifications().then(notifications => {
        if (notifications.length > 0) {
            // Do something with the scheduled notifications
        } else {
            // None
        }
    })

    // Add an event listener for receiving local notifications
    // NOTE: this returns a NativeEventEmitter handler
    const handler = LocalNotification.addListener(event => {
        console.log(event)
    })

    // Remove the listener
    handler.remove() 

    // Cancel all local notifications
    LocalNotification.cancelAllLocalNotifications()

    // Cancel a specific local notification
    LocalNotification.cancelLocalNotification("my-custom-id")

    // Set the application icon badge number
    LocalNotification.setApplicationIconBadgeNumber(1)

    // Get the application icon badge number
    LocalNotification.getApplicationIconBadgeNumber().then(n => {
        console.log(n)
    })
}
```

For more usage visit [react-native-fcm](https://github.com/evollu/react-native-fcm).
