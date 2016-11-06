package net.kaisoz.droidstorm.bluetooth;

import net.kaisoz.droidstorm.bluetooth.base.BluetoothWrapper;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;

/**
 * Wrapper for ConnectionLegacy.c file. Sends and retrieves messages to/from the robots and listens to follower
 * messages.
 *
 * @author Tomás Tormo Franco
 */

public abstract class ConnectionBase implements Connection {

    protected static final int MODE_SINGLE = 0;
    protected static final int MODE_BROADCAST = 1;
    protected int mode = MODE_SINGLE;
    protected String mBtAddr;

    /**
     * // Broadcast command constructor
     */
    protected ConnectionBase() {
        mode = MODE_BROADCAST;
    }

    /**
     * Send singe command constructor
     */
    protected ConnectionBase(String btAddr) {
        mode = MODE_SINGLE;
        this.mBtAddr = btAddr;
    }


    /**
     * Sends a command to the robot associated to this connection
     *
     * @param mValues  Command to be sent to the robot
     * @param response If true, a response is required. In other case, false
     * @return Response from the robot (if requested)
     * @throws BluetoothException if the operation fails
     */
    public char[] sendCommand(char[] values, boolean response) throws BluetoothException {
        char[] robotResponse;
        if (mode == MODE_BROADCAST) {
            robotResponse = broadcastCommand(values, response);
        } else {
            robotResponse = sendSingleCommand(mBtAddr, values, response);
        }
        return robotResponse;
    }

    /**
     * Starts a listerner to the robot associated to this connection
     *
     * @return Robot message (if any)
     * @throws BluetoothException if the operation fails
     */
    public char[] initBTListener() throws BluetoothException {
        return waitForMessage(this.mBtAddr);
    }

    @Override
    public void setBroadcastMode() {
        this.mode = MODE_BROADCAST;
    }

    @Override
    public void setSingleModeToAddress(String btAdress) {
        this.mode = MODE_SINGLE;
        this.mBtAddr = btAdress;
    }

    public abstract char[] sendSingleCommand(String btAddr, char[] values, boolean response) throws BluetoothException;

    public abstract char[] broadcastCommand(char[] values, boolean response) throws BluetoothException;

    public abstract char[] waitForMessage(String btAddr) throws BluetoothException;
}
