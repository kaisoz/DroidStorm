package net.kaisoz.droidstorm.nxt.handler;


import java.util.List;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.nxt.controller.NXTMovementController;
import net.kaisoz.droidstorm.util.SensorAverageDamper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * Activity which implements the robot control by movements.
 * It registers all three sensors and combines its values to get the orientation of the phone. In order to get the orientation,
 * coordinate system is changed from the phone coordinate system to the real world's coordinate system.
 * Once orientation values are calculated,  they are "dampered" in order to avoid possible jitters.
 * Finally, values are changed from rectangular coordinates to polar coordinates, in order to get the distance and angle of the point
 * These distance and angle will be used as power and turn ratio to the motors
 *
 * @author Tomás Tormo Franco
 */

public class MovementHandlerActivity extends NXTHandlerBaseActivity implements SensorEventListener {

    private static final double TILT_MAGNIFIER = 1.5;
    static Sensor mAcceleratorSensor = null;
    static Sensor mMagneticSensor = null;
    static Sensor mOrientationSensor = null;
    static SensorManager mSensorManager = null;
    private SensorArrowView mSensorArrowView;
    final int mMatrix_size = 16;
    float[] Rarray = new float[mMatrix_size];
    float[] mOutR = new float[mMatrix_size];
    float[] mLarray = new float[mMatrix_size];
    private float mOrientationValues[] = new float[3];
    float[] mMags = new float[3];
    float[] mAccels = new float[3];
    boolean mIsReady = false;
    private SensorAverageDamper mDamper = null;
    private boolean mStopRequested = false;
    int mAngle = 0;
    int mTilt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(R.string.activity_label_movhandler);
        mDamper = new SensorAverageDamper(6, false, true, true);

        mSensorArrowView = new SensorArrowView(this);
        setContentView(mSensorArrowView);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAcceleratorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mController = new NXTMovementController();
        mNXTReady = true;
        Toast.makeText(this, R.string.movHandler_toast_startRecording, Toast.LENGTH_LONG).show();
    }

    @Override
    protected boolean disableHandler() {
        mStopRequested = true;
        mSensorManager.unregisterListener(this);

        return true;
    }

    @Override
    protected boolean enableHandler() {
        mStopRequested = false;
        mSensorManager.registerListener(this, mAcceleratorSensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_GAME);
        return true;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mRecording) {
                    startRecording();
                    Toast.makeText(this, R.string.movHandler_toast_stopRecording, Toast.LENGTH_LONG).show();
                } else {
                    stopRecording();
                }
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (!mStopRequested) {
                int type = event.sensor.getType();

                switch (type) {
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        Log.d("MOV", "onSensorChanged: magnetic");
                        mMags = event.values.clone();
                        mIsReady = true;
                        break;
                    case Sensor.TYPE_ACCELEROMETER:
                        Log.d("MOV", "onSensorChanged: accelerometer");
                        mAccels = event.values.clone();
                        break;
                }

                if (mMags != null && mAccels != null && mIsReady) {
                    mIsReady = false;
                    // Remap and calculate polar coordinates
                    translateCoordenates();
                    // Pass results to sensor arrow view in order to print the arrow
                    mSensorArrowView.setData(this.mOrientationValues, this.mTilt, -this.mAngle);
                    mSensorArrowView.calcArrowAngle();
                    mSensorArrowView.invalidate();

                    if (mOrientationValues[1] < 0) mTilt *= -1;
                    try {
                        // Send values to the robot
                        Log.d("MOV", "onSensorChanged: sending to the robot");
                        ((NXTMovementController) mController).moveNXT(mTilt, mAngle);
                    } catch (BluetoothException e) {
                        handleException(e);
                    }
                }
            }
        }
    }


    /**
     * Remaps coordinates system and translates sensors values to polar coordinates
     */
    private void translateCoordenates() {
        /** Remap coordinates to landscape mValues **/
        SensorManager.getRotationMatrix(Rarray, mLarray, mAccels, mMags);
        SensorManager.remapCoordinateSystem(Rarray, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mOutR);
        SensorManager.getOrientation(mOutR, mOrientationValues);

        // Filter mValues 
        mOrientationValues = mDamper.damp(mOrientationValues);

        // Convert radians to degrees
        for (int i = 0; i < mOrientationValues.length; i++) {
            mOrientationValues[i] = (float) Math.toDegrees(mOrientationValues[i]);
        }
        if (mOrientationValues[2] == 0) return;

        // Rectangular coordinates to polar coordinates conversion
        mAngle = (int) (Math.toDegrees(
                Math.atan(mOrientationValues[2] / mOrientationValues[1])));
        mTilt = (int) Math.sqrt((
                Math.pow(mOrientationValues[2], 2))
                + (Math.pow(mOrientationValues[1], 2)));
        mTilt *= TILT_MAGNIFIER;
        if (mTilt > 100) mTilt = 100;
    }
}
