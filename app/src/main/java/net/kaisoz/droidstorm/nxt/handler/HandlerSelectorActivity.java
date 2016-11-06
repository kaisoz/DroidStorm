package net.kaisoz.droidstorm.nxt.handler;

import java.util.Locale;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.bluetooth.BluetoothManager;
import net.kaisoz.droidstorm.nxt.Interface.MotorInterface;
import net.kaisoz.droidstorm.util.DroidStormApp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * This activity shows the user all available handlers
 *
 * @author Tomás Tormo Franco
 */

public class HandlerSelectorActivity extends Activity {

    private static final int MOVEMENT_HANDLER_ACTIVITY = 1;
    private static final int BUTTON_HANDLER_ACTIVITY = 2;
    private static final int DEMO_HANDLER_ACTIVITY = 3;
    private boolean mExitApp = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale();
        setContentView(R.layout.handler_selector);

        Button movHandler = (Button) findViewById(R.id.main_mov);
        Button buttonHandler = (Button) findViewById(R.id.main_buttons);
        Button demoHandler = (Button) findViewById(R.id.main_demo);

        movHandler.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchHandler(MOVEMENT_HANDLER_ACTIVITY);
            }
        });

        buttonHandler.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchHandler(BUTTON_HANDLER_ACTIVITY);
            }
        });

        demoHandler.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchHandler(DEMO_HANDLER_ACTIVITY);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DroidStormApp app = (DroidStormApp) getApplication();
        if (app.isLocaleChanged()) {
            app.setLocaleChanged(false);
            refresh();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setLocale();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu_global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.connman:
                i = new Intent(HandlerSelectorActivity.this, net.kaisoz.droidstorm.connmanager.ConnectionManagerActivity.class);
                HandlerSelectorActivity.this.startActivity(i);
                break;
            case R.id.preferences:
                i = new Intent(this, net.kaisoz.droidstorm.PreferencesActivity.class);
                this.startActivity(i);
                break;
            case R.id.exit:
                finish();
                mExitApp = true;
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExitApp)
            android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * Sets the connection object and launches the handler
     */
    private void launchHandler(int handler) {
        MotorInterface motor = MotorInterface.getInstance();
        motor.setConnection(BluetoothManager.getInstance().getConnection());

        if (handler == MOVEMENT_HANDLER_ACTIVITY) {
            launchMovementHandler();
        } else {
            if (handler == BUTTON_HANDLER_ACTIVITY) {
                launchButtonHandler();
            } else {
                launchDemoHandler();
            }
        }
    }


    /**
     * Launches the movement handler
     */
    private void launchMovementHandler() {
        Intent i = new Intent(HandlerSelectorActivity.this, net.kaisoz.droidstorm.nxt.handler.MovementHandlerActivity.class);
        HandlerSelectorActivity.this.startActivity(i);
    }

    /**
     * Launches the button handler
     */
    private void launchButtonHandler() {
        Intent i = new Intent(HandlerSelectorActivity.this, net.kaisoz.droidstorm.nxt.handler.ButtonHandlerActivity.class);
        HandlerSelectorActivity.this.startActivity(i);
    }

    /**
     * Launches the button handler
     */
    private void launchDemoHandler() {
        Intent i = new Intent(HandlerSelectorActivity.this, net.kaisoz.droidstorm.nxt.handler.DemoHandlerActivity.class);
        HandlerSelectorActivity.this.startActivity(i);
    }

    /**
     * Re-ĺaunches the whole activity
     */
    private void refresh() {
        finish();
        Intent myIntent = new Intent(this, this.getClass());
        startActivity(myIntent);
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
            setTitle(R.string.activity_label_selhandler);
        }
    }
}