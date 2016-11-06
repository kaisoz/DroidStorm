package net.kaisoz.droidstorm.nxt.demo;

import java.util.ArrayList;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.util.DatabaseHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity which shows to the user all the demos stored in the database.
 * Allows the user to load, rename or delete a demo
 *
 * @author Tomás Tormo Franco
 */
public class DemoSelectorActivity extends Activity {

    private ListView mDemoListView;
    private ArrayList<String> mAdapterData;
    DatabaseHelper mDbHelper = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_selector);

        mDemoListView = (ListView) findViewById(R.id.demoList);
        registerForContextMenu(mDemoListView);
        mDbHelper = new DatabaseHelper(this);
        mAdapterData = mDbHelper.getAllDemoNames();
        if (mAdapterData.size() == 0) {
            mAdapterData.add((String) this.getText(R.string.DemoSel_adapter_noDemos));
        } else {
            mDemoListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

                    // Get follower address
                    String demoName = (String) mDemoListView.getItemAtPosition(position);
                    long demoId = mDbHelper.getDemoIDByName(demoName);

                    Intent returnData = new Intent();
                    returnData.putExtra("demoId", demoId);
                    setResult(RESULT_OK, returnData);
                    finish();
                }
            });
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mAdapterData);
        mDemoListView.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add("Rename");
        menu.add("Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        TextView name = (TextView) info.targetView;

        if (item.getTitle().toString().equals("Rename")) {
            long demoId = mDbHelper.getDemoIDByName(name.getText().toString());
            nameDialog(demoId, name.getText().toString());
        } else {
            long demoId = mDbHelper.getDemoIDByName(name.getText().toString());
            boolean rtn = mDbHelper.deleteDemo(demoId);
            if (rtn) {
                Toast.makeText(this, "Demo \"" + name.getText().toString() + "\" deleted successfully", Toast.LENGTH_SHORT).show();
                refresh();
            } else {
                Toast.makeText(this, "Error deleting demo \"" + name.getText().toString() + "\" ", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu_demosel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean rtnEnd = false;
        switch (item.getItemId()) {
            case R.id.delAllDemos:
                for (String demoName : mAdapterData) {
                    long demoId = mDbHelper.getDemoIDByName(demoName);
                    boolean rtn = mDbHelper.deleteDemo(demoId);
                    if (!rtn) rtnEnd = false;
                    if (rtn) {
                        Toast.makeText(this, "All demos deleted successfully", Toast.LENGTH_SHORT).show();
                        refresh();
                    } else {
                        Toast.makeText(this, "Error deleting some demos", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * Shows a dialog where the user can rename de demo
     *
     * @param demoId  demoId of the demo that is going to be renamed
     * @param oldName Previous name of the demo
     */
    protected void nameDialog(final long demoId, final String oldName) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Demo");
        alert.setMessage("Set demo name");

        // Set an EditText view to get user input 
        final EditText input = new EditText(this.getApplicationContext());
        input.setText(oldName);
        alert.setView(input);

        alert.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                mDbHelper.setDemoName(demoId, value.toString());
                refresh();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                return;
            }
        });

        alert.show();
    }

    /**
     * Re-launches the activity in order to reload the demos from database
     */
    protected void refresh() {
        finish();
        Intent myIntent = new Intent(this, this.getClass());
        startActivity(myIntent);
    }

}
