package com.roymam.android.notificationswidget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


@TargetApi(18)
public class NewNotificationsListener extends NotificationListenerService implements NotificationsProvider
{
    private static NotificationsProvider instance = null;
    private NotificationEventListener listener;
    private ArrayList<NotificationData> notifications = new ArrayList<NotificationData>();
    private NotificationParser parser;
    private HashMap<String, PersistentNotification> persistentNotifications = new HashMap<String, PersistentNotification>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        parser = new NotificationParser(getApplicationContext());
        getApplicationContext().sendBroadcast(new Intent(NotificationsProvider.ACTION_SERVICE_READY));
    }

    @Override
    public void onDestroy()
    {
        instance = null;
        getApplicationContext().sendBroadcast(new Intent(NotificationsProvider.ACTION_SERVICE_DIED));
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        Log.d("NiLS","onNotificationPosted");
        if (!parser.isPersistent(sbn.getNotification()))
        {
            NotificationData nd = parser.parseNotification(sbn.getNotification(), sbn.getPackageName(), sbn.getId(), sbn.getTag());
            if (nd != null)
            {
                // remove old notification with the same id
                for(NotificationData oldnd : notifications)
                {
                    if (oldnd.id == sbn.getId())
                    {
                        notifications.remove(oldnd);
                        break;
                    }
                }

                // add the new notification
                notifications.add(nd);

                // notify that the notification was added
                if (listener != null)
                {
                    listener.onNotificationAdded(nd);
                    listener.onNotificationsListChanged();
                }
            }
        }
        else
        {
            PersistentNotification pn = parser.parsePersistentNotification(sbn.getNotification(), sbn.getPackageName(), sbn.getId());
            if (pn != null)
            {
                persistentNotifications.put(sbn.getPackageName(), pn);
                if (listener != null) listener.onPersistentNotificationAdded(pn);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String sync = prefs.getString(SettingsActivity.SYNC_NOTIFICATIONS, SettingsActivity.SYNC_NOTIFICATIONS_TWOWAY);
        if (sync.equals(SettingsActivity.SYNC_NOTIFICATIONS_TWOWAY) ||
            sync.equals(SettingsActivity.SYNC_NOTIFICATIONS_ONEWAY))
        {
            Log.d("NiLS","onNotificationRemoved");
            // find the notification and remove it
            for (NotificationData nd : notifications)
            {
                if (nd.id == sbn.getId() && !nd.pinned)
                {
                    // remove the notification
                    notifications.remove(nd);

                    // notify that the notification was added
                    if (listener != null)
                    {
                        listener.onNotificationCleared(nd);
                        listener.onNotificationsListChanged();
                    }
                    break;
                }
            }
            // remove also persistent notification
            if (parser.isPersistent(sbn.getNotification()))
            {
                if (persistentNotifications.containsKey(sbn.getPackageName()))
                {
                    PersistentNotification pn = persistentNotifications.get(sbn.getPackageName());
                    persistentNotifications.remove(sbn.getPackageName());
                    if (listener != null)
                    {
                        listener.onPersistentNotificationCleared(pn);
                    }
                }
            }
        }
    }

    public static NotificationsProvider getSharedInstance()
    {
        return instance;
    }

    @Override
    public List<NotificationData> getNotifications()
    {
        return notifications;
    }

    @Override
    public HashMap<String, PersistentNotification> getPersistentNotifications()
    {
        return persistentNotifications;
    }

    @Override
    public void clearAllNotifications()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean syncback = prefs.getString(SettingsActivity.SYNC_NOTIFICATIONS, SettingsActivity.SYNC_NOTIFICATIONS_TWOWAY).equals(SettingsActivity.SYNC_NOTIFICATIONS_TWOWAY);

        Iterator<NotificationData> i = notifications.iterator();
        while (i.hasNext())
        {
            NotificationData nd = i.next();
            if (!nd.pinned)
            {
                if (listener != null) listener.onNotificationCleared(nd);
                i.remove();

                // notify android to clear it too
                if (syncback)
                    cancelNotification(nd.packageName, nd.tag, nd.id);
            }
        }

        if (listener != null) listener.onNotificationsListChanged();
    }

    @Override
    public void clearNotification(int notificationId)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean syncback = prefs.getString(SettingsActivity.SYNC_NOTIFICATIONS, SettingsActivity.SYNC_NOTIFICATIONS_TWOWAY).equals(SettingsActivity.SYNC_NOTIFICATIONS_TWOWAY);
        boolean changed = false;
        // first, find it on list
        for(NotificationData nd : notifications)
        {
            if (nd.id == notificationId)
            {
                notifications.remove(nd);
                if (syncback)
                    cancelNotification(nd.packageName, nd.tag, nd.id);
                if (listener != null) listener.onNotificationCleared(nd);
                changed = true;
                break;
            }
        }
        if (changed  && listener != null) listener.onNotificationsListChanged();
    }

    @Override
    public void setNotificationEventListener(NotificationEventListener listener)
    {
        this.listener = listener;
    }

    @Override
    public NotificationEventListener getNotificationEventListener()
    {
        return listener;
    }

    @Override
    public void clearNotificationsForApps(String[] packages)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean syncback = prefs.getString(SettingsActivity.SYNC_NOTIFICATIONS, SettingsActivity.SYNC_NOTIFICATIONS_TWOWAY).equals(SettingsActivity.SYNC_NOTIFICATIONS_TWOWAY);
        boolean changed = false;
        for(String packageName : packages)
        {
            Iterator<NotificationData> i = notifications.iterator();
            while (i.hasNext())
            {
                NotificationData nd = i.next();
                if (!nd.pinned && nd.packageName.equals(packageName))
                {
                    i.remove();
                    if (syncback)
                        cancelNotification(packageName, nd.tag, nd.id);

                    changed = true;
                    if (listener != null) listener.onNotificationCleared(nd);
                }
            }
        }
        if (changed)
        {
            if (listener != null) listener.onNotificationsListChanged();
        }
    }
}
