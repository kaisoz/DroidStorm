package net.kaisoz.droidstorm.util;

/**
 * Class that adapts the sensor output mValues
 * by damping them over an window of previous sensor mValues.
 * <p>
 * The new sensor mValues are the average over all sensor mValues
 * in the window.
 * <p>
 * A biger window means less fluctuation in the sensor mValues.
 * However a side-effect of a large window is that it takes
 * there is a delay in movement detection.
 *
 * @author Tomás Tormo Franco
 */
public class SensorAverageDamper {
    private float[] window0;
    private float[] window1;
    private float[] window2;
    private int i;
    private int windowSize;

    /**
     * Creates an sensor mDamper with a specific window size.
     *
     * @param windowSize the of the average window
     */
    public SensorAverageDamper(int windowSize) {
        this(windowSize, true, true, true);
    }

    /**
     * Creates a new mDamper with a specific window size.
     *
     * @param windowSize the of the average window
     * @param damp0      wether to damp sensor value mValues[0]
     * @param damp1      wether to damp sensor value mValues[1]
     * @param damp1      wether to damp sensor value mValues[1]
     */
    public SensorAverageDamper(int windowSize, boolean damp0, boolean
            damp1, boolean damp2) {
        this.windowSize = windowSize;
        if (damp0) window0 = createWindow();
        if (damp1) window1 = createWindow();
        if (damp2) window2 = createWindow();
    }

    protected float[] createWindow() {
        float[] w = new float[windowSize];
        for (int i = 0; i < w.length; i++) w[i] = -1;
        return w;
    }

    protected float average(float[] w) {
        int l = 0;
        int j = i;
        float sum = 0;
        while (true) {
            if (w[j] != -1) {
                l++;
                sum += w[j];
            }
            j = (j + 1) % w.length;
            if (j == i) break;
        }
        return sum / l;
    }

    /**
     * Adds the mValues to the window and
     * returns the damped mValues (average in the window)
     *
     * @param mValues
     * @return
     */
    public float[] damp(float[] values) {
        i = (i + 1) % windowSize;
        if (window0 != null) {
            window0[i] = values[0];
            values[0] = average(window0);
        }
        if (window1 != null) {
            window1[i] = values[1];
            values[1] = average(window1);
        }
        if (window2 != null) {
            window2[i] = values[2];
            values[2] = average(window2);
        }
        return values;
    }
} 

