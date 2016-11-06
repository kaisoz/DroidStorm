package net.kaisoz.droidstorm.nxt.message;

/**
 * Abstracts a response received when the state of a motor is requested
 *
 * @author Tomás Tormo Franco
 */
public class motorStateResponse extends GenericResponse {


    public motorStateResponse(char[] values) {
        super(values);
    }

    /**
     * Returns the motor port of the motor state
     *
     * @return
     */
    public char getMotorNum() {
        return (char) ((values[1] & 0x00FF) << 8);
    }

    /**
     * Returns the power of the motor
     *
     * @return
     */
    public int getPower() {
        return Integer.valueOf((int) ((values[2] & 0xFF00) >> 8));
    }

    /**
     * Returns the mode applied to the motor
     *
     * @return
     */
    public char getMode() {
        return (char) ((values[2] & 0x00FF) << 8);
    }

    /**
     * Returns the regulation mode applied to the motor
     *
     * @return
     */
    public char getRegulationMode() {
        return (char) ((values[3] & 0xFF00) >> 8);
    }

    /**
     * Returns the turn ratio applied to the motor
     *
     * @return
     */
    public int getTurnRatio() {
        return (int) ((values[3] & 0xFF00) >> 8);
    }

    /**
     * Returns the runstate applied to the motor
     *
     * @return
     */
    public char getRunState() {
        return (char) ((values[4] & 0xFF00) >> 8);
    }

    /**
     * Returns the tacho limit applied to the motor
     *
     * @return
     */
    public long getTachoLimit() {
        long tacholimit = 0;
        tacholimit = (tacholimit | (values[4] & 0xFF)) << 24;
        tacholimit = (tacholimit | values[5]) << 16;
        tacholimit = tacholimit | ((values[6] & 0xFF00) >> 8);
        return tacholimit;
    }

    /**
     * Returns the number of counts since the last reset of the motor counter
     *
     * @return
     */
    public long getTachoCount() {
        long tachoCount = 0;
        tachoCount = (tachoCount | (values[6] & 0xFF)) << 24;
        tachoCount = (tachoCount | values[7]) << 16;
        tachoCount = tachoCount | ((values[8] & 0xFF00) >> 8);
        return tachoCount;
    }

    /**
     * Returns the current position relative to the last programmed movement
     *
     * @return
     */
    public long getBlockTachoCount() {
        long blockTachoCount = 0;
        blockTachoCount = (blockTachoCount | (values[8] & 0xFF)) << 24;
        blockTachoCount = (blockTachoCount | values[9]) << 16;
        blockTachoCount = blockTachoCount | ((values[10] & 0xFF00) >> 8);
        return blockTachoCount;
    }

    /**
     * Returns the current position relative to the last reset of the rotation sensor for this motor
     *
     * @return
     */
    public long getRotationCount() {
        long rotationCount = 0;
        rotationCount = (rotationCount | (values[10] & 0xFF)) << 24;
        rotationCount = (rotationCount | values[11]) << 16;
        rotationCount = rotationCount | ((values[12] & 0xFF00) >> 8);
        return rotationCount;
    }
}
