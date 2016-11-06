package net.kaisoz.droidstorm.bluetooth.legacy;

import net.kaisoz.droidstorm.bluetooth.ConnectionBase;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;

/**
 * Wrapper for ConnectionLegacy.c file. Sends and retrieves messages to/from the robots and listens to follower
 * messages.
 *
 * @author Tomás Tormo Franco
 */

public class ConnectionLegacy extends ConnectionBase {

    public ConnectionLegacy() {
        super();
    }

    public ConnectionLegacy(String btAddress) {
        super(btAddress);
    }

    public char[] sendSingleCommand(String btAddr, char[] values, boolean response) throws BluetoothException {
        return sendSingleCommandNative(btAddr, values, response);
    }


    public char[] broadcastCommand(char[] values, boolean response) throws BluetoothException {
        return broadcastCommandNative(values, response);
    }

    public char[] waitForMessage(String btAddr) throws BluetoothException {
        return waitForMessageNative(btAddr);
    }

    /**
     * Native functions
     **/
    private native char[] sendSingleCommandNative(String btAddr, char[] values, boolean response) throws BluetoothException;

    private native char[] broadcastCommandNative(char[] values, boolean response) throws BluetoothException;

    private native char[] waitForMessageNative(String response) throws BluetoothException;
}
