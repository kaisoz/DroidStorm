package net.kaisoz.droidstorm.connmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.bluetooth.BluetoothManager;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.util.DroidStormApp;
import net.kaisoz.droidstorm.util.IndexedMap;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

/**
 * This activity manages all the connection and disconnection user interface
 * It shows two lists. The first one holds discovered robots and the other one connected robots.
 * It holds a map with all found robots. Connected robots are hold by BluetoothManager object which this activity uses
 * <p>
 * Also, it has three async classes which perform the following asynchronous tasks:
 * - Discover devices
 * - Connect to devices
 * - Disconnect from devices
 *
 * @author Tomás Tormo Franco
 */
public class ConnectionManagerActivity extends ExpandableListActivity {

    public static final String exceptionIntent = "net.kaisoz.droidstorm.connmanager.EXCEPTION";
    private static final int PREFERENCES_ITEMID = 1;
    private static final int HANDSEL_ITEMID = 2;
    private static final int EXIT_ITEMID = 3;
    private static final String TAG = "ConnManagerActivity";
    private static final int mConnection = 1;
    private static final int mDisconnection = 2;
    private DevicesAdapter mDevicesAdapter;
    private IndexedMap mDevFound;
    private BluetoothManager mManager;
    private Button mConnectButton = null;
    private Button mDisconnectButton = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale();
        mManager = BluetoothManager.getInstance();
        setContentView(R.layout.conn_manager);
        mDevicesAdapter = new DevicesAdapter(this);
        setListAdapter(mDevicesAdapter);

        mConnectButton = (Button) findViewById(R.id.connectButton);
        mDisconnectButton = (Button) findViewById(R.id.disconnect);
        Button searchButton = (Button) findViewById(R.id.search);

        mConnectButton.setEnabled(false);
        mDisconnectButton.setEnabled(false);

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                IndexedMap addresses = mDevFound.getSubMapFromKeys(mDevicesAdapter.getFoundSelected());
                new ConnectTask(addresses).execute();
            }

        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDevicesAdapter.clearFound();
                Toast.makeText(v.getContext().getApplicationContext(), R.string.toast_selectDevices, Toast.LENGTH_LONG).show();
                new SearchTask().execute();
            }
        });

        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                IndexedMap devices = mManager.getMapByNames(mDevicesAdapter.getConnectedSelectedNames());
                new DisconnectTask(devices).execute();
            }
        });

        DroidStormApp app = ((DroidStormApp) getApplication());
        ArrayList data = (ArrayList) app.getValue(TAG + "_adapter_data");

        if (data != null) {
            mDevFound = (IndexedMap) app.getValue(TAG + "_devFound");
            mDevicesAdapter.loadFromCollectableData(data);
            this.getExpandableListView().expandGroup(0);
            this.getExpandableListView().expandGroup(1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DroidStormApp app = (DroidStormApp) getApplication();
        if (app.isLocaleChanged()) {
            app.setLocaleChanged(false);
            refresh();
        }

        Intent intent = getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().equals(exceptionIntent)) {
            AlertDialog.Builder errorDialog = new AlertDialog.Builder(ConnectionManagerActivity.this);
            errorDialog.setPositiveButton(R.string.alert_button_positive, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            errorDialog.setTitle(R.string.bt_alert_title_error).setMessage(R.string.bt_alert_msg_connectionError).create().show();
            String[] devAddr = mManager.getAddressesByNames(mDevicesAdapter.getConnectedNames());
            mManager.disconnect(devAddr);
            onDevicesDisconnected(mDevicesAdapter.getConnectedNames());
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.clear();
        menu.add(0, PREFERENCES_ITEMID, 0, this.getText(R.string.activity_label_preferences));

        if (mManager.isConnected())
            menu.add(0, HANDSEL_ITEMID, 0, this.getText(R.string.activity_label_selhandler));

        menu.add(0, EXIT_ITEMID, 0, this.getText(R.string.optionsMenu_exit));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;
        switch (item.getItemId()) {
            case PREFERENCES_ITEMID:
                i = new Intent(this, net.kaisoz.droidstorm.PreferencesActivity.class);
                this.startActivity(i);
                break;
            case HANDSEL_ITEMID:
                ConnectionManagerActivity.this.startActivity(new Intent(this, net.kaisoz.droidstorm.nxt.handler.HandlerSelectorActivity.class));
                break;
            case EXIT_ITEMID:
                finish();
                break;
        }

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setLocale();
        super.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
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
            setTitle(R.string.activity_label_connman);
        }
    }

    /**
     * Sets found devices
     * Callback called when searching task has finished
     */
    public void onDevicesFound(IndexedMap devices) {
        this.mDevFound = devices;
        Object[] devNames = devices.getAllKeys();
        mDevicesAdapter.setFoundDevices(Arrays.asList(devNames).toArray(new String[devNames.length]));
        refreshView(DevicesAdapter.GROUP_FOUND);
        this.getExpandableListView().invalidateViews();
        if (this.getExpandableListView().isGroupExpanded(0)) {
            this.getExpandableListView().collapseGroup(0);
            this.getExpandableListView().expandGroup(0);
        } else {
            this.getExpandableListView().expandGroup(0);
        }
    }

    @Override
    protected void onPause() {
        DroidStormApp app = ((DroidStormApp) getApplication());
        app.remove(TAG + "_adapter_data");
        app.remove(TAG + "_devFound");
        app.setValue(TAG + "_adapter_data", mDevicesAdapter.getCollectableData());
        app.setValue(TAG + "_devFound", mDevFound);

        super.onPause();
    }

    /**
     * Sets connected devices
     * Callback called when connection task has finished
     */
    public void onDevicesConnected(String[] names) {
        IndexedMap connDevs = mDevFound.getSubMapFromKeys(names);
        mDevFound.removeByKeys(names);
        mManager.setConnectedDevices(connDevs);
        mDevicesAdapter.setConnectedDevices(names);
        refreshView(DevicesAdapter.GROUP_ALL);
    }


    /**
     * Sets disconnected devices
     * Callback called when disconnection task has finished
     */
    public void onDevicesDisconnected(String[] names) {
        mDevicesAdapter.clearConnected(names);
        mManager.setDisconnected(mManager.getAddressesByNames(names));
        refreshView(DevicesAdapter.GROUP_ALL);
    }

    /**
     * Refreshes list view data
     *
     * @param group List view group to be refreshed
     */
    private void refreshView(int group) {
        this.getExpandableListView().invalidateViews();
        if (group == DevicesAdapter.GROUP_FOUND || group == DevicesAdapter.GROUP_ALL) {
            this.getExpandableListView().collapseGroup(DevicesAdapter.GROUP_FOUND);
            this.getExpandableListView().expandGroup(DevicesAdapter.GROUP_FOUND);
        }
        if (group == DevicesAdapter.GROUP_CONNECTED || group == DevicesAdapter.GROUP_ALL) {
            this.getExpandableListView().collapseGroup(DevicesAdapter.GROUP_CONNECTED);
            this.getExpandableListView().expandGroup(DevicesAdapter.GROUP_CONNECTED);
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        String devName = (String) mDevicesAdapter.getChild(groupPosition, childPosition);

        if (groupPosition == DevicesAdapter.GROUP_CONNECTED) {
            if (mDevicesAdapter.getNumFoundSelected() == 0) {
                mConnectButton.setEnabled(false);
                mDevicesAdapter.toggleConnectedDevice(devName);
                if (mDevicesAdapter.getNumConnectedSelected() > 0) {
                    mDisconnectButton.setEnabled(true);
                } else {
                    mDisconnectButton.setEnabled(false);
                }
            } else {
                Toast.makeText(v.getContext().getApplicationContext(), R.string.bt_toast_uncheckAllFound, Toast.LENGTH_LONG).show();
            }
        } else if (groupPosition == DevicesAdapter.GROUP_FOUND) {
            if (mDevicesAdapter.getNumConnectedSelected() == 0) {
                mDisconnectButton.setEnabled(false);
                mDevicesAdapter.toggleFoundDevice(devName);
                if (mDevicesAdapter.getNumFoundSelected() > 0) {
                    mConnectButton.setEnabled(true);
                } else {
                    mConnectButton.setEnabled(false);
                }
            } else {
                Toast.makeText(v.getContext().getApplicationContext(), R.string.bt_toast_uncheckAllConnected, Toast.LENGTH_LONG).show();
            }
        }

        refreshView(DevicesAdapter.GROUP_ALL);
        return true;
    }

    /**
     * Shows an error dialog used to inform the user about fail connections/disconnections
     * This method is called when there was an operation error with some device, but it could be completed with others
     * That's why the other devices names are passed in success array, because, in that case, the successDialog
     * method is also called to show the success dialog
     *
     * @param error   Device names of the devices which couldn't be connected/disconnected
     * @param success Device names of the devices which could be connected/disconnected
     * @param type    Operation type
     */
    private void errorDialog(String[] error, final String[] success, final int type) {
        String title = null;
        String message = null;
        if (type == mConnection) {
            // Error connecting
            mDevicesAdapter.clearFoundSelected();
            title = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_title_error);
            message = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_msg_errorConnecting);
        } else {
            // Error disconnecting
            mDevicesAdapter.clearConnectedSelected();
            title = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_title_error);
            message = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_msg_errorDisconnecting);
        }

        AlertDialog.Builder errorDialog = new AlertDialog.Builder(ConnectionManagerActivity.this);
        errorDialog.setPositiveButton(R.string.alert_button_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (success != null && success.length > 0)
                    successDialog(success, type);
                return;
            }
        });

        for (int i = 0; i < error.length; i++) {
            message += "- " + error[i] + "\n";
        }
        errorDialog.setMessage(message).setTitle(title);
        errorDialog.create().show();
    }

    /**
     * Shows success dialog used to inform the user about connections/disconnectios
     *
     * @param success Device names of the devices which could be connected/disconnected
     * @param type    Operation type
     */
    private void successDialog(String[] success, final int type) {
        String title = null;
        String message = null;
        if (type == mConnection) {
            // Success connecting
            onDevicesConnected(success);
            title = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_title_deviceConnection);
            message = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_msg_connectionSuccess);
        } else {
            // Success disconnecting
            onDevicesDisconnected(success);
            title = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_title_deviceDisconnection);
            message = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_msg_disconnectionSuccess);
        }

        AlertDialog.Builder successDialog = new AlertDialog.Builder(ConnectionManagerActivity.this);
        successDialog.setPositiveButton(R.string.alert_button_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (type == mConnection) {
                    ConnectionManagerActivity.this.startActivity(new Intent(ConnectionManagerActivity.this, net.kaisoz.droidstorm.nxt.handler.HandlerSelectorActivity.class));
                }
                return;
            }
        });

        for (int i = 0; i < success.length; i++) {
            message += "- " + success[i] + "\n";
        }
        successDialog.setMessage(message).setTitle(title);
        successDialog.create().show();
    }


    /***** Async Tasks ****/

    /**
     * Nested class used to search for devices in background
     */
    private class SearchTask extends AsyncTask<Object, Void, Object> {
        private AlertDialog alert = null;
        BluetoothManager manager = mManager;

        protected void onPreExecute() {
            String title = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_title_search);
            String msg = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_msg_searching);
            alert = ProgressDialog.show(ConnectionManagerActivity.this, title, msg, true);
        }

        protected Object doInBackground(Object... devices) {
            try {
                return manager.discoverDevices();
            } catch (BluetoothException e) {
                e.printStackTrace();
                new AlertDialog.Builder(ConnectionManagerActivity.this)
                        .setTitle(R.string.bt_alert_title_error).setMessage(R.string.bt_alert_msg_errorBluetooth).create().show();
                return null;
            }
        }

        protected void onPostExecute(Object result) {
            alert.dismiss();
            if (result != null) {
                IndexedMap convResult = (IndexedMap) result;
                if (convResult.size() == 0) {
                    AlertDialog.Builder noDevicesDialog = new AlertDialog.Builder(ConnectionManagerActivity.this);
                    noDevicesDialog.setPositiveButton(R.string.alert_button_positive, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    noDevicesDialog.setTitle(R.string.bt_alert_title_search).setMessage(R.string.bt_alert_msg_noDevicesFound)
                            .create().show();
                } else {
                    onDevicesFound(convResult);
                }
            }
        }
    }

    /*
    * Nested class used to connect to devices in background
    */
    private class ConnectTask extends AsyncTask<Void, Void, IndexedMap> {
        private AlertDialog alert = null;
        BluetoothManager manager = mManager;
        private IndexedMap devices;

        public ConnectTask(IndexedMap devices) {
            this.devices = devices;
        }

        protected void onPreExecute() {
            String title = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_title_deviceConnection);
            String msg = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_msg_connectingTo);
            alert = ProgressDialog.show(ConnectionManagerActivity.this, title, msg, true);
        }

        protected IndexedMap doInBackground(Void... voids) {
            Object[] addresses = devices.getAllValues();
            return manager.connect(Arrays.asList(addresses).toArray(new String[addresses.length]));
        }

        protected void onPostExecute(IndexedMap result) {
            alert.dismiss();
            Object[] errorNames = null;
            String[] errorAddr = (String[]) result.get("error");
            Object[] successNames = null;
            String[] successAddr = (String[]) result.get("success");

            if (errorAddr != null && errorAddr.length > 0) {
                errorNames = devices.getKeysFromValues(errorAddr);
                successNames = devices.getKeysFromValues(successAddr);
                errorDialog(Arrays.asList(errorNames).toArray(new String[errorNames.length]),
                        Arrays.asList(successNames).toArray(new String[successNames.length]), mConnection);
            } else {
                successNames = devices.getKeysFromValues(successAddr);
                successDialog(Arrays.asList(successNames).toArray(new String[successNames.length]), mConnection);
            }
        }
    }

    /*
     * Nested class used to disconnecto from devices in background
     */
    private class DisconnectTask extends AsyncTask<Void, Void, IndexedMap> {
        private AlertDialog alert = null;
        BluetoothManager manager = mManager;
        private String[] devicesToDisconnect = null;
        private IndexedMap devices = null;

        public DisconnectTask(String[] devices) {
            this.devicesToDisconnect = devices;
        }

        public DisconnectTask(IndexedMap devices) {
            this.devices = devices;
        }

        protected void onPreExecute() {
            String title = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_title_deviceDisconnection);
            String msg = (String) ConnectionManagerActivity.this.getText(R.string.bt_alert_msg_disconnectingFrom);
            alert = ProgressDialog.show(ConnectionManagerActivity.this, title, msg, true);
        }

        protected IndexedMap doInBackground(Void... voids) {
            Object[] addresses = devices.getAllValues();
            return manager.disconnect(Arrays.asList(addresses).toArray(new String[addresses.length]));
        }

        protected void onPostExecute(IndexedMap result) {
            alert.dismiss();
            Object[] errorNames = null;
            String[] errorAddr = (String[]) result.get("error");
            Object[] successNames = null;
            String[] successAddr = (String[]) result.get("success");

            if (errorAddr != null && errorAddr.length > 0) {
                errorNames = devices.getKeysFromValues(errorAddr);
                successNames = devices.getKeysFromValues(successAddr);
                errorDialog(Arrays.asList(errorNames).toArray(new String[errorNames.length]),
                        Arrays.asList(successNames).toArray(new String[successNames.length]), mDisconnection);
            } else {
                successNames = devices.getKeysFromValues(successAddr);
                successDialog(Arrays.asList(successNames).toArray(new String[successNames.length]), mDisconnection);
            }
        }
    }

}
   


