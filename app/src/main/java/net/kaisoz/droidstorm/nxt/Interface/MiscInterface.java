package net.kaisoz.droidstorm.nxt.Interface;

import net.kaisoz.droidstorm.bluetooth.Connection;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.nxt.message.CloseHandleResponse;
import net.kaisoz.droidstorm.nxt.message.FindFileResponse;
import net.kaisoz.droidstorm.nxt.message.GenericResponse;

/**
 * Class which exposes system functions to interact with the robot such as program starting or program searching
 *
 * @author Tomás Tormo Franco
 */

public class MiscInterface extends NXTInterface {


    /***
     * Singleton
     ***/
    private MiscInterface() {
    }

    private Connection connect = null;

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class miscHolder {
        public static final MiscInterface INSTANCE = new MiscInterface();
    }

    /**
     * Returns an instance of a MiscInterface
     *
     * @return
     */
    public static MiscInterface getInstance() {
        return miscHolder.INSTANCE;
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
     * Plays a tone through the robot speaker.
     * For parameters mValues, see Bluetooth developer kit at http://mindstorms.lego.com/en-us/support/files/default.aspx
     *
     * @param frequency   Frequency of the tone
     * @param tone        tone type
     * @param messageType If a response should be expected from the robot (RESPONSE or NO_RESPONSE)
     * @return
     * @throws Exception if the operation fails
     */
    public GenericResponse playTone(int frequency, int tone, char messageType) throws Exception {
        char[] message = new char[3];
        COMMAND_BYTE = 0x0003;
        message[0] = (char) (messageType | COMMAND_BYTE);

        message[1] = (char) (frequency & 0x00FF);
        message[1] = (char) (message[1] << 8);
        frequency = frequency << 8;
        message[1] = (char) (message[1] | frequency);

        message[2] = (char) (tone & 0x00FF);
        message[2] = (char) (message[2] << 8);
        tone = tone << 8;
        message[2] = (char) (message[2] | tone);

        if (messageType == MESSAGETYPE_NORESPONSE) {
            connect.sendCommand(message, false);
            return null;
        } else {
            char[] response = connect.sendCommand(message, true);
            return new GenericResponse(response);
        }
    }

    /**
     * Starts a program in the NXT device
     *
     * @param name        Name of the program to start
     * @param messageType If a response should be expected from the robot (RESPONSE or NO_RESPONSE)
     * @return
     * @throws Exception
     */
    public GenericResponse startProgram(String name, char messageType) throws BluetoothException {
        int messageSize = Math.round(name.length() / 2) + 2;
        char[] message = new char[messageSize];
        COMMAND_BYTE = 0x0000;
        message[0] = (char) (messageType | COMMAND_BYTE);

        int i = 0;
        int j = 1;
        while (i < name.length() && j < messageSize) {
            message[j] = name.charAt(i);
            message[j] = (char) (message[j] << 8);
            i++;
            if (i < name.length()) {
                message[j] = (char) (message[j] | name.charAt(i));
                i++;
            }
            j++;
        }

        message[message.length - 1] = (char) (message[message.length - 1] & 0xFF00);

        if (messageType == MESSAGETYPE_NORESPONSE) {
            connect.sendCommand(message, false);
            return null;
        } else {
            char[] response = connect.sendCommand(message, true);
            return new GenericResponse(response);
        }
    }

    /**
     * Finds the first file name which name matches "name" pattern
     *
     * @param name Pattern that the file name should match
     * @return
     * @throws BluetoothException If the operation fails
     */
    public FindFileResponse findFirst(String name) throws BluetoothException {
        int messageSize = Math.round(name.length() / 2) + 2;
        char[] message = new char[messageSize];
        COMMAND_BYTE = 0x0186;
        message[0] = COMMAND_BYTE;
        int i = 0;
        int j = 1;
        while (i < name.length() && j < messageSize) {
            message[j] = name.charAt(i);
            message[j] = (char) (message[j] << 8);
            i++;
            if (i < name.length()) {
                message[j] = (char) (message[j] | name.charAt(i));
                i++;
            }
            j++;
        }

        message[message.length - 1] = (char) (message[message.length - 1] & 0xFF00);
        char[] response = connect.sendCommand(message, true);
        return new FindFileResponse(response);

    }

    /**
     * Finds the next file name that matches the pattern passed in findFirst method
     *
     * @param handle file handler returned by findFirst method
     * @return
     * @throws BluetoothException If the operation fails
     */
    public FindFileResponse findNext(int handle) throws BluetoothException {
        char handleByte = (char) (handle & 0x00FF);

        char[] message = new char[2];
        COMMAND_BYTE = 0x0187;
        message[0] = COMMAND_BYTE;
        message[1] = (char) (handleByte << 8);
        char[] response = connect.sendCommand(message, true);
        return new FindFileResponse(response);
    }

    /**
     * Closes a file handler
     *
     * @param handle Handler that should be closed
     * @return
     * @throws BluetoothException If the operation fails
     */
    public CloseHandleResponse closeHandle(int handle) throws BluetoothException {
        char handleByte = (char) (handle & 0x00FF);

        char[] message = new char[2];
        COMMAND_BYTE = 0x0184;
        message[0] = COMMAND_BYTE;
        message[1] = (char) (handleByte << 8);
        char[] response = connect.sendCommand(message, true);
        return new CloseHandleResponse(response);
    }
}
