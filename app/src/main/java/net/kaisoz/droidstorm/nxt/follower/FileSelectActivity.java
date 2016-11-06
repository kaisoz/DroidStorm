package net.kaisoz.droidstorm.nxt.follower;

import java.util.ArrayList;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.bluetooth.BluetoothManager;
import net.kaisoz.droidstorm.bluetooth.Connection;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.nxt.Interface.MiscInterface;
import net.kaisoz.droidstorm.nxt.message.FindFileResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This activity shows all the installed programs in the follower
 * First of all, it calls a nested async task to read all programs installed in the robot. The result is presented to the user
 * When the user selects a program, its name is passed back to the calling activity which will manages its start
 *
 * @author Tomás Tormo Franco
 */
public class FileSelectActivity extends Activity {

    private String mFollowerAddr;
    private ArrayList<String> mFileNames;
    ArrayAdapter<String> mAdapter;
    BluetoothManager mManager;
    private ListView mNamesList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_select);

        if (this.getIntent() != null) {
            Intent i = this.getIntent();
            mFollowerAddr = (String) i.getExtras().get("followerAddr");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_select);
        mManager = BluetoothManager.getInstance();

        mNamesList = (ListView) findViewById(R.id.fileList);
        mFileNames = new ArrayList<String>();
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFileNames);
        mNamesList.setAdapter(mAdapter);

        mNamesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                String selectedFile = (String) mNamesList.getItemAtPosition(position);
                Intent returnData = new Intent();
                returnData.putExtra("selectedFile", selectedFile);
                setResult(RESULT_OK, returnData);
                finish();
            }
        });

        new ReadRobotFiles().execute(mFollowerAddr);
    }


    /**
     * Nested class used to read all installed programs in the follower robot
     * It's implemented using an AsyncTask which assures that the program flow doesn't get blocked during the reading
     *
     * @author root
     */
    private class ReadRobotFiles extends AsyncTask<String, Void, Boolean> {
        private AlertDialog alert = null;
        private FindFileResponse response = null;
        private int handle = 0;
        private int nextHandle = 0;

        protected void onPreExecute() {
            String title = (String) FileSelectActivity.this.getText(R.string.robotComm_alert_title);
            String msg = (String) FileSelectActivity.this.getText(R.string.fileSelect_alert_msg);
            alert = ProgressDialog.show(FileSelectActivity.this, title, msg, true);
        }

        protected Boolean doInBackground(String... address) {
            try {
                Connection conn = mManager.getConnection(address[0]);
                MiscInterface fileReader = MiscInterface.getInstance();
                fileReader.setConnection(conn);

                response = fileReader.findFirst("*.rxe");
                mFileNames.add(response.getFileName());
                handle = (int) response.getHandle();
                while (response.getStatus() != FindFileResponse.FILE_NOT_FOUND) {
                    response = fileReader.findNext(handle);
                    nextHandle = (int) response.getHandle();
                    mFileNames.add(response.getFileName());
                    fileReader.closeHandle(handle);
                    handle = nextHandle;
                }
                fileReader.closeHandle(handle);
                return true;
            } catch (BluetoothException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Boolean response) {
            alert.dismiss();
            if (response) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
