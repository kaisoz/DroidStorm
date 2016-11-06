package net.kaisoz.droidstorm.connmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.util.IndexedMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Adapter which holds the data shown in the lists.
 * Composed by two lists: discovered devices and connected devices
 *
 * @author Tomás Tormo Franco
 */
public class DevicesAdapter extends BaseExpandableListAdapter {

    public static int GROUP_FOUND = 0;
    public static int GROUP_CONNECTED = 1;
    public static int GROUP_ALL = 2;
    private IndexedMap mData = null;
    private HashSet<Object> mFoundSelected = null;
    private HashSet<Object> mConnectedSelected = null;
    private String mFoundStr;
    private String mDevStr;
    private String mConnStr;
    private LayoutInflater mInflater;

    public DevicesAdapter(Context context) {
        mFoundStr = (String) context.getText(R.string.devAdapter_foundString);
        mDevStr = (String) context.getText(R.string.devAdapter_devicesString);
        mConnStr = (String) context.getText(R.string.devAdapter_connectedString);
        mFoundSelected = new HashSet<Object>();
        mConnectedSelected = new HashSet<Object>();
        mInflater = LayoutInflater.from(context);
        buildDataStructure(null, null);
    }

    /**
     * Creates the initial data structure
     *
     * @param found     Map which will mantain found devices
     * @param connected Map which will mantain connected devices
     */
    private void buildDataStructure(ArrayList found, ArrayList connected) {
        mData = new IndexedMap();
        if (found == null) found = new ArrayList();
        if (connected == null) connected = new ArrayList();
        mData.put(mFoundStr.concat(" ").concat(String.valueOf(found.size())).concat(" ").concat(mDevStr), found);
        mData.put(mConnStr, connected);
    }

    /**
     * Returns all the data that must persist in memory when this activity dies (found/connected devices)
     *
     * @return ArrayList which, in turn, contains two arraylists more with the found and connected devices
     */
    public ArrayList getCollectableData() {
        ArrayList collectData = new ArrayList();
        collectData.add(mFoundSelected);
        collectData.add((ArrayList) mData.getValue(GROUP_FOUND));
        collectData.add((ArrayList) mData.getValue(GROUP_CONNECTED));
        return collectData;
    }

    /**
     * Fills the data structure with the data loaded from memory (found/connected devices)
     *
     * @param collectData
     */
    public void loadFromCollectableData(ArrayList collectData) {
        mFoundSelected = (HashSet<Object>) collectData.get(0);
        ArrayList groupFound = (ArrayList) collectData.get(1);
        ArrayList groupConnected = (ArrayList) collectData.get(2);
        buildDataStructure(groupFound, groupConnected);
    }

    /**
     * Returns the number of the devices selected by the user from the "found" group
     *
     * @return
     */
    public int getNumFoundSelected() {
        return mFoundSelected.size();
    }

    /**
     * Returns the number of the devices selected by the user from the "connected" group
     *
     * @return
     */
    public int getNumConnectedSelected() {
        return mConnectedSelected.size();
    }

    /**
     * Returns the names of the devices selected by the user from the "connected" group
     *
     * @return
     */
    public String[] getConnectedSelectedNames() {
        return mConnectedSelected.toArray(new String[mConnectedSelected.size()]);
    }

    /**
     * Returns the names of the devices selected by the user from the "found" group
     *
     * @return
     */
    public String[] getFoundSelected() {
        return mFoundSelected.toArray(new String[mFoundSelected.size()]);
    }

    /**
     * Returns the names of the connected devices
     *
     * @return
     */
    public String[] getConnectedNames() {
        ArrayList connDevices = (ArrayList) mData.getValue(GROUP_CONNECTED);
        return (String[]) connDevices.toArray(new String[connDevices.size()]);
    }

    /**
     * Toggles the checkbock associated to a displayed device name from the "found" group
     *
     * @return
     */
    public void toggleFoundDevice(String devName) {
        if (mFoundSelected.contains(devName)) {
            mFoundSelected.remove(devName);
        } else {
            mFoundSelected.add(devName);
        }
    }

    /**
     * Toggles the checkbock associated to a displayed device name from the "connected" group
     *
     * @return
     */
    public void toggleConnectedDevice(String devName) {
        if (mConnectedSelected.contains(devName)) {
            mConnectedSelected.remove(devName);
        } else {
            mConnectedSelected.add(devName);
        }
    }

    /**
     * Sets the found devices names in the "found" group
     *
     * @return
     */
    public void setFoundDevices(String[] devices) {
        // Do not show devices that are already connected
        ArrayList<String> devicesAsList = new ArrayList<String>();
        devicesAsList.addAll(Arrays.asList(devices));
        ArrayList foundGrp = (ArrayList) mData.getValue(GROUP_FOUND);
        ArrayList connGrp = (ArrayList) mData.getValue(GROUP_CONNECTED);
        if (connGrp != null) {
            devicesAsList.removeAll(connGrp);
        }

        foundGrp.clear();
        foundGrp.addAll(devicesAsList);
        mData.replaceKey(GROUP_FOUND, "Found " + devicesAsList.size() + " devices");
    }

    /**
     * Sets the connected devices names in the "connected" group
     *
     * @return
     */
    public void setConnectedDevices(String[] devices) {
        // Remove devices from found list
        List devicesAsList = Arrays.asList(devices);
        ArrayList foundGroup = (ArrayList) mData.getValue(GROUP_FOUND);
        ArrayList connGroup = (ArrayList) mData.getValue(GROUP_CONNECTED);

        mFoundSelected.removeAll(Arrays.asList(devices));
        mConnectedSelected.addAll(Arrays.asList(devices));
        foundGroup.removeAll(devicesAsList);
        connGroup.addAll(devicesAsList);

        mData.replaceKey(GROUP_FOUND, "Found " + foundGroup.size() + " devices");
        mData.put(GROUP_FOUND, foundGroup);
        mData.put(GROUP_CONNECTED, connGroup);
    }


    /**
     * Empties "found" group
     */
    public void clearFound() {
        // Vaciamos los datos del grupo de encontrados
        ArrayList foundGrp = (ArrayList) mData.getValue(GROUP_FOUND);
        foundGrp.clear();
        mData.put(GROUP_FOUND, foundGrp);
        if (mFoundSelected != null) mFoundSelected.clear();
    }

    /**
     * Clears selected devices from "found" group
     *
     * @return
     */
    public void clearFoundSelected() {
        if (mFoundSelected != null) mFoundSelected.clear();
    }

    /**
     * Clears selected devices from "connected" group
     *
     * @return
     */
    public void clearConnectedSelected() {
        if (mConnectedSelected != null) mConnectedSelected.clear();
    }

    /**
     * Clears connected devices by name
     *
     * @param names
     */
    public void clearConnected(String[] names) {
        // Vaciamos los datos del grupo de encontrados
        List namesAsList = Arrays.asList(names);
        ArrayList connected = (ArrayList) mData.getValue(GROUP_CONNECTED);
        if (connected != null) {
            connected.removeAll(namesAsList);
            mData.put(GROUP_CONNECTED, connected);
            if (mConnectedSelected != null) mConnectedSelected.removeAll(namesAsList);
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList children = (ArrayList) mData.getValue(groupPosition);
        if (children != null) {
            return children.get(childPosition);
        } else {
            return null;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (long) (groupPosition * 1024 + childPosition);  // Max 1024 children per group
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = null;
        if (convertView != null)
            v = convertView;
        else
            v = mInflater.inflate(R.layout.child_row, parent, false);

        String devName = (String) getChild(groupPosition, childPosition);
        TextView device = (TextView) v.findViewById(R.id.devname);
        if (device != null) {
            device.setText(devName);
            CheckBox cb = (CheckBox) v.findViewById(R.id.check);
            cb.setClickable(false);
            TextView bdaddr = (TextView) v.findViewById(R.id.bdaddr);
            if (mFoundSelected.contains(devName) || mConnectedSelected.contains(devName)) {
                cb.setChecked(true);
            } else {
                cb.setChecked(false);
            }
        }
        return v;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList children = (ArrayList) mData.getValue(groupPosition);
        if (children == null)
            return 0;
        else
            return children.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mData.getKey(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return (long) (groupPosition * 1024);  // To be consistent with getChildId
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = null;
        if (convertView != null)
            v = convertView;
        else
            v = mInflater.inflate(R.layout.group_row, parent, false);
        String gt = (String) getGroup(groupPosition);
        TextView groups = (TextView) v.findViewById(R.id.childname);
        if (gt != null)
            groups.setText(gt);
        return v;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void onGroupCollapsed(int groupPosition) {
    }

    public void onGroupExpanded(int groupPosition) {
    }


}
