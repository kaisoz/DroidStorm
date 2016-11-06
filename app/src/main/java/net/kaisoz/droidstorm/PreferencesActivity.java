package net.kaisoz.droidstorm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import net.kaisoz.droidstorm.nxt.handler.NXTHandlerBaseActivity;
import net.kaisoz.droidstorm.util.DroidStormApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

/**
 * Activity that manages the application preferences. Managed preferences are:
 * - Application language
 * - Motor ports - wheels associated
 * - Operation mode: Synchronized or Follower
 * - IR Emitter port (in case of follower mode)
 *
 * @author Tomás Tormo Franco
 */
public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private HashMap<CharSequence, CharSequence> mSummaries;
    private DroidStormApp mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale();
        addPreferencesFromResource(R.xml.preferences);
        mApp = (DroidStormApp) getApplication();

        mSummaries = new HashMap<CharSequence, CharSequence>();
        mSummaries.put(this.getText(R.string.prf_motorRight_id), this.getText(R.string.prf_motorRight_summary));
        mSummaries.put(this.getText(R.string.prf_motorLeft_id), this.getText(R.string.prf_motorLeft_summary));
        mSummaries.put(this.getText(R.string.prf_irPort_id), this.getText(R.string.prf_irPort_summary));
        mSummaries.put(this.getText(R.string.prf_synchMode_id), this.getText(R.string.prf_synchMode_summary));
        mSummaries.put(this.getText(R.string.prf_locale_id), this.getText(R.string.prf_locale_summary));

        ListPreference synch = (ListPreference) findPreference(this.getText(R.string.prf_synchMode_id));
        EditTextPreference firstStart = (EditTextPreference) findPreference(this.getText(R.string.prf_firstStart_id));
        ListPreference irport = (ListPreference) findPreference(this.getText(R.string.prf_irPort_id));

        boolean firstStartVal = Boolean.valueOf(firstStart.getText());
        int synchVal = Integer.valueOf(synch.getValue()).intValue();

        if (firstStartVal) {
            Toast.makeText(this, R.string.toast_selectWheels, Toast.LENGTH_LONG).show();
            ListPreference locale = (ListPreference) findPreference(this.getText(R.string.prf_locale_id));
            locale.setValue(mApp.getDefaultLocale());
            firstStart.setText("false");
        }

        if (synchVal == NXTHandlerBaseActivity.MODE_FOLLOW) {
            Toast.makeText(this, R.string.toast_irEmitterOff, Toast.LENGTH_LONG).show();
            irport.setEnabled(true);
            irport.setSelectable(true);
        } else {
            irport.setEnabled(false);
            irport.setSelectable(false);
        }

        updateSummary(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSummary(null);
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(this.getText(R.string.prf_synchMode_id))) {
            int synchVal = Integer.valueOf(((ListPreference) findPreference(key)).getValue()).intValue();
            ListPreference irport = (ListPreference) findPreference(this.getText(R.string.prf_irPort_id));
            if (synchVal == 1) {
                irport.setEnabled(true);
                irport.setSelectable(true);
            } else {
                irport.setEnabled(false);
                irport.setSelectable(false);
            }
            mApp.setModeChanged(true);
        } else if (key.equals(this.getText(R.string.prf_locale_id))) {
            DroidStormApp app = (DroidStormApp) getApplication();
            app.setDefaultLocale(((ListPreference) findPreference(key)).getValue());
            app.setLocaleChanged(true);
            setLocale();
            refresh();
        } else if (key.equals(this.getText(R.string.prf_motorLeft_id)) || key.equals(this.getText(R.string.prf_motorRight_id))) {
            mApp.setWheelChanged(true);
        }
        updateSummary(key);
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Sets the language
     * Used to change the activity language on runtime
     */
    private void setLocale() {
        String locale = ((DroidStormApp) getApplication()).getDefaultLocale();
        if (locale != null) {
            Locale newLocale = new Locale(locale);
            Locale.setDefault(newLocale);
            Configuration configLocale = new Configuration();
            configLocale.locale = newLocale;
            getBaseContext().getResources().updateConfiguration(configLocale, null);
            setTitle(R.string.activity_label_preferences);
        }
    }

    /**
     * Re-launches the activity
     */
    private void refresh() {
        finish();
        Intent myIntent = new Intent(this, this.getClass());
        startActivity(myIntent);
    }

    /**
     * Updates configuration entries summary on runtime
     *
     * @param key key of the configuration entry which summary has to be changed
     */
    private void updateSummary(String key) {
        String summary = null;
        if (key == null) {
            Iterator<CharSequence> it = mSummaries.keySet().iterator();
            while (it.hasNext()) {
                String preferenceKey = (String) it.next();
                ListPreference portPreference = (ListPreference) findPreference(preferenceKey);
                if (portPreference != null && portPreference.getEntry() != null) {
                    summary = mSummaries.get(preferenceKey) + ": " + portPreference.getEntry();
                    portPreference.setSummary(summary);
                }
            }
        } else {
            ListPreference portPreference = (ListPreference) findPreference(key);
            if (portPreference != null && portPreference.getEntry() != null) {
                summary = mSummaries.get(key) + ": " + portPreference.getEntry();
                portPreference.setSummary(summary);
            }
        }
    }

}
