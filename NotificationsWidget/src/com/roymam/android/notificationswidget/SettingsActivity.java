package com.roymam.android.notificationswidget;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.roymam.android.common.ListPreferenceChangeListener;
import com.roymam.android.notificationswidget.WizardActivity.AboutDialogFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.BaseAdapter;
import android.widget.RemoteViews;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	public static String TURNSCREENON = "turnscreenon";
	public static String DISABLE_PROXIMITY = "disableproximity";
	public static String DELAYED_SCREEON = "delayed_screenon";
	public static String KEEP_ON_FOREGROUND = "keep_on_foreground";
	public static String CLOCK_STYLE = "clockstyle";
	public static String CLEAR_BUTTON_MODE = "clearbuttonmode";
	public static String SHOW_EDIT_BUTTON= "showeditbutton";
	public static String NOTIFICATION_STYLE="notification_style";
	public static String MAX_LINES="max_lines";
	public static String NOTIFICATION_BG_OPACITY="notification_bg_opacity";
	public static String TEXT_COLOR = "notification_text_color";
	public static String TIME_COLOR = "notification_time_color";
	public static String CLOCK_COLOR = "clock_text_color";
	public static String CLEAR_ON_UNLOCK = "clearonunlock";
	public static String CLEAR_ON_LOCK = "clearonlock";
	public static String COLLECT_ON_UNLOCK = "collectonunlock";
	public static String DISABLE_NOTIFICATION_CLICK = "disable_notification_click";
	public static String CLEAR_ON_CLEAR = "clearonclear";
	public static String CLOCK_SMALL = "small";
	public static String CLOCK_LARGE = "large";
	public static String CLOCK_HIDDEN = "hidden";
	public static String CLOCK_AUTO = "auto";
	public static String APPS_SETTINGS = "specificapps";
	
	public static class PrefsGeneralFragment extends PreferenceFragment 
	{
	    @Override
	    public void onCreate(Bundle savedInstanceState) 
	    {
	        super.onCreate(savedInstanceState);

	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.preferences);
	        
		    // proximity sensor listener
	        Preference proxPref = findPreference(DISABLE_PROXIMITY);	        
	        Boolean currValue = getPreferenceScreen().getSharedPreferences().getBoolean(DISABLE_PROXIMITY, true);	        
	        
	        proxPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
	        {
				@Override
				public boolean onPreferenceChange(final Preference preference,
						Object newValue) 
				{
					Boolean value = (Boolean)newValue;
					if (value == false && Build.MODEL.equals("Nexus 4"))
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
						builder.setTitle(R.string.disableproximity_nexus4_warning_title)
						.setMessage(R.string.disableproximity_nexus4_warning)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() 
			               {
			                   public void onClick(DialogInterface dialog, int id) 
			                   {
			                	   preference.getEditor().putBoolean(SettingsActivity.DISABLE_PROXIMITY, false).commit();
			                	   getActivity().recreate();
			                   }
			               })
						.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() 
			               {
			                   public void onClick(DialogInterface dialog, int id) 
			                   {
			                	   // do nothing
			                   }
			               })
						.show();
						return false;
					}

					return true;
				}
	        	
	        });

	    }
	}	
	
	public static class PrefsAppearanceFragment extends PreferenceFragment 
	{			
	    @Override
	    public void onCreate(Bundle savedInstanceState) 
	    {
	        super.onCreate(savedInstanceState);

	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.appearancepreferences);
	        
	        // set list preferences auto summary
	        // clear mode
	        final Preference clearPref = findPreference(CLEAR_BUTTON_MODE);	        
	        String currValue = getPreferenceScreen().getSharedPreferences().getString(CLEAR_BUTTON_MODE, "visibile");
	        
	        ListPreferenceChangeListener listener = new ListPreferenceChangeListener(
	        		getResources().getStringArray(R.array.settings_buttons_entries),
	        		getResources().getStringArray(R.array.settings_buttons_values));
	        listener.setPrefSummary(clearPref, (String)currValue);
	        clearPref.setOnPreferenceChangeListener(listener);
	        	        
	        // text / time colors
	        listener = new ListPreferenceChangeListener(
	        		getResources().getStringArray(R.array.settings_colors_entries),
	        		getResources().getStringArray(R.array.settings_colors_values));
	        
	        Preference textColorPref = findPreference(TEXT_COLOR);	        
	        currValue = getPreferenceScreen().getSharedPreferences().getString(TEXT_COLOR, "white");	        
	        listener.setPrefSummary(textColorPref, (String)currValue);
	        textColorPref.setOnPreferenceChangeListener(listener);
	        
	        Preference timeColorPref = findPreference(TIME_COLOR);	        
	        currValue = getPreferenceScreen().getSharedPreferences().getString(TIME_COLOR, "blue");	        
	        listener.setPrefSummary(timeColorPref, (String)currValue);
	        timeColorPref.setOnPreferenceChangeListener(listener);
	        
	        Preference clockColorPref = findPreference(CLOCK_COLOR);	        
	        currValue = getPreferenceScreen().getSharedPreferences().getString(CLOCK_COLOR, "white");	        
	        listener.setPrefSummary(clockColorPref, (String)currValue);
	        clockColorPref.setOnPreferenceChangeListener(listener);
	        
	        // clock style
	        listener = new ListPreferenceChangeListener(
	        		getResources().getStringArray(R.array.settings_clock_entries),
	        		getResources().getStringArray(R.array.settings_clock_values));
	        
	        Preference clockPref = findPreference(CLOCK_STYLE);	        
	        currValue = getPreferenceScreen().getSharedPreferences().getString(CLOCK_STYLE, "auto");	        
	        listener.setPrefSummary(clockPref, (String)currValue);
	        clockPref.setOnPreferenceChangeListener(listener);
	        
	        
	     // notification style
	        listener = new ListPreferenceChangeListener(
	        		getResources().getStringArray(R.array.settings_notifications_entries),
	        		getResources().getStringArray(R.array.settings_notifications_values));
	        
	        Preference notPref = findPreference(NOTIFICATION_STYLE);	        
	        currValue = getPreferenceScreen().getSharedPreferences().getString(NOTIFICATION_STYLE, "normal");	        
	        listener.setPrefSummary(notPref, (String)currValue);
	        notPref.setOnPreferenceChangeListener(listener);
	    }
	}
	
	public static class PrefsAdvancedFragment extends PreferenceFragment 
	{
	    @Override
	    public void onCreate(Bundle savedInstanceState) 
	    {
	        super.onCreate(savedInstanceState);

	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.advancedpreferences);
	    }
	}
	
	public static class PrefsAppSpecificFragment extends PreferenceFragment
	{
		@Override
	    public void onCreate(Bundle savedInstanceState) 
	    {
	        super.onCreate(savedInstanceState);

	        // add app specific settings
			PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
			
			// Specfic app list 
			String specificApps = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(APPS_SETTINGS, "");

			for (String packageName : specificApps.split(",")) 
			{		
				if (!packageName.equals(""))
				{				
					PreferenceScreen intentPref = getPreferenceManager().createPreferenceScreen(getActivity());
					Intent runAppSpecificSettings = new Intent(getActivity(), AppSettingsActivity.class);
					runAppSpecificSettings.putExtra(AppSettingsActivity.EXTRA_PACKAGE_NAME, packageName);
					intentPref.setIntent(runAppSpecificSettings);
					
					// get package title
					try 
					{
						ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(packageName, 0);
						String appName = getActivity().getPackageManager().getApplicationLabel(ai).toString();
						if (appName == null) appName = packageName;
						intentPref.setTitle(appName);
						intentPref.setIcon(getActivity().getPackageManager().getApplicationIcon(ai));
					} catch (NameNotFoundException e) 
					{
						intentPref.setTitle(packageName);
					}
			        root.addPreference(intentPref);
				}
				else
				{
					PreferenceCategory noAppsPref =  new PreferenceCategory(getActivity());
					noAppsPref.setTitle(R.string.no_apps);
					root.addPreference(noAppsPref);
				}
			}
	        setPreferenceScreen(root);
	    }
	}
	
	public static class PrefsPersistentNotificationsFragment extends PreferenceFragment
	{
		@Override
	    public void onCreate(Bundle savedInstanceState) 
	    {
	        super.onCreate(savedInstanceState);

	        // add app specific settings
			PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			
			// Persistent notifications list
			NotificationsService ns = NotificationsService.getSharedInstance();
			if (ns != null)
    		{    			
    			Iterator<Entry<String, RemoteViews>> it = ns.getPersistentNotifications().entrySet().iterator();
    			
    			while (it.hasNext())
    			{
    				Entry<String, RemoteViews> e = it.next();
    				final String packageName = e.getKey();    				
    				CheckBoxPreference intentPref = new CheckBoxPreference(getActivity());					
					getPreferenceManager();
					intentPref.setChecked(prefs.getBoolean(packageName + "." + AppSettingsActivity.SHOW_PERSISTENT_NOTIFICATION, false));
					intentPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
					{
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) 
						{
							prefs.edit().putBoolean(packageName + "." + AppSettingsActivity.SHOW_PERSISTENT_NOTIFICATION, (Boolean)newValue).commit();
							AppSettingsActivity.addAppToAppSpecificSettings(packageName, getActivity());
							return true;
						}
						
					});
					/*intentPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference preference) 
						{	
							Intent runAppSpecificSettings = new Intent(getActivity(), AppSettingsActivity.class);
							runAppSpecificSettings.putExtra(AppSettingsActivity.EXTRA_PACKAGE_NAME, packageName);
							getActivity().startActivity(runAppSpecificSettings);
							return true;
						}					
					});*/
					// get package title
					try 
					{
						ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(packageName, 0);
						String appName = getActivity().getPackageManager().getApplicationLabel(ai).toString();
						if (appName == null) appName = packageName;
						intentPref.setTitle(appName);
						intentPref.setIcon(getActivity().getPackageManager().getApplicationIcon(ai));
					} catch (NameNotFoundException e2) 
					{
						intentPref.setTitle(packageName);
					}
			        root.addPreference(intentPref);
    			}	    			    			
			}
	        setPreferenceScreen(root);
	    }
	}
	
	@Override
    public void onBuildHeaders(List<Header> target) 
	{
        loadHeadersFromResource(R.xml.preferences_headers, target);
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
    }
	
	private void showAbout() 
	{	
		DialogFragment dialog = new WizardActivity.AboutDialogFragment();
		//TBD - find a way to show about dialog
		//dialog.show(getFragmentManager(), "AboutDialogFragment");	
	}
	
	// TBD - find a way to show about dialog
	/*
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		final MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.settings_menu, menu);
	    
	    OnMenuItemClickListener menuListener = new OnMenuItemClickListener()
	    {
			@Override
			public boolean onMenuItemClick(MenuItem arg0) 
			{
				if (arg0.getItemId() == R.id.menu_about)
				{
					showAbout();
					return true;
				}
				return false;
			}
	    };
	    for(int i=0;i<menu.size();i++)
	    	menu.getItem(i).setOnMenuItemClickListener(menuListener);
	    
	    return super.onCreateOptionsMenu(menu);
	}
*/
	
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
	public void onSharedPreferenceChanged(final SharedPreferences prefs, String key) 
	{		
		NotificationsService ns = NotificationsService.getSharedInstance();
		if ((key.equals(DISABLE_PROXIMITY) || key.equals(TURNSCREENON)) && ns != null) 
        {
			if (!prefs.getBoolean(SettingsActivity.DISABLE_PROXIMITY, false) &&
				 prefs.getBoolean(SettingsActivity.TURNSCREENON, true))
			{
				ns.startProximityMontior();
			}
			else 
			{
				ns.stopProximityMontior();
			}
        }
		else if (key.equals(KEEP_ON_FOREGROUND))
		{
			if (prefs.getBoolean(SettingsActivity.KEEP_ON_FOREGROUND, false))
			{
				NotificationsService.getSharedInstance().keepOnForeground();
			}
			else
			{
				NotificationsService.getSharedInstance().removeFromForeground();
			}
		}		
	}
}
