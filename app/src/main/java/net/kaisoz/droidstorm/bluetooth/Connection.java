package net.kaisoz.droidstorm.bluetooth;

import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;

/**
 * @author Tomás Tormo Franco
 */

public interface Connection {

    public char[] initBTListener() throws BluetoothException;

    public void setSingleModeToAddress(String btAdress);

    public void setBroadcastMode();

    public char[] sendCommand(char[] values, boolean response) throws BluetoothException;

    public char[] sendSingleCommand(String btAddr, char[] values, boolean response) throws BluetoothException;

    public char[] broadcastCommand(char[] values, boolean response) throws BluetoothException;

    public char[] waitForMessage(String btAddr) throws BluetoothException;
}
