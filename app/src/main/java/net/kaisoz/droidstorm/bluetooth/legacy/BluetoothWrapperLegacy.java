package net.kaisoz.droidstorm.bluetooth.legacy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import net.kaisoz.droidstorm.bluetooth.BluetoothOperations;
import net.kaisoz.droidstorm.bluetooth.Connection;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.util.IndexedMap;

import java.lang.reflect.Method;

/**
 * Wrapper for BluetoothManager.c .
 * Manages device discovery, device connection and disconnection. Also, it enables and disables Bluetooth adapter
 *
 * @author Tomás Tormo Franco
 */
public class BluetoothWrapperLegacy implements BluetoothOperations {


    private static final String TAG = "BluetoothWrapperLegacy";
    private static final String Bluetooth_State_Changed_Action = "android.bluetooth.intent.action.BLUETOOTH_STATE_CHANGED";
    private static final String Bluetooth_State_Extra = "android.bluetooth.intent.action.BLUETOOTH_STATE_CHANGED";
    private static final int sBluetooth_State_ON = 2;
    private static final int sBluetooth_State_OFF = 0;
    private Context mContext;
    private Object mDevice;
    private Method mEnable;
    private Method mDisable;
    private Method mIsEnabled;

    public BluetoothWrapperLegacy(Context context) throws BluetoothException {
        mContext = context;
        mDevice = mContext.getSystemService("bluetooth");

        try {
            if (mDevice != null) {
                loadMethodsFromBluetothSystemService();
            } else {
                throw new BluetoothException();
            }

            prepareBluetoothStateBroadcastReceiver();
            this.loadNativeLibrary();
        } catch (NoSuchMethodException e) {
            throw new BluetoothException();
        }
    }

    private void loadMethodsFromBluetothSystemService() throws NoSuchMethodException {
        // Load Bluetooth methods
        Class<?> c = mDevice.getClass();
        mEnable = c.getMethod("enable");
        mEnable.setAccessible(true);
        mDisable = c.getMethod("disable");
        mDisable.setAccessible(true);
        mIsEnabled = c.getMethod("isEnabled");
        mIsEnabled.setAccessible(true);
    }

    private void prepareBluetoothStateBroadcastReceiver() {
        BroadcastReceiver bluetoothStateChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int action = intent.getExtras().getInt(BluetoothWrapperLegacy.Bluetooth_State_Extra);
                if (action == BluetoothWrapperLegacy.sBluetooth_State_ON) {
                    Log.d(TAG, "Recieved broadcast intent "
                            + "Enable bluetooth");
                } else if (action == BluetoothWrapperLegacy.sBluetooth_State_OFF) {
                    Log.d(TAG, "Recieved broadcast intent "
                            + "Disable bluetooth");
                }

            }
        };
        IntentFilter filter = new IntentFilter(BluetoothWrapperLegacy.Bluetooth_State_Changed_Action);
        mContext.registerReceiver(bluetoothStateChangedReceiver, filter);
    }

    private void loadNativeLibrary() {
        System.loadLibrary("BTCommunication");
    }

    public boolean enable() {
        boolean rtn = false;
        try {
            rtn = (Boolean) mEnable.invoke(mDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }


    public boolean isEnabled() {
        boolean bEnabled = false;
        try {
            bEnabled = (Boolean) mIsEnabled.invoke(mDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bEnabled;
    }

    public Connection getConnection() {
        return new ConnectionLegacy();
    }

    public Connection getConnection(String btAddress) {
        return new ConnectionLegacy(btAddress);
    }

    /**
     * Native functions
     ***/
    public native IndexedMap connect(String[] btAddresses);

    public native IndexedMap disconnect(String[] btAddresses);

    public native IndexedMap discoverDevices() throws BluetoothException;

    public native boolean isConnected();

} 
