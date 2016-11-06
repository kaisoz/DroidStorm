package net.kaisoz.droidstorm.nxt.message;

/**
 * Abstracts a generic response received from the robot
 * Contains the result of the operation amongst other things
 *
 * @author Tomás Tormo Franco
 */
public class GenericResponse {

    protected char[] values;

    public GenericResponse(char[] values) {
        this.values = values;
    }

    /**
     * Returns the command that returned this response
     *
     * @return
     */
    public char getFromCommand() {
        return (char) (values[0] & 0x00FF);
    }

    /**
     * Returns the result of the operation.
     * If 0, the operation was successful, otherwise there was an error.
     * See Bluetooth Developer Kit (http://mindstorms.lego.com/en-us/support/files/default.aspx) for return values
     *
     * @return
     */
    public char getStatus() {
        return (char) ((values[1] & 0xFF00) >> 8);
    }
}
