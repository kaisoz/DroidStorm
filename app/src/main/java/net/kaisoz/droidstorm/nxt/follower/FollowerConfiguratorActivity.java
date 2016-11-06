package net.kaisoz.droidstorm.nxt.follower;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.bluetooth.BluetoothManager;
import net.kaisoz.droidstorm.bluetooth.Connection;
import net.kaisoz.droidstorm.bluetooth.ConnectionBase;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.nxt.Interface.MiscInterface;
import net.kaisoz.droidstorm.nxt.message.GenericResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This activity allows the user to select which one of both connected robots will be the follower.
 * When the user has selected the follower, it starts FileSelectActivity in order to allow him or she to select which program
 * installed in the follower should be started
 * Once the user has selected the program in FileSelectActivity, it passes the name back to this activity which will start it
 *
 * @author Tomás Tormo Fracno
 */
public class FollowerConfiguratorActivity extends Activity {

    protected static final int FILE_MODE_UPLOAD = 1;
    protected static final int FILE_MODE_SELECT = 2;
    private static final int FILE_RUNNER_ACTIVITY = 0;
    private static final int FILE_SELECT_ACTIVITY = 1;
    private BluetoothManager mManager;
    private ListView mNXTRobots;
    private String mLeaderAddr;
    private String mFollowerAddr;
    private List<String> mAdapterData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leader_selector);
        mManager = BluetoothManager.getInstance();

        mNXTRobots = (ListView) findViewById(R.id.nxtConn);
        mAdapterData = new ArrayList<String>();
        mAdapterData.addAll(Arrays.asList(mManager.getAllNames()));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mAdapterData);
        mNXTRobots.setAdapter(adapter);
        Toast.makeText(FollowerConfiguratorActivity.this, R.string.followerConf_toast_selectFollower, Toast.LENGTH_LONG).show();

        mNXTRobots.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

                // Get follower address
                mFollowerAddr = (String) mNXTRobots.getItemAtPosition(position);
                mAdapterData.remove(mFollowerAddr);
                mFollowerAddr = mManager.getAddressesByNames(new String[]{mFollowerAddr})[0];
                // Get leader address
                mLeaderAddr = (String) mAdapterData.get(0);
                mLeaderAddr = mManager.getAddressesByNames(new String[]{mLeaderAddr})[0];

                Intent i = new Intent(FollowerConfiguratorActivity.this, net.kaisoz.droidstorm.nxt.follower.FileSelectActivity.class);
                i.putExtra("followerAddr", mFollowerAddr);
                i.putExtra("leaderAddr", mLeaderAddr);
                FollowerConfiguratorActivity.this.startActivityForResult(i, FILE_SELECT_ACTIVITY);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_RUNNER_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    int fileMode = (Integer) data.getExtras().get("fileUploadMode");
                    Intent i = new Intent(FollowerConfiguratorActivity.this, net.kaisoz.droidstorm.nxt.follower.FileSelectActivity.class);
                    i.putExtra("followerAddr", mFollowerAddr);
                    FollowerConfiguratorActivity.this.startActivityForResult(i, FILE_SELECT_ACTIVITY);
                }
                break;

            default:
                if (resultCode == RESULT_OK) {
                    String selectedFile = (String) data.getExtras().get("selectedFile");
                    new StartTask(selectedFile).execute(mFollowerAddr);
                }
                break;
        }
    }


    /**
     * Nested class used to start a program remotelly in the follower robot
     * It's implemented as an asyncTask in order to assure that the program flow doesn't get blocked
     *
     * @author root
     */
    private class StartTask extends AsyncTask<String, Void, GenericResponse> {
        private AlertDialog mAlert = null;
        private String mFileName = null;

        protected StartTask(String file) {
            mFileName = file;
        }

        protected void onPreExecute() {
            String title = (String) FollowerConfiguratorActivity.this.getText(R.string.robotComm_alert_title);
            String msg = (String) FollowerConfiguratorActivity.this.getText(R.string.followerConf_alert_starting_msg);
            msg += " " + mFileName + "...";
            mAlert = ProgressDialog.show(FollowerConfiguratorActivity.this, title, msg, true);
        }

        protected GenericResponse doInBackground(String... args) {
            try {
                Connection conn = mManager.getConnection(args[0]);
                MiscInterface starter = MiscInterface.getInstance();
                starter.setConnection(conn);
                return starter.startProgram(mFileName, MiscInterface.MESSAGETYPE_RESPONSE);
            } catch (BluetoothException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(GenericResponse response) {
            mAlert.dismiss();
            if (response != null) {
                if (response.getStatus() == MiscInterface.RESPONSE_SUCCESS) {
                    showSuccessDialog();
                } else {
                    showErrorDialog();
                }
            } else {
                showErrorDialog();
            }
        }

        private void showErrorDialog() {
            String msg = (String) FollowerConfiguratorActivity.this.getText(R.string.followerConf_alert_started_error_msg) + " " + mFileName;
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FollowerConfiguratorActivity.this).setTitle(R.string.robotComm_alert_response_title)
                    .setMessage(msg);
            alertDialog.setPositiveButton(R.string.alert_button_positive, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    FollowerConfiguratorActivity.this.setResult(RESULT_CANCELED, null);
                    FollowerConfiguratorActivity.this.finish();
                    return;
                }
            });
            alertDialog.create().show();
        }

        private void showSuccessDialog() {
            String msg = mFileName + " " + (String) FollowerConfiguratorActivity.this.getText(R.string.followerConf_alert_started_success_msg);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FollowerConfiguratorActivity.this).setTitle(R.string.robotComm_alert_response_title)
                    .setMessage(msg);
            alertDialog.setPositiveButton(R.string.alert_button_positive, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent data = new Intent();
                    data.putExtra("leaderAddr", mLeaderAddr);
                    data.putExtra("followerAddr", mFollowerAddr);
                    FollowerConfiguratorActivity.this.setResult(RESULT_OK, data);
                    FollowerConfiguratorActivity.this.finish();
                    return;
                }
            });
            alertDialog.create().show();
        }
    }
}
