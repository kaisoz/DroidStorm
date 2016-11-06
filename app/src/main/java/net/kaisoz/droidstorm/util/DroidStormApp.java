package net.kaisoz.droidstorm.util;

import android.app.Application;

import java.util.HashMap;

/**
 * Application class. This kind of applications remain alive during the whole application lifecycle
 * Used to hold data that should be accesible from all the activities (mainly preferences)
 *
 * @author Tomás Tormo Franco
 */
public class DroidStormApp extends Application {

    private HashMap mValues = null;
    private String mDefaultLocale;
    private boolean mLocaleChanged = false;
    private boolean mWheelChanged = true;
    private boolean mModeChanged = true;

    @Override
    public void onCreate() {
        super.onCreate();
        mValues = new HashMap();
    }

    public void setValue(Object key, Object value) {
        mValues.put(key, value);
    }

    public Object getValue(Object key) {
        return mValues.get(key);
    }

    public void remove(Object key) {
        mValues.remove(key);
    }

    /**
     * Sets the default locale for the application
     *
     * @param defaultLocale
     */
    public void setDefaultLocale(String defaultLocale) {
        this.mDefaultLocale = defaultLocale;
    }

    /**
     * Returns default locale for the application
     *
     * @return
     */
    public String getDefaultLocale() {
        return mDefaultLocale;
    }

    /**
     * Marks the locale as changed
     *
     * @param localeChanged
     */
    public void setLocaleChanged(boolean localeChanged) {
        this.mLocaleChanged = localeChanged;
    }

    /**
     * True is the application locale has changed
     *
     * @return
     */
    public boolean isLocaleChanged() {
        return mLocaleChanged;
    }

    /**
     * Sets the wheels configuration as changed
     *
     * @param wheelChanged
     */
    public void setWheelChanged(boolean wheelChanged) {
        this.mWheelChanged = wheelChanged;
    }

    /**
     * True when the wheels configuration has changed
     *
     * @return
     */
    public boolean isWheelChanged() {
        return mWheelChanged;
    }

    /**
     * Marks mode configuration has changed
     *
     * @param modeChanged
     */
    public void setModeChanged(boolean modeChanged) {
        this.mModeChanged = modeChanged;
    }

    /**
     * True when the operation mode has changed (synchronized, follower)
     *
     * @return
     */
    public boolean isModeChanged() {
        return mModeChanged;
    }

}