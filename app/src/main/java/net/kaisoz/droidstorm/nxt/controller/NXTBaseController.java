package net.kaisoz.droidstorm.nxt.controller;

import net.kaisoz.droidstorm.bluetooth.Connection;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.nxt.Interface.MotorInterface;
import net.kaisoz.droidstorm.nxt.message.MotorMessage;
import net.kaisoz.droidstorm.nxt.message.motorStateResponse;
import net.kaisoz.droidstorm.util.DatabaseHelper;

/**
 * Base class for the controllers.
 * Exposes the main robot movement methods as well as methods to get motor states.
 * It also exposes methods to control the IR emitter
 * This base class must be extended by all controllers in order to use the basic movement functions
 *
 * @author Tomás Tormo Franco
 */

public class NXTBaseController {

    protected final int SEARCH_LEFT = 4;
    protected final int SEARCH_FRONT = 5;
    protected final int SEARCH_RIGHT = 6;
    protected static char mLeftWheel = MotorInterface.NO_MOTOR;
    protected static char mRightWheel = MotorInterface.NO_MOTOR;
    protected char mIRport = MotorInterface.NO_MOTOR;
    protected int mPower = 0;
    protected MotorMessage mMessage = null;
    protected MotorInterface mMotorInterface = null;
    protected DatabaseHelper mDbHelper = null;
    protected boolean mRecording = false;
    protected long mTimeExec = 0;
    protected static int mPrevPower = 0;
    protected static int mPrevAngle = 0;
    protected static final int STRAIGHT_ANGLE = 0;

    public NXTBaseController() {
        mMessage = new MotorMessage();
        mMotorInterface = MotorInterface.getInstance();
        mMessage.setMessageType(MotorInterface.MESSAGETYPE_NORESPONSE);
    }

    public NXTBaseController(char lWheel, char rWheel) {
        mMessage = new MotorMessage();
        mMotorInterface = MotorInterface.getInstance();
        mMessage.setMessageType(MotorInterface.MESSAGETYPE_NORESPONSE);

        mLeftWheel = lWheel;
        mRightWheel = rWheel;
    }

    /** Setters **/


    /**
     * Sets wheels ports
     *
     * @param lWheel port for the left wheel
     * @param rWheel port for the right wheel
     */
    public void setWheels(char lWheel, char rWheel) {
        mLeftWheel = lWheel;
        mRightWheel = rWheel;
    }

    /**
     * Sets the power to apply to the robot wheels
     *
     * @param power
     */
    public void setPower(int power) {
        this.mPower = power;
    }

    /**
     * Sets infrared emitter port
     *
     * @param irport IR emitter port
     */
    public void setIRPort(char irport) {
        this.mIRport = irport;
    }

    /**
     * Sets a connection object
     *
     * @param conn ConnectionLegacy object
     */
    public void setConnection(Connection conn) {
        mMotorInterface.setConnection(conn);
    }

    /**
     * Returns the name of the port for the given port id
     *
     * @param port port ID which name is wanted to know
     * @return Port name (A|B|C|none)
     */
    public String getPortString(char portID) {
        switch (portID) {
            case MotorInterface.MOTOR_A:
                return "A";
            case MotorInterface.MOTOR_B:
                return "B";
            case MotorInterface.MOTOR_C:
                return "C";
            default:
                return "None";
        }
    }


    /** Robot actuators **/
    /**
     * Turns the IR emitter on
     *
     * @throws BluetoothException If the operation fails
     */
    public void turnIREmitterOn() throws BluetoothException {
        mMessage.setMode(MotorInterface.MOTOR_MODE_ON);
        mMessage.setTurnRatio(0);
        mMessage.setMotorNum(mIRport);
        mMessage.setPower(100);
        mMessage.setRegulationMode(MotorInterface.MOTOR_REGULATION_IDLE);
        mMessage.setRunState(MotorInterface.MOTOR_RUNSTATE_RUNNING);
        mMotorInterface.setOutputState(mMessage);

    }

    /**
     * Turns the IR Emitter off
     *
     * @throws BluetoothException If the operation fails
     */
    public void turnIREmitterOff() throws BluetoothException {
        mMessage.setMode(MotorInterface.MOTOR_MODE_BRAKE);
        mMessage.setMotorNum(mIRport);
        mMessage.setPower(0);
        mMessage.setRegulationMode(MotorInterface.MOTOR_REGULATION_IDLE);
        mMessage.setRunState(MotorInterface.MOTOR_RUNSTATE_IDLE);
        mMotorInterface.setOutputState(mMessage);
    }

    /**
     * Stops the robot
     * Also, if recording mode is enabled, previous movement is saved in database, otherwilse, last movement would
     * not be recorded
     *
     * @throws BluetoothException If the operation fails
     */
    public void stop() throws BluetoothException {

        if (mRecording) {
            if (mTimeExec == 0) {
                mTimeExec = System.currentTimeMillis();
            } else {
                recordMovement();
            }
            mPrevPower = 0;
            mPrevAngle = 0;
        }

        mMessage.setMode(MotorInterface.MOTOR_MODE_BRAKE);
        mMessage.setMotorNum(mLeftWheel);
        mMessage.setPower(0);
        mMessage.setTurnRatio(0);
        mMessage.setTachoLimit(0L);
        mMessage.setRegulationMode(MotorInterface.MOTOR_REGULATION_IDLE);
        mMessage.setRunState(MotorInterface.MOTOR_RUNSTATE_IDLE);
        mMotorInterface.setOutputState(mMessage);
        mMessage.setMotorNum(mRightWheel);
        mMotorInterface.setOutputState(mMessage);

    }

    /**
     * Moves the robot
     * Also, if recording mode is enabled, previous movement is saved in database
     *
     * @param masterWheel Port ID of the master wheel
     * @param slaveWheel  Port ID of the slave wheel which will be synchronized to the master wheel
     * @param turnRatio   Turn ratio to be applied to the wheels
     * @param power       Power to be applied to the wheels
     * @param tachoLimit  Travel distance in degrees. If 0, means "no limit"
     * @throws BluetoothException If the operation fails
     */
    public void move(char masterWheel, char slaveWheel, int turnRatio, int power, long tachoLimit) throws BluetoothException {

        if (mRecording) {
            if (mTimeExec == 0) {
                mTimeExec = System.currentTimeMillis();
            } else {
                recordMovement();
            }
            mPrevPower = power;
            mPrevAngle = turnRatio;
        }

        mMotorInterface.resetMotorPosition(masterWheel, true, MotorInterface.MESSAGETYPE_NORESPONSE);
        mMotorInterface.resetMotorPosition(slaveWheel, true, MotorInterface.MESSAGETYPE_NORESPONSE);

        mMessage.setTachoLimit(tachoLimit);
        mMessage.setMode(MotorInterface.MOTOR_MODE_ON_REGULATED_BRAKE);
        mMessage.setTurnRatio(turnRatio);
        mMessage.setMotorNum(masterWheel);
        mMessage.setPower(power);
        mMessage.setRegulationMode(MotorInterface.MOTOR_REGULATION_MOTORSYNC);
        mMessage.setRunState(MotorInterface.MOTOR_RUNSTATE_RUNNING);
        mMotorInterface.setOutputState(mMessage);
        mMessage.setMotorNum(slaveWheel);
        mMotorInterface.setOutputState(mMessage);

    }

    /**
     * Moves the robot using default wheels configuration and no tacho limit
     *
     * @param turnRatio Turn ratio to be applied to the wheels
     * @param power     Power to be applied to the wheels
     * @throws BluetoothException If the operation fails
     */
    public void move(int turnRatio, int power) throws BluetoothException {
        move(mLeftWheel, mRightWheel, turnRatio, power, 0);
    }

    /**
     * Moves the robot using default wheels configuration
     *
     * @param turnRatio  Turn ratio to be applied to the wheels
     * @param power      Power to be applied to the wheels
     * @param tachoLimit Travel distance in degrees. If 0, means "no limit"
     * @throws BluetoothException If the operation fails
     */
    public void move(int turnRatio, int power, long tachoLimit) throws BluetoothException {
        move(mLeftWheel, mRightWheel, turnRatio, power, tachoLimit);
    }

    /**
     * Returns the state for a motor with connected to the given portId
     *
     * @param portId Port ID where the inquiried motor is connected to
     * @return MotorStateResponse object with the motor state
     * @throws BluetoothException if the operation fails
     */
    public motorStateResponse getMotorState(char portId) throws BluetoothException {
        char motorQuery = portId;
        if (motorQuery == MotorInterface.NO_MOTOR) {
            motorQuery = mRightWheel;
        }

        return mMotorInterface.getOutputState(motorQuery);
    }

    /**
     * Gets the motor state for the default master wheel (leftwheel)
     *
     * @return MotorStateResponse object with the motor state
     * @throws BluetoothException if the operation fails
     */
    public motorStateResponse getMotorState() throws BluetoothException {
        return this.getMotorState(mLeftWheel);
    }


    /**
     * Moves the robot in order to be visible by the follower
     *
     * @param direction Last known direction where the leader was seen by the follower
     * @throws BluetoothException In case of communication error
     */
    public void beVisible(int direction) throws BluetoothException {

        switch (direction) {
            case SEARCH_LEFT:
                move(100, 50, 100L);
                break;

            case SEARCH_RIGHT:
                move(-100, 50, 100L);
                break;

            case SEARCH_FRONT:
                move(STRAIGHT_ANGLE, -50, 150L);
                break;
        }
    }

    /**
     * Starts movement recording in the database
     *
     * @param db Reference to the database object handler
     */
    public void startRecording(DatabaseHelper db) {
        this.mDbHelper = db;
        this.mDbHelper.startDemoActionTransaction();
        this.mRecording = true;
    }

    /**
     * Records previous movement in the database
     */
    public void recordMovement() {
        mDbHelper.insertDemoAction(mPrevPower, mPrevAngle, 0, (System.currentTimeMillis() - mTimeExec));
        mTimeExec = System.currentTimeMillis();
    }

    /**
     * Stops movement recording
     */
    public void stopRecording() {
        mRecording = false;
        mTimeExec = 0;
        mDbHelper.finishDemoActionTransaction();
    }

}
