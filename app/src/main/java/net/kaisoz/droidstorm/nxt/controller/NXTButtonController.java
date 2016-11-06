package net.kaisoz.droidstorm.nxt.controller;

import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;

/**
 * Controller used by NXTButtonHandlerActivity.
 * Exposes all the basic robot operations bind to the buttons
 *
 * @author Tomás Tormo Franco
 */
public class NXTButtonController extends NXTBaseController {

    public NXTButtonController() {
        super();
    }

    public NXTButtonController(char lWheel, char rWheel) {
        super(lWheel, rWheel);
    }

    /**
     * Makes the Mindstorm go forwards
     *
     * @throws BluetoothException If the operation fails
     */
    public void goForward() throws BluetoothException {
        move(STRAIGHT_ANGLE, mPower);
    }

    /**
     * Makes the Mindstorm go backwards
     *
     * @throws BluetoothException If the operation fails
     */
    public void goBackwards() throws BluetoothException {
        move(STRAIGHT_ANGLE, mPower * -1);
    }

    /**
     * Makes the Mindstorm turn around left
     *
     * @throws BluetoothException If the operation fails
     */
    public void goLeft() throws BluetoothException {
        move(-100, mPower);
    }

    /**
     * Makes the Mindstorm turn around right
     *
     * @throws BluetoothException If the operation fails
     */
    public void goRight() throws BluetoothException {
        move(100, mPower);
    }

}
