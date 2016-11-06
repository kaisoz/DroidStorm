package net.kaisoz.droidstorm.nxt.Interface;

import net.kaisoz.droidstorm.bluetooth.Connection;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.nxt.message.GenericResponse;
import net.kaisoz.droidstorm.nxt.message.MotorMessage;
import net.kaisoz.droidstorm.nxt.message.motorStateResponse;

/**
 * Exposes motor commands methods, such as setOutputState (the one used to send a command to a motor) or
 * getMotorState (used to get the state of a motor)
 *
 * @author Tomás Tormo Franco
 */
public class MotorInterface extends NXTInterface {

    private static char mWheel1 = 0x00;
    private static char mWheel2 = 0x00;
    private static boolean mWheelsReady = false;

    private MotorInterface() {
    }

    private Connection connect = null;

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class MotorHolder {
        public static final MotorInterface INSTANCE = new MotorInterface();
    }

    /**
     * Returns an instance of a MotorInterface
     *
     * @return
     */
    public static MotorInterface getInstance() {
        return MotorHolder.INSTANCE;
    }

    /**
     * Sets a Bluetooth connection for this interface
     *
     * @param connection
     */
    public void setConnection(Connection connection) {
        connect = connection;
    }

    /**
     * Sets the motor port for a given wheel
     *
     * @param motor The motor port to set. Could be A, B or C
     * @param wheel The wheel to set
     */
    public void setWheel(String motor, int wheel) {
        char chosenMotor = NO_MOTOR;
        if (motor.equals("A")) {
            chosenMotor = MOTOR_A;
        } else if (motor.equals("B")) {
            chosenMotor = MOTOR_B;
        } else if (motor.equals("C")) {
            chosenMotor = MOTOR_C;
        }

        if (wheel == LEFT_MOTOR)
            mWheel1 = chosenMotor;
        else if (wheel == RIGHT_MOTOR)
            mWheel2 = chosenMotor;

        if (mWheel1 != NO_MOTOR && mWheel2 != NO_MOTOR)
            mWheelsReady = true;
    }

    /**
     * Returns true if both wheels are associated to a motor port
     *
     * @return
     */
    public boolean areWheelsReady() {
        return mWheelsReady;
    }

    /**
     * Unsets both wheels
     */
    public void resetWheels() {
        mWheel1 = 0x00;
        mWheel2 = 0x00;
        mWheelsReady = false;
    }

    /**
     * Gets the motor port associated to the given wheel
     *
     * @param wheel
     * @return The motor port
     */
    public char getWheel(int wheel) {
        if (wheel == LEFT_MOTOR)
            return mWheel1;
        else if (wheel == RIGHT_MOTOR)
            return mWheel2;
        else
            return NO_MOTOR;
    }

    /**
     * Sends a command to the robot/s
     *
     * @param message Message to send
     * @return
     * @throws BluetoothException if the operation fails
     */
    public GenericResponse setOutputState(MotorMessage message) throws BluetoothException {
        COMMAND_BYTE = 0x0004;
        message.setCommandByte(COMMAND_BYTE);

        if (message.getMessageType() == MESSAGETYPE_NORESPONSE) {
            connect.sendCommand(message.getCommandValues(), false);
            return null;
        } else {
            char[] response = connect.sendCommand(message.getCommandValues(), true);
            return new GenericResponse(response);
        }
    }

    /**
     * Resets motor internal counter
     *
     * @param motor       Motor port to reset
     * @param isrelative  if the reset should be relative to the last movement or absolute
     * @param messageType If a response should be expected from the robot or not (RESPONSE or NO_RESPONSE)
     * @return
     * @throws BluetoothException if the operation fails
     */
    public GenericResponse resetMotorPosition(char motor, boolean isrelative, char messageType) throws BluetoothException {
        char[] message = new char[2];
        message[0] = (char) (messageType | 0x0A);

        if (!isrelative) {
            message[1] = motor;
        } else {
            message[1] = (char) (0x0001 | motor);
        }

        if (messageType == MESSAGETYPE_NORESPONSE) {
            connect.sendCommand(message, false);
            return null;
        } else {
            char[] response = connect.sendCommand(message, true);
            return new GenericResponse(response);
        }
    }

    /**
     * Gets the motor state for the given motor port
     *
     * @param motor Motor port
     * @return
     * @throws BluetoothException if the operation fails
     */
    public motorStateResponse getOutputState(char motor) throws BluetoothException {
        COMMAND_BYTE = 0x0006;

        char[] message = new char[2];

        message[0] = COMMAND_BYTE;
        message[1] = motor;

        char[] response = connect.sendCommand(message, true);
        return new motorStateResponse(response);
    }


}
