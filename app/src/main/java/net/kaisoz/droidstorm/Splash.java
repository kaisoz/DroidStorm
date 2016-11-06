package net.kaisoz.droidstorm;

import java.util.Locale;

import net.kaisoz.droidstorm.bluetooth.BluetoothManager;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.util.DroidStormApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Fist activity launched when the application is started
 * Sets application language and enables Bluetooth device if not enabled
 *
 * @author Tomás Tormo Franco
 */
public class Splash extends Activity {

    private static final String TAG = "Splash";
    private static final int OK = 0;
    private static final int BT_NOT_INIT = -1;
    private static final int ERROR = -2;
    private int mSplashTime = 3000;
    private BluetoothManager mManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.splash);
            setDefaultLocale();
            mManager = BluetoothManager.getInstance();
            mManager.initialize(getApplicationContext());
            new enableBTTask().execute();
        } catch (BluetoothException e) {
            killApp(ERROR);
        }
    }

    /**
     * If there is any user configuration, it is used in order to set the application default language.
     * Otherwise, phone language is used
     */
    private void setDefaultLocale() {

        DroidStormApp app = (DroidStormApp) getApplication();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String locale = PreferenceManager.getDefaultSharedPreferences(this).getString((String) this.getText(R.string.prf_locale_id), "en");
        boolean isReady = Boolean.valueOf(sp.getString((String) this.getText(R.string.prf_isReady_id), "false"));

        if (!isReady) {

            boolean isSupported = false;
            String systemLocale = Locale.getDefault().getCountry().toLowerCase();
            String[] supportedLocales = getResources().getStringArray(R.array.localeValues);
            for (int i = 0; i < supportedLocales.length && isSupported == false; i++) {
                if (supportedLocales[i].equals(systemLocale))
                    isSupported = true;
            }
            if (isSupported && locale.equals(systemLocale)) {
                app.setDefaultLocale(systemLocale);
                Log.i(TAG, "App language: " + systemLocale);
            } else {
                app.setDefaultLocale(locale);
                setLocale(locale);
                Log.i(TAG, "App language: " + locale);
            }
        } else {
            app.setDefaultLocale(locale);
            setLocale(locale);
            Log.i(TAG, "App language: " + locale);
        }
    }

    /**
     * Sets the application default language
     *
     * @param locale
     */
    private void setLocale(String locale) {
        Locale newLocale = new Locale(locale);
        Locale.setDefault(newLocale);
        Configuration configLocale = new Configuration();
        configLocale.locale = newLocale;
        getBaseContext().getResources().updateConfiguration(configLocale, null);
    }

    /**
     * kills the whole application in case that there was an error enabling the Bluetooth mAdapter
     *
     * @param rtnCode Return code of the Bluetooth enabling operation
     */
    protected void killApp(int rtnCode) {
        CharSequence msg;
        if (rtnCode == ERROR) {
            msg = this.getText(R.string.splash_alert_msg_errorOcurred);
        } else {
            msg = this.getText(R.string.splash_alert_msg_couldntInitBT);
        }

        AlertDialog.Builder noDevicesDialog = new AlertDialog.Builder(this);
        noDevicesDialog.setPositiveButton(R.string.alert_button_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        noDevicesDialog.setTitle(R.string.bt_alert_title_error).setMessage(msg)
                .create().show();
    }

    /**
     * Nested class used to enable bluetooth mAdapter in background during an informative message is shown
     */
    private class enableBTTask extends AsyncTask<Void, Void, Integer> {
        private AlertDialog alert = null;
        private boolean enable = false;

        protected void onPreExecute() {
            if (!mManager.isEnabled()) {
                enable = true;
                CharSequence title = Splash.this.getText(R.string.splash_alert_title_bluetooth);
                CharSequence msg = Splash.this.getText(R.string.splash_alert_msg_enablingBluetooth);
                Log.i(TAG, "Enabling bluetooth");
                alert = ProgressDialog.show(Splash.this, title, msg, true);
            } else {
                Toast.makeText(Splash.this, R.string.splash_toast_btAlreadyEnabled, Toast.LENGTH_LONG).show();
                Log.i(TAG, "Bluetooth enabled");
            }
        }

        protected Integer doInBackground(Void... voids) {

            if (enable) {
                int btActTimeout = 8000;
                long startTime = System.currentTimeMillis();
                long elapsedTime = 0;

                mManager.enable();
                while (!mManager.isEnabled() && (elapsedTime <= btActTimeout)) {
                    elapsedTime = System.currentTimeMillis() - startTime;
                }
                if (elapsedTime >= btActTimeout) {
                    return BT_NOT_INIT;
                }

            } else {
                try {
                    Thread.sleep(mSplashTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return ERROR;
                }
            }

            return OK;
        }

        protected void onPostExecute(Integer rtnCode) {
            if (enable) alert.dismiss();
            if (rtnCode == 0) {
                finish();
                Log.i(TAG, "Bluetooth enabled successfully");
                startActivity(new Intent(Splash.this, net.kaisoz.droidstorm.connmanager.ConnectionManagerActivity.class));
            } else {
                killApp(rtnCode);
                Log.e(TAG, "Error enabling Bluetooth. Killing application");
            }
        }
    }

}

