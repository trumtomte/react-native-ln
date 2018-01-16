import { Platform, NativeModules, NativeEventEmitter } from 'react-native'

class LocalNotification {
    constructor() {
        this.EVENT_RECEIVED = 'LNNotificationReceived'
        this.RNLNMessaging = NativeModules.RNLNMessaging
        this.eventEmitter = new NativeEventEmitter(this.RNLNMessagning || {})
    }

    getInitialNotification() {
        return this.RNLNMessaging.getInitialNotification()
    }

    presentLocalNotification(details) {
        details.id = details.id || (new Date()).getTime().toString()
        details.localNotification = true
        this.RNLNMessaging.presentLocalNotification(details)
    }

    scheduleLocalNotification(details) {
        if (!details.id) {
            throw new Error('id is required for scheduled local notifications')
        }

        details.localNotification = true
        this.RNLNMessaging.scheduleLocalNotification(details)
    }

    getScheduledLocalNotifications() {
        return this.RNLNMessaging.getScheduledLocalNotifications()
    }

    cancelLocalNotification(id) {
        if (!id) {
            return
        }

        this.RNLNMessaging.cancelLocalNotification(id)
    }

    cancelAllLocalNotifications() {
        this.RNLNMessaging.cancelAllLocalNotifications()
    }

    setApplicationIconBadgeNumber(n) {
        this.RNLNMessaging.setBadgeNumber(n)
    }

    getApplicationIconBadgeNumber() {
        return this.RNLNMessaging.getBadgeNumber()
    }

    addListener(callback) {
        return this.eventEmitter.addListener(this.EVENT_RECEIVED, async (data) => {
            try {
                await callback(data)
            } catch (err) {
                console.error('[ERROR] Unable to handle local notification event', err)
                throw err
            }
        })
    }
}

export default new LocalNotification()
