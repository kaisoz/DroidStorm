package net.kaisoz.droidstorm.bluetooth;

import java.lang.reflect.Method;
import java.util.Arrays;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.bluetooth.base.BluetoothWrapper;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.bluetooth.legacy.BluetoothWrapperLegacy;
import net.kaisoz.droidstorm.util.IndexedMap;

/**
 * Wrapper for BluetoothManager.c .
 * Manages device discovery, device connection and disconnection. Also, it enables and disables Bluetooth adapter
 *
 * @author Tomás Tormo Franco
 */
public class BluetoothManager {


    private static final String TAG = "BluetoothManager";
    // Map which mantains all connected devices (name/Bluetooth address)
    private IndexedMap mConnDevices = null;
    private boolean mInitialized = false;

    private BluetoothOperations mBluetooth;

    private BluetoothManager() {
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class ConnectionManagerHolder {
        public static final BluetoothManager INSTANCE = new BluetoothManager();
    }

    public static BluetoothManager getInstance() {
        return ConnectionManagerHolder.INSTANCE;
    }

    public BluetoothManager initialize(Context context) throws BluetoothException {
        if (!mInitialized) {
            if (androidVersionIsPreEclair()) {
                mBluetooth = new BluetoothWrapperLegacy(context);
            } else {
                mBluetooth = new BluetoothWrapper(context);
            }
        }
        return ConnectionManagerHolder.INSTANCE;
    }

    private boolean androidVersionIsPreEclair() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR);

    }

    /**
     * Returns a broadcast Connection object
     * This will be used to send a message to all connected robots
     *
     * @return Connection object
     */
    public Connection getConnection() {
        return mBluetooth.getConnection();
    }

    /**
     * Returns a ConnectionLegacy object to the given Bluetooth address
     *
     * @param btAddress Bluetooth address
     * @return Connection object
     */
    public Connection getConnection(String btAddress) {
        return mBluetooth.getConnection(btAddress);
    }

    /**
     * Returns the number of connected robots
     *
     * @return The number of connected robots
     */
    public int getNumDevConnected() {
        if (mConnDevices != null) {
            return mConnDevices.size();
        } else {
            return 0;
        }
    }

    /**
     * Adds successfully connected devices
     *
     * @param devices Succesfully connected devices (name/Bluetooth address)
     */
    public void setConnectedDevices(IndexedMap devices) {
        if (this.mConnDevices == null) {
            this.mConnDevices = devices;
        } else {
            String[] names = Arrays.asList(devices.getAllKeys()).toArray(new String[devices.getAllKeys().length]);
            for (int i = 0; i < names.length; i++) {
                if (!mConnDevices.containsKey(names[i])) {
                    mConnDevices.put(names[i], devices.get(names[i]));
                }
            }
        }
    }

    /**
     * removes successfully connected devices
     *
     * @param devices Names of the devices that have been disconnected
     */
    public void setDisconnected(String[] devices) {
        this.mConnDevices.removeByValues(devices);
    }

    /**
     * Returns an String array with the Bluetooth addresses associated to the given names
     *
     * @param names Names of the devices which addresses are requested
     * @return String array with the Bluetooth addresses
     */
    public String[] getAddressesByNames(String[] names) {
        Object[] addresses = this.mConnDevices.getSubMapFromKeys(names).getAllValues();
        return Arrays.asList(addresses).toArray(new String[addresses.length]);
    }

    /**
     * Returns an new map formed by those pairs name/address which name is in the given names array
     *
     * @param Addr names of the devices which will be included in the map
     * @return IndexedMap with the name/address pairs
     */
    public IndexedMap getMapByNames(String[] names) {
        return mConnDevices.getSubMapFromKeys(names);
    }

    /**
     * Return the names of all connected devices
     *
     * @return
     */
    public String[] getAllNames() {
        Object[] names = this.mConnDevices.getAllKeys();
        return Arrays.asList(names).toArray(new String[names.length]);
    }

    public boolean enable() {
        return mBluetooth.enable();
    }


    public boolean isEnabled() {
        return mBluetooth.isEnabled();
    }

    public IndexedMap connect(String[] btAddresses) {
        return mBluetooth.connect(btAddresses);
    }

    public IndexedMap disconnect(String[] btAddresses) {
        return mBluetooth.disconnect(btAddresses);
    }

    public IndexedMap discoverDevices() throws BluetoothException {
        return mBluetooth.discoverDevices();
    }

    public boolean isConnected() {
        return mBluetooth.isConnected();
    }
}


