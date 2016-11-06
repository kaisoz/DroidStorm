package net.kaisoz.droidstorm.nxt.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.kaisoz.droidstorm.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Activity which implements a file explorer.
 * Used to load an XML file from the local storage.
 * Its root point is the sdcard (/sdcard) and it filters results by .xml extension
 *
 * @author Tomás Tormo Franco
 */
public class FileExplorerActivity extends ListActivity {

    private List<String> mDirectoryEntries = new ArrayList<String>();
    private File mCurrentDirectory = new File("/sdcard");
    private String mFileExtension;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (this.getIntent() != null) {
            Intent i = this.getIntent();
            mFileExtension = (String) i.getExtras().get("fileExtension");
        }
        browseToRoot();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        String selectedFileString = this.mDirectoryEntries.get(position);
        if (selectedFileString.equals(".")) {
            // Refresh
            this.browseTo(this.mCurrentDirectory);
        } else if (selectedFileString.equals("..")) {
            this.upOneLevel();
        } else {
            File clickedFile = new File(this.mCurrentDirectory.getAbsolutePath()
                    + this.mDirectoryEntries.get(position));

            if (clickedFile != null)
                this.browseTo(clickedFile);
        }
    }


    /**
     * Browses to the root-directory of the file-system.
     */
    private void browseToRoot() {
        browseTo(new File("/sdcard"));
    }

    /**
     * Browses up one level according to the field: mCurrentDirectory
     */
    private void upOneLevel() {
        if (this.mCurrentDirectory.getParent() != null)
            this.browseTo(this.mCurrentDirectory.getParentFile());
    }

    /**
     * Browses to the given directory
     *
     * @param aDirectory Directory to browse to
     */
    private void browseTo(final File aDirectory) {
        if (aDirectory.isDirectory()) {
            this.mCurrentDirectory = aDirectory;
            fill(aDirectory.listFiles());
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this).setTitle(R.string.fileExplorer_alert_title_openFile)
                    .setMessage(this.getText(R.string.fileExplorer_alert_msg_openFile) + " " + aDirectory.getName() + "?");
            alert.setPositiveButton(R.string.alert_button_yes, new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    Intent returnData = new Intent();
                    returnData.putExtra("filePath", aDirectory.getAbsolutePath());
                    FileExplorerActivity.this.setResult(RESULT_OK, returnData);
                    FileExplorerActivity.this.finish();
                }
            });
            alert.setNegativeButton(R.string.alert_button_no, new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    return;
                }
            });
            alert.create().show();
        }
    }

    /**
     * Fills the mDirectoryEntries list with the files of the current directory
     *
     * @param files Files that the current directory contains
     */
    private void fill(File[] files) {
        this.mDirectoryEntries.clear();

        // Add the "." and the ".." == 'Up one level'
        try {
            Thread.sleep(10);
        } catch (InterruptedException e1) {
            // Wait for the clear operation to complete
            e1.printStackTrace();
        }
        this.mDirectoryEntries.add(".");
        if (this.mCurrentDirectory.getParent() != null)
            this.mDirectoryEntries.add("..");

        int currentPathStringLenght = this.mCurrentDirectory.getAbsolutePath().length();
        for (File file : files) {
            if (file.isDirectory() || file.getName().endsWith(mFileExtension))
                this.mDirectoryEntries.add(file.getAbsolutePath().substring(currentPathStringLenght));
            ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,
                    R.layout.file_upload_row, this.mDirectoryEntries);

            this.setListAdapter(directoryList);
        }
    }
}