package net.kaisoz.droidstorm.nxt.message;

public class CloseHandleResponse {

    protected char[] values;

    public CloseHandleResponse(char[] values) {
        this.values = values;
    }

    /**
     * Gets operation status
     *
     * @return
     */
    public char getStatus() {
        return (char) ((values[1] & 0xFF00) >> 8);
    }

    /**
     * Returns the handler just closed
     *
     * @return
     */
    public char getHandle() {
        return (char) (values[1] & 0x00FF);
    }
}
