package com.roymam.android.notificationswidget;

import java.util.StringTokenizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.WindowManager;

public class AppSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener 
{
	public static String EXTRA_PACKAGE_NAME = "com.roymam.android.notificationswidget.packagename";
	public static String IGNORE_APP = "ignoreapp";
	public static String KEEP_ONLY_LAST = "showlast";
	
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
			setTitle(appName + " - " + getString(R.string.app_specific_settings_title));
		} catch (NameNotFoundException e) {
			setTitle(packageName + " - " + getString(R.string.app_specific_settings_title));
		}
		
		super.onCreate(savedInstanceState);

		// add app specific settings
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		
		// Blacklist preference
        CheckBoxPreference ignoreNotificationsPref = new CheckBoxPreference(this);
        ignoreNotificationsPref.setKey(packageName+"."+IGNORE_APP);
        ignoreNotificationsPref.setTitle(R.string.ignore_notifications);
        ignoreNotificationsPref.setSummary(R.string.ignore_notifications_summary);
        //ignoreNotificationsPref.setDisableDependentsState(true);
        root.addPreference(ignoreNotificationsPref);
        
        // Show last preference
        CheckBoxPreference showlastNotificationsPref = new CheckBoxPreference(this);
        showlastNotificationsPref.setKey(packageName+"."+KEEP_ONLY_LAST);
        showlastNotificationsPref.setTitle(R.string.show_only_last_notification);
        showlastNotificationsPref.setSummary(R.string.show_only_last_notification_summary);
        //showlastNotificationsPref.setDependency(packageName+"."+IGNORE_APP);
        root.addPreference(showlastNotificationsPref);
        
        // Clear app specific preferences button
        PreferenceScreen clearPref = getPreferenceManager().createPreferenceScreen(this);
        clearPref.setTitle(R.string.clear_app_settings);
        clearPref.setSummary(R.string.clear_app_summary);
        clearPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
			@Override
			public boolean onPreferenceClick(Preference arg0) 
			{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppSettingsActivity.this);
				String specficApps = prefs.getString(SettingsActivity.APPS_SETTINGS, "");
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
				prefs.edit()
					.remove(packageName+"."+IGNORE_APP)
					.putString(SettingsActivity.APPS_SETTINGS, updatedSpecificApps)
					.commit();
				finish();
				return false;
			}
        	
        });
		//Intent runAppSpecificSettings = new Intent(this, AppSettingsActivity.class);
		//runAppSpecificSettings.putExtra(AppSettingsActivity.EXTRA_PACKAGE_NAME, packageName);
		//intentPref.setIntent(runAppSpecificSettings);
		root.addItemFromInflater(clearPref);
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) 
	{
		if (key.startsWith(packageName))
		{
			String specificApps = sharedPreferences.getString(SettingsActivity.APPS_SETTINGS, "");
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
			
			sharedPreferences.edit().putString("specificapps", specificApps).commit();
		}
	}
	
}