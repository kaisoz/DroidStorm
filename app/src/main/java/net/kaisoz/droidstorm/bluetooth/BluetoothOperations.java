package net.kaisoz.droidstorm.bluetooth;


/**
 * @author Tomás Tormo Franco
 */

import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.util.IndexedMap;

public interface BluetoothOperations {

    public Connection getConnection();

    public Connection getConnection(String btAddress);

    /**
     * Requests for the device to be enabled. Returns true if the request is
     * successful
     */
    public boolean enable();


    /**
     * Returns true if the device is currently enabled.
     */
    public boolean isEnabled();


    /**
     * Connects to a set of Bluetooth MAC addresses
     *
     * @param btAddresses Array of MAC addresses to connect to in String format
     * @return IndexedMap with two entries:
     * * key: "success", value: Array of MAC addresses of the peers successfully connected
     * * key: "error",   value: Array of MAC addresses of the peers which the connection couldn't be established
     */
    public IndexedMap connect(String[] btAddresses);

    /**
     * Disconnects from a set of Bluetooth MAC addresses
     *
     * @param btAddresses Array of MAC addresses to disconnect from in String format
     * @return IndexedMap with two entries:
     * * key: "success", value: Array of MAC addresses of the peers successfully disconnected
     * * key: "error",   value: Array of MAC addresses of the peers which the disconnection failed
     */
    public IndexedMap disconnect(String[] btAddresses);

    /**
     * Initiates the Bluetooth discovery procedure
     *
     * @return IndexedMap with the entries of the discovered devices
     * * key: Device name, value: Device MAC address
     * @throws BluetoothException
     */
    public IndexedMap discoverDevices() throws BluetoothException;


    public boolean isConnected();
}
