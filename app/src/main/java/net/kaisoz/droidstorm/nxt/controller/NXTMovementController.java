package net.kaisoz.droidstorm.nxt.controller;

import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;

/**
 * Controller used by NXTMovementHandlerActivity.
 * Does the required distance and angle checks to assure that the motor has to move
 *
 * @author Tomás Tormo Franco
 */
public class NXTMovementController extends NXTBaseController {

    private static final int MIN_ANGLE = 5;
    private static final int MIN_POWER = 11;

    public NXTMovementController() {
        super();
    }

    public NXTMovementController(char lWheel, char rWheel) {
        super(lWheel, rWheel);
    }

    /**
     * Moves the robot depending on a given power an angle
     * These two mValues have to be previously calculated. If they are in a minimun range, its value wil be used to
     * move the robot.
     *
     * @param distance Distance of the polar coordinates calculated previously. Will be used as the power
     * @param angle    Angle of the polar coordinates calculated previously. Will be used as the turn ratio
     */
    public void moveNXT(int distance, int angle) throws BluetoothException {

        // Check if the mValues are different from the previous ones
        if (distance != mPrevPower || angle != mPrevAngle) {
            if (distance > MIN_POWER || distance < (MIN_POWER * -1)) {
                if (mRecording) {
                    if (mTimeExec == 0)
                        mTimeExec = System.currentTimeMillis();
                    else
                        recordMovement();
                }
                if (angle > MIN_ANGLE || angle < (MIN_ANGLE * -1)) {
                    move(angle, distance);
                    mPrevPower = distance;
                    mPrevAngle = angle;
                } else {
                    move(STRAIGHT_ANGLE, distance);
                    mPrevPower = distance;
                    mPrevAngle = STRAIGHT_ANGLE;
                }
            } else {
                stop();
            }
        }
    }


    /**
     * Stops the robot
     *
     * @throws BluetoothException If the operation fails
     */
    @Override
    public void stop() throws BluetoothException {
        super.stop();
        mPrevPower = 0;
        mPrevAngle = 0;
    }
}


