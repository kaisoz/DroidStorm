package net.kaisoz.droidstorm.nxt.handler;

import java.util.Locale;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.bluetooth.BluetoothManager;
import net.kaisoz.droidstorm.bluetooth.Connection;
import net.kaisoz.droidstorm.bluetooth.MessageBuffer;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.connmanager.ConnectionManagerActivity;
import net.kaisoz.droidstorm.nxt.controller.NXTBaseController;
import net.kaisoz.droidstorm.util.DatabaseHelper;
import net.kaisoz.droidstorm.util.DroidStormApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Base class for handlers.
 * Manages preferences change as well as follower mode enabling.
 * When follower mode is selected, it launches the FollowerConfiguratorActivity in order to allow the user select which robot will
 * be the follower and which the leader.
 * It also has a netsted async class which handles all "Leader lost protocol". When a new message is received, this class takes the control
 * of the leader and moves it to make it visible for the follower.
 * Finally, it starts and stops recording mode.
 * This base class must be extended by all handlers in order to use follower mode.
 *
 * @author Tomás Tormo Franco
 */
public abstract class NXTHandlerBaseActivity extends Activity {

    /**
     * Follower mode
     **/
    public static final int MODE_SYNCH = 0;
    public static final int MODE_FOLLOW = 1;
    protected static final int LEADER_SELECTOR_ACTIVITY = 0;

    /**
     * Follower Mode messages
     **/
    protected static final int READY = 0;
    protected static final int INITIALIZING = 1;
    protected static final int LEADER_LOST = 2;
    protected static final int LEADER_SEARCH = 3;
    protected static final int LEADER_FOUND = 4;

    boolean mNXTReady = false;
    public int mMode = MODE_SYNCH;
    protected followerListener mFollowerListener;
    protected NXTBaseController mController;
    protected MessageBuffer mBuffer;
    protected BluetoothManager mConnManager;
    protected SharedPreferences mSp;
    protected DroidStormApp app;
    protected DatabaseHelper dbHelper;
    protected static boolean mRecording = false;
    protected long mTempDemoID = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnManager = BluetoothManager.getInstance();
        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        app = (DroidStormApp) getApplication();
        boolean firstStart = Boolean.valueOf(mSp.getString((String) this.getText(R.string.prf_firstStart_id), "true"));
        if (firstStart) {
            Intent i = new Intent(this, net.kaisoz.droidstorm.PreferencesActivity.class);
            this.startActivity(i);
        }
    }

    /**
     * Called when the activity is resumed and just after it is created.
     * It checks possible configuration changes and in that case, it applies it
     * If the user activated follower mode, it first checks if the phone is connected to two devices and if the IR Emitter port is set
     * In that case, it launches FollowerConfiguratorActivity to select the follower
     * If syncrhonized, or follower mode already enabled and checked,  it enables user interaction
     */
    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        app = (DroidStormApp) getApplication();
        if (app.isLocaleChanged()) {
            app.setLocaleChanged(false);
            refresh();
        }

        if (app.isWheelChanged()) {
            char lWheel = (char) (Integer.valueOf(mSp.getString((String) this.getText(R.string.prf_motorLeft_id), "0")).intValue());
            char rWheel = (char) (Integer.valueOf(mSp.getString((String) this.getText(R.string.prf_motorRight_id), "0")).intValue());
            mController.setWheels(lWheel, rWheel);
            app.setWheelChanged(false);
        }

        if (app.isModeChanged()) {
            int mode = Integer.valueOf(mSp.getString((String) this.getText(R.string.prf_synchMode_id), "0")).intValue();

            if (mode == MODE_FOLLOW) {
                if (mConnManager.getNumDevConnected() > 1) {
                    int irportInt = Integer.valueOf(mSp.getString((String) this.getText(R.string.prf_irPort_id), "-1")).intValue();
                    if (irportInt != -1) {
                        app.setModeChanged(false);
                        Intent i = new Intent(this, net.kaisoz.droidstorm.nxt.follower.FollowerConfiguratorActivity.class);
                        this.startActivityForResult(i, LEADER_SELECTOR_ACTIVITY);
                    } else {
                        Toast.makeText(this, R.string.handler_toast_followmeModeOnNOPORT, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, R.string.handler_toast_followmeModeOnOneDevice, Toast.LENGTH_LONG).show();
                }
            }

            mController.setConnection(mConnManager.getConnection());
            mNXTReady = true;
            mMode = MODE_SYNCH;
            app.setModeChanged(false);
            enableHandler();
        }
        if (mNXTReady)
            enableHandler();
    }

    /**
     * Called when the user changes the activity
     * Stops NXT movement, unregisters listener and, if follower mode is enabled, it turns the IR Emitter off
     * If it's is in recording mode, the record is finished
     */
    @Override
    public void onPause() {
        super.onPause();
        disableHandler();

        if (mNXTReady) {
            try {
                mController.stop();
                if (mRecording) mController.stopRecording();
                if (mMode == MODE_FOLLOW) {
                    mController.turnIREmitterOff();
                    if (mFollowerListener != null) {
                        mFollowerListener.finishListener();
                        mFollowerListener = null;
                    }
                }
            } catch (BluetoothException e) {
                handleException(e);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu_handler, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;
        switch (item.getItemId()) {
            case R.id.connman:
                i = new Intent(this, net.kaisoz.droidstorm.connmanager.ConnectionManagerActivity.class);
                this.startActivity(i);
                break;
            case R.id.preferences:
                i = new Intent(this, net.kaisoz.droidstorm.PreferencesActivity.class);
                this.startActivity(i);
                break;
        }

        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LEADER_SELECTOR_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                mMode = MODE_FOLLOW;
                String leaderAddr = (String) data.getExtras().get("leaderAddr");
                String followerAddr = (String) data.getExtras().get("followerAddr");
                enableFollowerMode(leaderAddr, followerAddr);
            }
        }
    }

    /**
     * Handles a native (Bluetooth) Exception
     *
     * @param ex Exception that has been thrown
     */
    public void handleException(Exception ex) {
        mNXTReady = false;
        disableHandler();
        ex.printStackTrace();
        Intent i = new Intent(this, net.kaisoz.droidstorm.connmanager.ConnectionManagerActivity.class);
        i.setAction(ConnectionManagerActivity.exceptionIntent);
        this.startActivity(i);
    }

    /**
     * Enables follower mode.
     * Creates the follower and leader connection objects and turns the IR on.
     * Also, it starts a listener that will listen to follower messages
     *
     * @param leaderAddr   Leader BT Address
     * @param followerAddr Follower BT Address
     */
    protected void enableFollowerMode(String leaderAddr, String followerAddr) {
        disableHandler();
        //Set the mottor connection to the leader and turn the IREmitter On
        mController.setConnection(mConnManager.getConnection(leaderAddr));
        enableIR();
        // Start the listener thread
        mBuffer = new MessageBuffer();
        runListener(followerAddr);
        mMode = MODE_FOLLOW;
    }

    /**
     * Starts the follower listener
     *
     * @param followerAddr BT Address of the follower robot which the listener will listen to
     */
    protected void runListener(String followerAddr) {
        mNXTReady = true;
        mFollowerListener = new followerListener(this);
        mFollowerListener.execute(mConnManager.getConnection(followerAddr));
    }

    /**
     * Stops the follower listener
     */
    protected void stopListener() {
        mNXTReady = false;
        if (mFollowerListener != null) {
            mFollowerListener.finishListener();
            mFollowerListener = null;
        }
    }

    /**
     * Starts recording mode.
     * It creates the demo in database and enables recording in the controller
     */
    public void startRecording() {
        if (!mRecording) {
            disableHandler();
            dbHelper = new DatabaseHelper(this);
            mTempDemoID = dbHelper.createDemo();
            Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();
            mController.startRecording(dbHelper);
            enableHandler();
            mRecording = true;
        }
    }

    /**
     * Stops recording mode
     */
    public void stopRecording() {
        try {
            disableHandler();
            mController.stop();
            mController.stopRecording();
            mRecording = false;
            nameDialog();
        } catch (BluetoothException ex) {
            handleException(ex);
        }
    }

    /**
     * Shows a dialog to the user in order to give a name to the recorded demo
     */
    protected void nameDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.activity_label_demohandler);
        alert.setMessage(R.string.DemoHandler_setDemoName);

        // Set an EditText view to get user input 
        final EditText input = new EditText(this.getApplicationContext());
        alert.setView(input);

        alert.setPositiveButton(R.string.alert_button_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                dbHelper.setDemoName(mTempDemoID, value.toString());
                mTempDemoID = 0;
                Toast.makeText(NXTHandlerBaseActivity.this, NXTHandlerBaseActivity.this.getText(R.string.activity_label_demohandler) + " \"" + value.toString() + "\" " +
                        NXTHandlerBaseActivity.this.getText(R.string.baseHandler_toast_savedsuccesfully), Toast.LENGTH_LONG).show();
                enableHandler();
            }
        });

        alert.setNegativeButton(R.string.alert_button_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dbHelper.deleteDemo(mTempDemoID);
                mTempDemoID = 0;
                enableHandler();
                return;
            }
        });

        alert.show();
    }

    /**
     * Turns IR Emitter on
     */
    protected void enableIR() {
        int irportInt = Integer.valueOf(mSp.getString((String) this.getText(R.string.prf_irPort_id), "-1")).intValue();
        mController.setIRPort((char) irportInt);
        try {
            mController.turnIREmitterOn();
        } catch (BluetoothException e) {
            handleException(e);
        }
        Toast.makeText(this, this.getText(R.string.handler_toast_followmeModeOn) + mController.getPortString((char) irportInt), Toast.LENGTH_LONG).show();
    }

    /**
     * Re-launches the activity
     */
    protected void refresh() {
        finish();
        Intent myIntent = new Intent(this, this.getClass());
        startActivity(myIntent);
    }

    /**
     * Sets the language
     * Used to change the activity language on runtime
     *
     * @param activityTitleRes New mTitle of the activity
     */
    protected void setLocale(int activityTitleRes) {
        String locale = ((DroidStormApp) getApplication()).getDefaultLocale();
        if (locale != null) {
            Locale newLocale = new Locale(locale);
            Locale.setDefault(newLocale);
            Configuration configLocale = new Configuration();
            configLocale.locale = newLocale;
            getBaseContext().getResources().updateConfiguration(configLocale, null);
            setTitle(activityTitleRes);
        }
    }


    /**
     * Enables user interaction
     *
     * @return
     */
    protected abstract boolean enableHandler();

    /**
     * Disables user interaction
     *
     * @return
     */
    protected abstract boolean disableHandler();

    /**
     * Nested class that will listen to follower messages.
     * First, when it receives a message, it disables the user interaction and processes the message using a MessageBuffer
     * Depending on the message type and the direction received, it moves the leader to make it visible
     * When a READY message is received, it enables user interaction
     * Due to it has to be listening all the time, it is implemented as an AsyncTask, which ensures that the main thread
     * won't get blocked
     *
     * @author root
     */
    protected class followerListener extends AsyncTask<Connection, Boolean, Void> {

        private AlertDialog mAlert = null;
        String mTitle = null;
        String mMsg = null;
        boolean mRunning = false;
        private MessageBuffer mBuffer = null;

        public followerListener(Context ctx) {
            mTitle = (String) NXTHandlerBaseActivity.this.getText(R.string.handler_alert_title_waitForFollower);
            mMsg = (String) NXTHandlerBaseActivity.this.getText(R.string.handler_alert_msg_waitForFollower);
            mRunning = true;
            mBuffer = new MessageBuffer();
            mAlert = new ProgressDialog(ctx);
            mAlert.setTitle(mTitle);
            mAlert.setMessage(mMsg);
        }

        /**
         * Stops the listener
         */
        public void finishListener() {
            mRunning = false;
        }

        @Override
        protected void onProgressUpdate(Boolean... enable) {
            if (enable[0]) {
                if (!mAlert.isShowing())
                    mAlert.show();
            } else {
                mAlert.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Connection... conn) {
            int state;
            char[] msg;
            Connection connection = conn[0];
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            publishProgress(true);

            try {
                while (mRunning) {
                    msg = connection.initBTListener();

                    if (msg != null) {
                        disableHandler();
                        v.vibrate(300);
                        publishProgress(true);
                        mBuffer.setMessage(msg);
                        msg = mBuffer.getMessage();
                        state = (int) msg[0];
                        switch (state) {
                            case LEADER_LOST:
                                mController.stop();
                                break;
                            case LEADER_SEARCH:
                                int direction = (int) msg[1];
                                mController.stop();
                                mController.beVisible(direction);
                                break;
                            case LEADER_FOUND:
                                mController.stop();
                                break;
                            case READY:
                                enableHandler();
                                publishProgress(false);
                                break;
                            default:
                                break;
                        }
                    }
                }
            } catch (BluetoothException ex) {
                handleException(ex);
            }
            return null;
        }
    }
}
