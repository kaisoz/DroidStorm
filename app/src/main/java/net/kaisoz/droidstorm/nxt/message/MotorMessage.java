
package net.kaisoz.droidstorm.nxt.message;

import net.kaisoz.droidstorm.nxt.Interface.MotorInterface;

/**
 * Class that abstracts a motor command.
 * Allow to set parameters such as power, turn ratio or tacho limit
 *
 * @author Tomás Tormo Franco
 */
public class MotorMessage {

    private char[] mValues = null;


    public MotorMessage() {
        mValues = new char[7];
        mValues[0] = 0x0000; /* Command byte. Will be set by the method */
    }

    /**
     * Returns the message type (RESPONSE or NO_RESPONSE)
     *
     * @return
     */
    public char getMessageType() {
        return (char) (mValues[0] & 0xFF00);
    }

    /**
     * Sets the message type (RESPONSE or NO_RESPONSE)
     *
     * @param messageType
     */
    public void setMessageType(char messageType) {
        mValues[0] = (char) (mValues[0] & 0x00FF);
        mValues[0] = (char) (mValues[0] | messageType);
    }


    /**
     * Returns the command that issued this message
     *
     * @return
     */
    public char getCommandByte() {
        return (char) (mValues[0] & 0xFF00);
    }

    /**
     * Sets the command that will issue this message
     *
     * @param commandByte
     */
    public void setCommandByte(char commandByte) {
        mValues[0] = (char) (mValues[0] & 0xFF00);
        mValues[0] = (char) (mValues[0] | commandByte);
    }

    /**
     * Returns the motor port that that this message is intended to
     *
     * @return
     */
    public char getMotorNum() {
        return (char) (mValues[1] & 0xFF00);
    }

    /**
     * Sets the motor port that that this message is intended to
     *
     * @param motorPort
     */
    public void setMotorNum(char motorPort) {
        mValues[1] = (char) (mValues[1] & 0x00FF);
        mValues[1] = (char) (mValues[1] | motorPort);
    }

    /**
     * Returns the power set to this message
     *
     * @return
     */
    public int getPower() {
        return Integer.valueOf((int) (mValues[1] & 0x00FF));
    }

    /**
     * Sets the power that this message will set
     *
     * @param power
     */
    public void setPower(int power) {
        mValues[1] = (char) (mValues[1] & 0xFF00);
        mValues[1] = (char) (mValues[1] | (Integer.valueOf(power).byteValue() & 0xFF));
    }

    /**
     * Returns the motor mode to apply to the motor
     *
     * @return
     */
    public char getMode() {
        return (char) (mValues[2] & 0xFF00);
    }

    /**
     * Sets the motor mode to apply to the motor
     *
     * @param mode
     */
    public void setMode(char mode) {
        mValues[2] = (char) (mValues[2] & 0x00FF);
        mValues[2] = (char) (mValues[2] | mode);
    }

    /**
     * Returns the regulation mode that will be applied to the motor
     *
     * @return
     */
    public char getRegulationMode() {
        return (char) (mValues[2] & 0x00FF);
    }

    /**
     * Sets the regulation mode to apply to the motor
     *
     * @param regulationMode
     */
    public void setRegulationMode(char regulationMode) {
        mValues[2] = (char) (mValues[2] & 0xFF00);
        mValues[2] = (char) (mValues[2] | regulationMode);
    }

    /**
     * Return the turn ratio field to be applied to the motor
     */
    public int getTurnRatio() {
        return (int) (mValues[3] & 0xFF00);
    }

    /**
     * Sets the turn ratio to apply to the motor
     *
     * @param turnRatio
     */
    public void setTurnRatio(int turnRatio) {
        mValues[3] = (char) (mValues[3] & 0x00FF);
        char bTurnRatio = (char) (Integer.valueOf(turnRatio).byteValue() & 0xFF);
        bTurnRatio = (char) (bTurnRatio << 8);
        mValues[3] = (char) (mValues[3] | bTurnRatio);
    }

    /**
     * Returns the run state to be applied to the motor
     */
    public char getRunState() {
        return (char) (mValues[3] & 0x00FF);
    }

    /**
     * Sets the run state to apply to the motor
     *
     * @param runState
     */
    public void setRunState(char runState) {
        mValues[3] = (char) (mValues[3] & 0xFF00);
        mValues[3] = (char) (mValues[3] | runState);
    }

    /**
     * Returns the tacho limit that will be applied to the robot
     *
     * @return
     */
    public long getTachoLimit() {
        long tacholimit = 0;
        tacholimit = tacholimit | mValues[5];
        tacholimit = tacholimit << 16;
        tacholimit = tacholimit | mValues[6];
        return tacholimit;
    }

    /**
     * Sets tachoLimit to apply to the robot
     *
     * @param runState
     */
    public void setTachoLimit(long tachoLimit) {
        mValues[4] = (char) (mValues[4] & 0x0000);
        mValues[5] = (char) (mValues[5] & 0x0000);
        mValues[6] = (char) (mValues[6] & 0x0000);

        if (tachoLimit > MotorInterface.TACHO_MAX_VALUE) {
            tachoLimit = MotorInterface.TACHO_MAX_VALUE;
        } else if (tachoLimit < MotorInterface.TACHO_MIN_VALUE) {
            tachoLimit = MotorInterface.TACHO_MIN_VALUE;
        }

        mValues[4] = (char) (tachoLimit & 0x000000FF);
        mValues[4] = (char) (mValues[4] << 8);
        tachoLimit = tachoLimit >> 8;
        mValues[4] = (char) (mValues[4] | (tachoLimit & 0x000000FF));
        tachoLimit = tachoLimit >> 8;

        mValues[5] = (char) (tachoLimit & 0x0000FFFF);
        tachoLimit = tachoLimit >> 16;
        mValues[6] = (char) (tachoLimit & 0x0000FFFF);
    }

    /**
     * Gets the char array with all the command mValues
     *
     * @return char[]
     */
    public char[] getCommandValues() {
        return mValues;
    }
}
