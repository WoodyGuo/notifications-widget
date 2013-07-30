package com.roymam.android.notificationswidget;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class EventsHandler extends BroadcastReceiver
{
    private final String WIDGET_LOCKER_UNLOCKED = "com.teslacoilsw.widgetlocker.intent.UNLOCKED";
    public final static String FN_DISMISS_NOTIFICATIONS = "robj.floating.notifications.dismissed";
    public final static String DISMISS_NOTIFICATIONS = "com.roymam.android.nils.remove_notification";
    public final static String OPEN_NOTIFICATION = "com.roymam.android.nils.open_notification";
    public final static String RESEND_ALL_NOTIFICATIONS = "com.roymam.android.nils.resend_all_notifications";
    public static final String ADD_NOTIFICATION = "com.roymam.android.nils.add_notification";

    public void onReceive(Context context, Intent intent)
    {
        if (intent != null)
        {
           NotificationsProvider ns = NotificationsService.getSharedInstance(context);

           String action = intent.getAction();
           if (action.equals(Intent.ACTION_USER_PRESENT) ||
               action.equals(WIDGET_LOCKER_UNLOCKED))
           {
                // clear all notifications if needed
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                if (sharedPref.getBoolean(SettingsActivity.CLEAR_ON_UNLOCK, false))
                {
                    NotificationsService.getSharedInstance(context).clearAllNotifications();
                }
           }
            if (action.equals(DISMISS_NOTIFICATIONS) || action.equals(FN_DISMISS_NOTIFICATIONS))
            {
                String packageName = intent.getStringExtra("package");
                int id = intent.getIntExtra("id",-1);
                if (id > -1)
                {
                    Log.d("NiLS", "remove notification #" + id);
                    if (ns != null) ns.clearNotification(id);
                }
                else
                    if (ns != null) ns.clearNotificationsForApps(new String[]{packageName});
            }
            else if (intent.getAction().equals(RESEND_ALL_NOTIFICATIONS))
            {
                if (ns != null)
                for(NotificationData nd : ns.getNotifications())
                {
                    notifyNotificationAdd(context, nd);
                }
            }
            else if (intent.getAction().equals(OPEN_NOTIFICATION))
            {
                int id = intent.getIntExtra("id",-1);
                if (id > -1 && ns != null)
                {
                    Log.d("NiLS", "open notification #" + id);
                    launchNotificationById(context, ns.getNotifications(), id);
                }
            }
            else if (intent.getAction().equals(NotificationsProvider.ACTION_SERVICE_READY))
            {
                // previous call to NotificationsService.getSharedInstance(context) has already
                // connected to the new service listener
                // notify that the service was started
                if (ns != null && ns.getNotificationEventListener() != null)
                    ns.getNotificationEventListener().onServiceStarted();
            }
            else if (intent.getAction().equals(NotificationsProvider.ACTION_SERVICE_DIED))
            {
                // previous call to NotificationsService.getSharedInstance(context) has already
                // connected to the new service listener
                // notify that the service was stopped
                if (ns != null && ns.getNotificationEventListener() != null)
                    ns.getNotificationEventListener().onServiceStopped();
            }
        }
    }

    // TODO: move those functions to NotificationsService
    private void notifyNotificationAdd(Context context, NotificationData nd)
    {
        Log.d("Nils", "notification add #" + nd.id);

        // send notification to nilsplus
        Intent npsIntent = new Intent();
        npsIntent.setComponent(new ComponentName("com.roymam.android.nilsplus", "com.roymam.android.nilsplus.NPService"));
        npsIntent.setAction(ADD_NOTIFICATION);
        npsIntent.putExtra("title", nd.title);
        npsIntent.putExtra("text", nd.text);
        npsIntent.putExtra("package", nd.packageName);
        npsIntent.putExtra("id", nd.id);

        // convert large icon to byte stream
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        nd.icon.compress(Bitmap.CompressFormat.PNG, 100, stream);
        npsIntent.putExtra("icon", stream.toByteArray());

        // convert large icon to byte stream
        stream = new ByteArrayOutputStream();
        nd.appicon.compress(Bitmap.CompressFormat.PNG, 100, stream);
        npsIntent.putExtra("appicon", stream.toByteArray());

        context.startService(npsIntent);
    }

    private void launchNotificationById(Context context, List<NotificationData> notifications, int id)
    {
        for(int i=0; i< notifications.size(); i++)
        {
            NotificationData nd = notifications.get(i);

            if (nd.id == id)
            {
                nd.launch(context);
            }
        }
    }
}