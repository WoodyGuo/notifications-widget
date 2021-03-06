package com.roymam.android.notificationswidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.WindowManager;

public class PersistentNotificationSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener 
{
	public static final String EXTRA_PACKAGE_NAME = "com.roymam.android.notificationswidget.packagename";
	public static final String USE_EXPANDED_TEXT = "useexpandedtext";
	public static final String SHOW_PERSISTENT_NOTIFICATION = "showpersistent";
	public static final String PERSISTENT_NOTIFICATION_HEIGHT = "persistent_notification_height";
    public static final String CATCH_ALL_NOTIFICATIONS = "catch_all_notifications";
    public static final String HIDE_WHEN_NOTIFICATIONS = "hidewhennotifications";
    public static final String HIDE_CLOCK_WHEN_VISIBLE = "hide_clock_when_visible";
    public static final String PN_TIMEOUT = "pn_timeout";
	public static final String PERSISTENT_APPS = "persistent_apps";
	
	private String packageName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
		
		// get package title
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, 0);
			String appName = getPackageManager().getApplicationLabel(ai).toString();
			if (appName == null) appName = packageName;
			setTitle(appName + " - " + getString(R.string.persistent_notifications));
		} catch (NameNotFoundException e) {
			setTitle(packageName + " - " + getString(R.string.app_specific_settings_title));
		}
		
		super.onCreate(savedInstanceState);

		// add app specific settings
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		
		// Show persistent notifications preference
        CheckBoxPreference showPresistentPref = new CheckBoxPreference(this);
        showPresistentPref.setKey(packageName+"."+SHOW_PERSISTENT_NOTIFICATION);
        showPresistentPref.setTitle(R.string.show_persistent_notifications);        
        showPresistentPref.setDefaultValue(false);
        showPresistentPref.setSummary(R.string.show_persistent_notifications_summary);
        root.addPreference(showPresistentPref); 
        
        // Extract expanded text preference
        CheckBoxPreference useExpandedTextPref = new CheckBoxPreference(this);
        useExpandedTextPref.setKey(packageName+"."+USE_EXPANDED_TEXT);
        useExpandedTextPref.setTitle(R.string.extract_expanded_text);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PersistentNotificationSettingsActivity.this);
		useExpandedTextPref.setDefaultValue(prefs.getBoolean(USE_EXPANDED_TEXT, true));
        useExpandedTextPref.setSummary(R.string.extract_expanded_text_summary);
        root.addPreference(useExpandedTextPref);  
                
        // persistent notification height
        ListPreference persistentHeight = new ListPreference(this);
        persistentHeight.setKey(packageName +"." +PERSISTENT_NOTIFICATION_HEIGHT);
        persistentHeight.setTitle(R.string.notification_height);
        persistentHeight.setDialogTitle(R.string.notification_height);
        persistentHeight.setEntries(R.array.settings_notification_height_entries);
        persistentHeight.setEntryValues(R.array.settings_notification_height_values);
        persistentHeight.setDefaultValue("normal");
        root.addPreference(persistentHeight);

        // treat all notifications as persistent notifications
        CheckBoxPreference catchAllNotificationsPref= new CheckBoxPreference(this);
        catchAllNotificationsPref.setKey(packageName+"."+CATCH_ALL_NOTIFICATIONS);
        catchAllNotificationsPref.setTitle(R.string.catch_all_notifications);
        catchAllNotificationsPref.setDefaultValue(true);
        catchAllNotificationsPref.setSummary(R.string.catch_all_notifications_summary);
        root.addPreference(catchAllNotificationsPref);

        // Hide when notifications appears
        CheckBoxPreference hideWhenNotifications = new CheckBoxPreference(this);
        hideWhenNotifications.setKey(packageName+"."+HIDE_WHEN_NOTIFICATIONS);
        hideWhenNotifications.setTitle(R.string.hide_when_notifications);
        hideWhenNotifications.setDefaultValue(false);
        hideWhenNotifications.setSummary(R.string.hide_when_notifications_summary);
        root.addPreference(hideWhenNotifications);

        // Hide when notifications appears
        CheckBoxPreference hideClockWhenVisible = new CheckBoxPreference(this);
        hideClockWhenVisible.setKey(packageName+"."+HIDE_CLOCK_WHEN_VISIBLE);
        hideClockWhenVisible.setTitle(R.string.hide_clock_when_visible);
        hideClockWhenVisible.setDefaultValue(false);
        hideClockWhenVisible.setSummary(R.string.hide_clock_when_visible_summary);
        root.addPreference(hideClockWhenVisible);

        // persistent notification timeout
        ListPreference persistenttimeout = new ListPreference(this);
        persistenttimeout.setKey(packageName +"." +PN_TIMEOUT);
        persistenttimeout.setTitle(R.string.auto_hide_persistent_notification);
        persistenttimeout.setSummary(R.string.auto_hide_persistent_notification_summary);
        persistenttimeout.setDialogTitle(R.string.auto_hide_persistent_notification);
        persistenttimeout.setEntries(R.array.settings_auto_hide_persistent_notifications_entries);
        persistenttimeout.setEntryValues(R.array.settings_auto_hide_persistent_notifications_values);
        persistenttimeout.setDefaultValue("0");
        root.addPreference(persistenttimeout);
        
        setPreferenceScreen(root);
	}
	
	@Override
	protected void onResume() 
	{
	    super.onResume();	   
	    PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() 
	{
	    super.onPause();
	    PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}
		
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
	{
		if (key.endsWith(SHOW_PERSISTENT_NOTIFICATION))
		{
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, false))
				addAppToPersistentNotifications(packageName, this);
			else
				removeAppFromPersistentNotifications(packageName, this);
		}
	}
	
	public static void removeAppFromPersistentNotifications(String packageName, Context ctx)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		String specficApps = prefs.getString(PersistentNotificationSettingsActivity.PERSISTENT_APPS, "");
		String updatedSpecificApps = "";
		for(String app:specficApps.split(","))
		{
			if (!app.equals(packageName))
			{
				if (updatedSpecificApps.isEmpty())
					updatedSpecificApps = app;
				else
					updatedSpecificApps+=","+app;
			}
		}
		prefs.edit().putString(PersistentNotificationSettingsActivity.PERSISTENT_APPS, updatedSpecificApps).commit();
	}
	
	public static void addAppToPersistentNotifications(String packageName, Context ctx)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	
		String specificApps = prefs.getString(PersistentNotificationSettingsActivity.PERSISTENT_APPS, "");
		boolean hasApp = false;
		for (String token : specificApps.split(",")) 
		{
		     if (token.equals(packageName)) hasApp = true;
		}
		if (!hasApp)
		{
			// add this app to the list of specific apps
			if (!specificApps.equals(""))
				specificApps+= ",";
			specificApps+= packageName; 
		}
		
		prefs.edit().putString(PersistentNotificationSettingsActivity.PERSISTENT_APPS, specificApps).commit();		
	}
}