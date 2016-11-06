package net.kaisoz.droidstorm.bluetooth;

/**
 * Holds and processes "Leader lost protocol" messages
 *
 * @author Tomás Tormo Franco
 */
public class MessageBuffer {

    public static int FORMAT_FANTOM = 0;
    public static int FORMAT_CUSTOM = 1;
    private int mMessageFormat = FORMAT_FANTOM;
    private boolean mNewMessage = false;
    private char[] mMessage;


    /**
     * Returns the set message.
     * - If the message format is the to custom, the message will be returned as it is
     * - If the message is set as fantom, previous calculations will be made in order to ensure the size of the
     * char array which will hold it
     *
     * @return
     */
    public char[] getMessage() {
        if (mMessageFormat == FORMAT_CUSTOM) {
            return mMessage;
        } else {
            int size = (int) mMessage[1];
            if (size % 2 == 0) {
                size = size / 2;
            } else {
                size = (size / 2) + 1;
            }
            char[] formattedMsg = new char[size];
            System.arraycopy(mMessage, 2, formattedMsg, 0, size);
            return formattedMsg;
        }
    }

    /**
     * Sets the message
     *
     * @param message
     */
    public void setMessage(char[] message) {
        this.mMessage = message;
    }

    /**
     * Returns true if a new message is avaliable
     *
     * @return
     */
    public boolean isNewMessage() {
        return mNewMessage;
    }

    /**
     * Marks that there is a new message
     *
     * @param newMessage True if there is a new message
     */
    public synchronized void setNewMessage(boolean newMessage) {
        this.mNewMessage = newMessage;
    }

    /**
     * Sets message format
     *
     * @param messageFormat FORMAT_CUSTOM or FORMAT_FANTOM
     */
    public void setMessageFormat(int messageFormat) {
        this.mMessageFormat = messageFormat;
    }

    /**
     * Returns message format
     *
     * @return
     */
    public int getMessageFormat() {
        return mMessageFormat;
    }


}
