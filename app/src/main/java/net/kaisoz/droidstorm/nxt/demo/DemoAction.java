package net.kaisoz.droidstorm.nxt.demo;

/**
 * Abstraction of an action.
 * Holds the power, turn ratio, tacho limit, and duration of a movement command.
 *
 * @author Tomás Tormo Franco
 */
public class DemoAction {

    private int mPower = 0;
    private int mTurnRatio = 0;
    private long mTachoLimit = 0;
    private long mDuration = 0;

    public DemoAction(int power, int turnRatio, long tachoLimit, long delay) {
        this.mPower = power;
        this.mTurnRatio = turnRatio;
        this.mTachoLimit = tachoLimit;
        this.mDuration = delay;
    }

    public DemoAction() {
    }

    public int getPower() {
        return mPower;
    }

    public void setPower(int power) {
        this.mPower = power;
    }

    public int getTurnRatio() {
        return mTurnRatio;
    }

    public void setTurnRatio(int turnRatio) {
        this.mTurnRatio = turnRatio;
    }

    public long getTachoLimit() {
        return mTachoLimit;
    }

    public void setTachoLimit(long tachoLimit) {
        this.mTachoLimit = tachoLimit;
    }

    public long getDelay() {
        return mDuration;
    }

    public void setDelay(long delay) {
        this.mDuration = delay;
    }


}
