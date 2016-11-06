
package net.kaisoz.droidstorm.nxt.handler;

import java.io.FileInputStream;
import java.util.ArrayList;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.nxt.controller.NXTBaseController;
import net.kaisoz.droidstorm.nxt.demo.DemoAction;
import net.kaisoz.droidstorm.nxt.demo.DemoXMLManager;
import net.kaisoz.droidstorm.util.DatabaseHelper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which implements the demo mode of the application
 * It allows the user to select the demo to be loaded from two sources: database or XML file.
 * <p>
 * When a demo is loaded either from the database or a XML file and the user has started it, a new thread is started.
 * In case of the database loading , this thread reads all the actions from the database and prints its parameters just before sending a command to the robot.
 * In case of XML loading, the thread reads an array list of DemoActions prepared previously from the XML file reading.
 * <p>
 * Both loading modes have been implemented as a thread because they need to be interruptable for follower mode. When a new message from the
 * follower is received, the running thread is interrupted and robot control passes to the followerListener async task, which will move the leader
 * to make it visible. Once the follower is ready, the thread is notified and it continues moving the robot
 *
 * @author Tomás Tormo Franco
 */
public class DemoHandlerActivity extends NXTHandlerBaseActivity {

    private static final int THREAD_RUNNING = 0;
    private static final int THREAD_SUSPENDED = 1;
    private static final int THREAD_STOPPED = -1;
    private static final int THREAD_NOINIT = -2;
    private static final int DATA_DB = 0;
    private static final int DATA_XML = 1;
    private static final int FILEEXPLORER_ACTIVITY = 1;
    private static final int DEMOSELECTOR_ACTIVITY = 2;

    private static int mThreadState = THREAD_NOINIT;
    private Button mStartDemo;
    private Button mLoadXML;
    private Button mLoadDB;
    private TextView mCommands;
    private ScrollView mScrollView;
    private ArrayList<DemoAction> mDemoActions;
    private Handler mHandler = new Handler();
    private String mLeaderAddr;
    private String mFollowerAddr;
    private static int mDemoDataSource = -1;
    private static final Object mThreadLock = new Object();
    private XMLDemoStarter mXMLStarter;
    private DBDemoStarter mDbStarter;
    private DatabaseHelper mDBHelper;
    private long mDemoId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(R.string.activity_label_demohandler);
        setContentView(R.layout.demo_handler);

        mController = new NXTBaseController();
        mCommands = (TextView) findViewById(R.id.demo_handler_commands);
        mScrollView = (ScrollView) findViewById(R.id.demo_handler_scrollview);

        mStartDemo = (Button) findViewById(R.id.demo_handler_Start);
        mStartDemo.setEnabled(false);
        mStartDemo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCommands.setText("");
                if (mMode == MODE_FOLLOW) {
                    if (mThreadState == THREAD_NOINIT) {
                        mThreadState = THREAD_STOPPED;
                        enableFollowerMode(mLeaderAddr, mFollowerAddr);
                    } else {
                        runListener(mFollowerAddr);
                        enableHandler();
                    }
                } else {
                    mThreadState = THREAD_STOPPED;
                    mNXTReady = true;
                    enableHandler();
                }
            }
        });

        mLoadXML = (Button) findViewById(R.id.demo_handler_loadXML);
        mLoadXML.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mNXTReady = false;
                Intent i = new Intent(DemoHandlerActivity.this, net.kaisoz.droidstorm.nxt.demo.FileExplorerActivity.class);
                i.putExtra("fileExtension", ".xml");
                DemoHandlerActivity.this.startActivityForResult(i, FILEEXPLORER_ACTIVITY);
            }
        });

        mLoadDB = (Button) findViewById(R.id.demo_handler_loadDB);
        mLoadDB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mNXTReady = false;
                Intent i = new Intent(DemoHandlerActivity.this, net.kaisoz.droidstorm.nxt.demo.DemoSelectorActivity.class);
                DemoHandlerActivity.this.startActivityForResult(i, DEMOSELECTOR_ACTIVITY);
            }
        });
    }

    @Override
    protected boolean disableHandler() {
        if (mThreadState == THREAD_RUNNING) {
            mThreadState = THREAD_SUSPENDED;
            mXMLStarter.interrupt();
        }
        return false;
    }

    @Override
    protected boolean enableHandler() {
        if (mThreadState == THREAD_SUSPENDED) {
            mThreadState = THREAD_RUNNING;
            synchronized (mThreadLock) {
                mThreadLock.notify();
            }
        } else if (mThreadState == THREAD_STOPPED && mNXTReady) {
            if (mDemoDataSource == DATA_XML) {
                mXMLStarter = new XMLDemoStarter();
                mXMLStarter.start();
                mThreadState = THREAD_RUNNING;
            } else if (mDemoDataSource == DATA_DB) {
                mDBHelper = new DatabaseHelper(this);
                mDbStarter = new DBDemoStarter(mDBHelper, mDemoId);
                mDbStarter.start();
                mThreadState = THREAD_RUNNING;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILEEXPLORER_ACTIVITY:
                try {
                    String filePath = (String) data.getExtras().get("filePath");
                    DemoXMLManager loader = new DemoXMLManager();
                    loader.setInputStream(new FileInputStream(filePath));
                    mDemoActions = (ArrayList<DemoAction>) loader.parse();

                    if (mDemoActions.size() > 0) {
                        mStartDemo.setEnabled(true);
                    } else {
                        Toast.makeText(this, R.string.DemoHandler_NoActionsLoaded, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(this, R.string.DemoHandler_ErrorLoadXML, Toast.LENGTH_LONG).show();
                }
                mDemoDataSource = DATA_XML;
                break;
            case LEADER_SELECTOR_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    mMode = MODE_FOLLOW;
                    mLeaderAddr = (String) data.getExtras().get("leaderAddr");
                    mFollowerAddr = (String) data.getExtras().get("followerAddr");
                }
                break;
            case DEMOSELECTOR_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    mDemoId = (Long) data.getExtras().get("demoId");
                    if (mDemoId != -1) {
                        mStartDemo.setEnabled(true);
                        mDemoDataSource = DATA_DB;
                    } else {
                        Toast.makeText(this, R.string.DemoHandler_CoulndtLoadDB, Toast.LENGTH_LONG).show();
                    }
                }
        }
    }

    /**
     * Nested class used to move the robot using movements recorded in the database
     * It's implemented as a thread to be able to be interrupted and stopped since the AsyncTask can't do it
     * Before sending each command to the robot, it prints the parameters into the screen thanks to a handler instantiated before
     *
     * @author tomtorfr
     */
    private class DBDemoStarter extends Thread {

        private DatabaseHelper db;
        private long demoId;

        /**
         * Creates the thread
         *
         * @param db     Helper object used to access the database
         * @param demoId demoId of the demo selected
         */
        public DBDemoStarter(DatabaseHelper db, long demoId) {
            this.db = db;
            this.demoId = demoId;
        }

        /**
         * Prints movement data to the screen
         *
         * @param data String with the data to be printed into the screen
         */
        private void postData(final String data) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCommands.append(data);
                    mScrollView.scrollTo(0, mCommands.getHeight());
                }
            });
        }

        /**
         * Starts the thread
         */
        public void run() {

            postData(DemoHandlerActivity.this.getText(R.string.DemoHandler_StartingDemo) + "\n\n");
            db.prepareActionsIteration(demoId);
            DemoAction action = null;

            try {

                while ((action = db.getNextAction()) != null) {

                    try {

                        int power = action.getPower();
                        int turnRatio = action.getTurnRatio();
                        long delay = action.getDelay();
                        postData("Power: " + power + ". TurnRatio: " + turnRatio + ". Duration: " + delay + "ms\n");
                        mController.move(turnRatio, power);

                        if (delay != 0)
                            Thread.sleep(delay);
                        postData("\n");

                    } catch (InterruptedException interr) {
                        if (mThreadState == THREAD_STOPPED) {
                            mController.stop();
                        } else {
                            try {
                                mController.stop();
                                synchronized (mThreadLock) {
                                    mThreadLock.wait();
                                }
                                mThreadState = THREAD_RUNNING;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                postData("\nStop");
                mController.stop();
                stopListener();
                db.finishActionsIteration();
                mThreadState = THREAD_STOPPED;

            } catch (BluetoothException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Nested class used to move the robot using the movements read from a XML file
     * It's implemented as a thread to be able to be interrupted and stopped since the AsyncTask can't do it
     * Before sending each command to the robot, it prints the parameters into the screen thanks to a handler instantiated before
     *
     * @author tomtorfr
     */
    private class XMLDemoStarter extends Thread {

        /**
         * Prints movement data to the screen
         *
         * @param data String with the data to be printed into the screen
         */
        private void postData(final String data) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCommands.append(data);
                    mScrollView.scrollTo(0, mCommands.getHeight());
                }
            });
        }

        /**
         * Starts the thread
         */
        public void run() {

            postData("Starting demo\n\n");
            try {

                for (int i = 0; i < mDemoActions.size(); i++) {

                    try {

                        DemoAction action = mDemoActions.get(i);
                        int power = action.getPower();
                        int turnRatio = action.getTurnRatio();
                        long delay = action.getDelay();
                        postData("Power: " + power + ". TurnRatio: " + turnRatio + ". Delay: " + (delay / 1000) + " seconds\n");
                        mController.move(turnRatio, power);

                        if (delay != 0)
                            Thread.sleep(delay);
                        postData("\n");

                    } catch (InterruptedException interr) {
                        if (mThreadState == THREAD_STOPPED) {
                            mController.stop();
                        } else {
                            try {
                                mController.stop();
                                synchronized (mThreadLock) {
                                    mThreadLock.wait();
                                }
                                mThreadState = THREAD_RUNNING;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                postData("\nStop");
                mController.stop();
                stopListener();
                mThreadState = THREAD_STOPPED;

            } catch (BluetoothException e) {
                e.printStackTrace();
            }
        }

    }

}