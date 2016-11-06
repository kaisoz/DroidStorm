package net.kaisoz.droidstorm.bluetooth.base;

import net.kaisoz.droidstorm.bluetooth.ConnectionBase;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Tomás Tormo Franco
 */

public class ConnectionDefault extends ConnectionBase {

    private HashMap<String, BluetoothPeer> mConnectedDevices = new HashMap<String, BluetoothPeer>();

    public void addConnectedPeer(BluetoothPeer server) {
        mConnectedDevices.put(server.getAddress(), server);
    }

    public void removeConnectedPeer(BluetoothPeer server) {
        mConnectedDevices.remove(server.getAddress());
    }

    public BluetoothPeer getBluetoothPeer(String btAddress) {
        return mConnectedDevices.get(btAddress);
    }

    public char[] sendSingleCommand(String btAddr, char[] values, boolean response) throws BluetoothException {
        char[] rsp = null;
        BluetoothPeer server = mConnectedDevices.get(btAddr);

        if (server == null)
            throw new BluetoothException();

        server.send(values);
        if (response)
            rsp = server.receive();

        return rsp;
    }


    public char[] broadcastCommand(char[] values, boolean response) throws BluetoothException {
        Iterator it = mConnectedDevices.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            BluetoothPeer peer = (BluetoothPeer) pair.getValue();
            peer.send(values);
        }
        return null;
    }

    public char[] waitForMessage(String btAddr) throws BluetoothException {
        BluetoothPeer peer = mConnectedDevices.get(btAddr);
        if (peer == null)
            throw new BluetoothException();

        return peer.receive();
    }
}
