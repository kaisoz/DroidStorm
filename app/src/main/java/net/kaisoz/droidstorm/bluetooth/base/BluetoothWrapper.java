package net.kaisoz.droidstorm.bluetooth.base;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import net.kaisoz.droidstorm.bluetooth.BluetoothOperations;
import net.kaisoz.droidstorm.bluetooth.Connection;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.util.IndexedMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Tomás Tormo Franco
 */

@TargetApi(5)
public class BluetoothWrapper implements BluetoothOperations {

    private long mBluetoothTimeout = 3000000000L; // 3 seconds
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private IndexedMap mFoundDevices;
    private final String TAG = "BluetoothWrapper";

    private final Lock mLock = new ReentrantLock();
    private final Condition mConditionVariable = mLock.newCondition();
    private boolean mDiscoveryHasFinished = false;
    private boolean mBluetoothStateChanged = false;

    private ConnectionDefault mConnection;

    public BluetoothWrapper(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mFoundDevices = new IndexedMap();
        mConnection = new ConnectionDefault();

        prepareBluetoothStateBroadcastReceiver();
        prepareDeviceDiscoveryBroadcastReceiver();
    }

    private void prepareBluetoothStateBroadcastReceiver() {
        BroadcastReceiver bluetoothStateChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int action = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
                mLock.lock();
                try {
                    if (action == BluetoothAdapter.STATE_ON) {
                        Log.d(TAG, "Recieved broadcast intent: Bluetooth enabled");
                    } else if (action == BluetoothAdapter.STATE_OFF) {
                        Log.d(TAG, "Recieved broadcast intent: Bluetooth disabled");
                    }
                } finally {
                    mBluetoothStateChanged = true;
                    mConditionVariable.signal();
                    mLock.unlock();
                }

            }
        };

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(bluetoothStateChangedReceiver, filter);
    }

    private void prepareDeviceDiscoveryBroadcastReceiver() {
        BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                mLock.lock();
                try {
                    if (android.bluetooth.BluetoothDevice.ACTION_FOUND.equals(action)) {
                        android.bluetooth.BluetoothDevice device = intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE);
                        mFoundDevices.put(device.getName(), device.getAddress());
                        Log.d(TAG, "Found new device: " + device.getName());
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        mDiscoveryHasFinished = true;
                        mConditionVariable.signal();
                    }
                } finally {
                    mLock.unlock();
                }
            }
        };

        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(android.bluetooth.BluetoothDevice.ACTION_FOUND);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(deviceFoundReceiver, bluetoothFilter); // Don't forget to unregister during onDestroy

    }

    @Override
    public boolean enable() {
        boolean rtn = true;
        if (!mBluetoothAdapter.isEnabled()) {
            mLock.lock();
            try {
                mBluetoothStateChanged = false;
                launchBluetoothEnableActivity();
                while (!mBluetoothStateChanged && mConditionVariable.awaitNanos(mBluetoothTimeout) >= 0)
                    ;
            } catch (InterruptedException e) {
                rtn = false;
            } finally {
                mLock.unlock();
                if (!mBluetoothAdapter.isEnabled())
                    rtn = false;
            }
        }
        return rtn;
    }

    private void launchBluetoothEnableActivity() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mContext.startActivity(enableBtIntent);
    }

    @Override
    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    @Override
    public IndexedMap connect(String[] btAddresses) {
        IndexedMap connectionResult = new IndexedMap();
        ArrayList<String> success = new ArrayList<String>();
        ArrayList<String> error = new ArrayList<String>();

        for (int i = 0; i < btAddresses.length; i++) {
            try {
                BluetoothPeer peer = new BluetoothPeer(mBluetoothAdapter.getRemoteDevice(btAddresses[i]));
                peer.connect();
                success.add(btAddresses[i]);
                mConnection.addConnectedPeer(peer);
                Log.d(TAG, "Added: " + btAddresses[i]);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Failed: " + btAddresses[i]);
                error.add(btAddresses[i]);
            }
        }

        connectionResult.put("success", success.toArray(new String[success.size()]));
        connectionResult.put("error", error.toArray(new String[error.size()]));
        return connectionResult;
    }

    @Override
    public IndexedMap disconnect(String[] btAddresses) {
        IndexedMap disconnectionResult = new IndexedMap();
        ArrayList<String> success = new ArrayList<String>();
        ArrayList<String> error = new ArrayList<String>();
        BluetoothPeer peer;

        for (int i = 0; i < btAddresses.length; i++) {
            try {
                peer = mConnection.getBluetoothPeer(btAddresses[i]);
                Log.d(TAG, "Added: " + btAddresses[i]);
                peer.disconnect();
                success.add(btAddresses[i]);
                mConnection.removeConnectedPeer(peer);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Failed: " + btAddresses[i]);
                error.add(btAddresses[i]);
            }
        }

        disconnectionResult.put("success", success.toArray(new String[success.size()]));
        disconnectionResult.put("error", error.toArray(new String[error.size()]));
        return disconnectionResult;
    }

    @Override
    public IndexedMap discoverDevices() throws BluetoothException {
        //mBluetoothAdapter.cancelDiscovery();
        mLock.lock();
        try {
            mDiscoveryHasFinished = false;
            if (mBluetoothAdapter.startDiscovery() == true) {
                while (!mDiscoveryHasFinished)
                    mConditionVariable.await();
            } else {
                Log.d(TAG, "Failed Discovering");
            }
        } catch (InterruptedException e) {
        } finally {
            mLock.unlock();
        }
        return mFoundDevices;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    public Connection getConnection() {
        mConnection.setBroadcastMode();
        return mConnection;
    }

    public Connection getConnection(String btAddress) {
        mConnection.setSingleModeToAddress(btAddress);
        return mConnection;
    }
}
