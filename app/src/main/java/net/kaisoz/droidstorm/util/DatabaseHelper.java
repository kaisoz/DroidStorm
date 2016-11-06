package net.kaisoz.droidstorm.util;

import java.util.ArrayList;

import net.kaisoz.droidstorm.nxt.demo.DemoAction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Data source class used to access demo registers in database.
 * It exposes functions to insert, delete and rename demos
 *
 * @author Tomás Tormo Franco
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DBNAME = "demoActionsDB";
    private static final String DEMO_TABLE = "demo";
    private static final String COL_ID = "demoID";
    private static final String COL_NAME = "demoName";
    private static final String DEMO_ACTIONS_TABLE = "demoActions";
    private static final String COL_DEMO_ACTION_ID = "demoActionId";
    private static final String COL_DEMOID = "demoId";
    private static final String COL_POWER = "Power";
    private static final String COL_TACHOLIMIT = "TachoLimit";
    private static final String COL_TURNRATIO = "TurnRatio";
    private static final String COL_DURATION = "Delay";
    private static ArrayList<Long> mDemoActionsId;
    private int mIterator = 0;
    private boolean mIterationReady = false;
    private long mDemoId = 0;


    public DatabaseHelper(Context context) {
        super(context, DBNAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DEMO_TABLE + " (" + COL_ID + " INTEGER PRIMARY KEY , " +
                COL_NAME + " TEXT)");

        db.execSQL("CREATE TABLE " + DEMO_ACTIONS_TABLE + " (" + COL_DEMO_ACTION_ID + " INTEGER PRIMARY KEY , " + COL_DEMOID + " INTEGER , " +
                COL_POWER + " INTEGER , " + COL_TACHOLIMIT + " INTEGER , " + COL_TURNRATIO + " INTEGER , " + COL_DURATION + " INTEGER )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    /**
     * Inserts a new demo in the database whith name demoName
     *
     * @param demoName Name of the demo
     * @return
     */
    public long insertDemo(String demoName) {
        long rowId = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, demoName);
        rowId = db.insert(DEMO_TABLE, null, cv);
        db.close();
        return rowId;
    }

    /**
     * Creates a new unnamed demo
     *
     * @return
     */
    public long createDemo() {
        mDemoId = insertDemo("unnamed");
        return mDemoId;
    }

    /**
     * Inserts a new action in the database using the global demo id variable
     *
     * @param power
     * @param turnRatio
     * @param tachoLimit
     * @param duration
     */
    public void insertDemoAction(int power, int turnRatio, long tachoLimit, long duration) {
        this.insertDemoAction(mDemoId, power, turnRatio, tachoLimit, duration);
    }

    /**
     * Returns the names of all the demos in the database
     *
     * @return
     */
    public ArrayList<String> getAllDemoNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> demoNames = new ArrayList<String>();
        String[] columns = new String[]{COL_NAME};
        Cursor c = db.query(DEMO_TABLE, columns, null,
                new String[]{}, null, null, null);
        if (c.moveToFirst()) {
            demoNames.add(c.getString(0));
            while (c.moveToNext()) {
                demoNames.add(c.getString(0));
            }
        }
        c.close();
        db.close();
        return demoNames;
    }

    /**
     * Prepares a database transaction in order to write several actions
     * Actions won't be commited until finishDemoAction is called
     */
    public void startDemoActionTransaction() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
    }

    /**
     * Inserts a new action. Should be used into a transaction
     *
     * @param demoId
     * @param power
     * @param turnRatio
     * @param tachoLimit
     * @param delay
     */
    public void insertDemoAction(long demoId, int power, int turnRatio, long tachoLimit, long delay) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DEMOID, demoId);
        cv.put(COL_POWER, power);
        cv.put(COL_TACHOLIMIT, tachoLimit);
        cv.put(COL_TURNRATIO, turnRatio);
        cv.put(COL_DURATION, delay);
        db.insert(DEMO_ACTIONS_TABLE, null, cv);
    }

    /**
     * Finishes the database transaction. This commits all the database changes
     */
    public void finishDemoActionTransaction() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    /**
     * Changes the name of the demo with the given demoId
     *
     * @param demoId
     * @param demoName
     */
    public void setDemoName(long demoId, String demoName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, demoName);
        db.update(DEMO_TABLE, cv, COL_ID + "=?",
                new String[]{String.valueOf(demoId)});
        db.close();
    }

    /**
     * Sets the demo name using the demo Id global variable
     *
     * @param demoName
     */
    public void setDemoName(String demoName) {
        setDemoName(mDemoId, demoName);
    }

    /**
     * Returns the name for the given demo id
     *
     * @param demoName
     * @return
     */
    public long getDemoIDByName(String demoName) {
        SQLiteDatabase db = this.getReadableDatabase();
        long demoId = -1;
        String[] columns = new String[]{COL_ID};
        Cursor c = db.query(DEMO_TABLE, columns, COL_NAME + "=?",
                new String[]{demoName}, null, null, null);
        if (c.moveToFirst()) {
            demoId = c.getLong(0);
        }
        c.close();
        db.close();
        return demoId;
    }


    /**
     * Sets the global variable demo Id by demo name.
     * Demo Id associated to this name will be assigned to the global variable
     *
     * @param demoName
     */
    public void setDemoIDByName(String demoName) {
        mDemoId = getDemoIDByName(demoName);
    }

    /**
     * Prepare the iteration of the actions associated to the demo Id global variable
     *
     * @return
     */
    public boolean prepareActionsIteration() {
        return this.prepareActionsIteration(mDemoId);
    }

    /**
     * Prepares the actions iteration for the given demo ID
     *
     * @param demoid
     * @return
     */
    public boolean prepareActionsIteration(long demoid) {
        SQLiteDatabase db = this.getReadableDatabase();
        mDemoActionsId = new ArrayList<Long>();
        String[] columns = new String[]{COL_DEMO_ACTION_ID};
        Cursor c = db.query(DEMO_ACTIONS_TABLE, columns, COL_DEMOID + "=?",
                new String[]{String.valueOf(demoid)}, null, null, null);
        if (c.moveToFirst()) {
            mDemoActionsId.add(c.getLong(0));
            while (c.moveToNext()) {
                mDemoActionsId.add(c.getLong(0));
            }
        } else {
            return false;
        }
        mIterationReady = true;
        c.close();
        return true;
    }


    /**
     * Returns a DemoAction object with the data of the next movement in database
     *
     * @return
     */
    public DemoAction getNextAction() {

        if (!mIterationReady || mIterator >= mDemoActionsId.size()) return null;
        DemoAction action = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{COL_POWER, COL_TURNRATIO, COL_TACHOLIMIT, COL_DURATION};
        Cursor c = db.query(DEMO_ACTIONS_TABLE, columns, COL_DEMO_ACTION_ID + "=?",
                new String[]{String.valueOf(mDemoActionsId.get(mIterator))}, null, null, null);
        if (c.moveToFirst()) {
            mIterator++;
            action = new DemoAction(
                    c.getInt(0),
                    c.getInt(1),
                    c.getInt(2),
                    c.getInt(3));

        }
        c.close();
        return action;
    }

    /**
     * Finish the actions iteration
     */
    public void finishActionsIteration() {
        mIterator = 0;
        mDemoActionsId = null;
        this.getReadableDatabase().close();
    }

    /**
     * Sets the global demo id variable
     *
     * @param demoId
     */
    public void setDemoId(long demoId) {
        this.mDemoId = demoId;
    }

    /**
     * Returns the global demo id variable
     *
     * @return
     */
    public long getDemoId() {
        return this.mDemoId;
    }

    /**
     * Deletes the demo with the given name from the database
     *
     * @param demoName
     * @return
     */
    public boolean deleteDemo(String demoName) {
        long demoId = this.getDemoIDByName(demoName);
        if (demoId == -1) return false;
        return this.deleteDemo(demoId);
    }

    /**
     * Deletes the demo with the given demoId from the database
     *
     * @param demoId
     * @return
     */
    public boolean deleteDemo(long demoId) {
        int rtn = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DEMO_ACTIONS_TABLE, COL_DEMOID + "=?", new String[]{String.valueOf(demoId)});
        rtn = db.delete(DEMO_TABLE, COL_ID + "=?", new String[]{String.valueOf(demoId)});
        db.close();
        if (rtn != 0)
            return true;
        else
            return false;
    }
}
