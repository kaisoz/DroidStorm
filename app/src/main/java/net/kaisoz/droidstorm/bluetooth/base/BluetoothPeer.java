package net.kaisoz.droidstorm.bluetooth.base;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @author Tomás Tormo Franco
 */


@TargetApi(5)
public class BluetoothPeer {

    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String TAG = "BluetoothPeer";

    public BluetoothPeer(BluetoothDevice device) {
        this.mDevice = device;
    }

    public String getAddress() {
        return this.mDevice.getAddress();
    }

    public void connect() throws java.io.IOException {
        this.mSocket = this.mDevice.createRfcommSocketToServiceRecord(this.mUUID);
        this.mSocket.connect();
        this.mInputStream = this.mSocket.getInputStream();
        this.mOutputStream = this.mSocket.getOutputStream();
    }

    public void disconnect() throws java.io.IOException {
        this.mSocket.close();
    }


    private byte[] prepareMessage(char[] buffer) {
        int command_len = (buffer.length * 2);
        byte[] msg = new byte[(command_len + 2)];

        msg[0] = (byte) (command_len & 0xFF);
        msg[1] = (byte) ((command_len >> 8) & 0xFF);
        for (int i = 0, j = 2; i < buffer.length; i++, j += 2) {
            msg[j + 1] = (byte) (buffer[i] & 0xFF);
            msg[j] = (byte) ((buffer[i] >> 8) & 0xFF);
        }
        return msg;
    }

    private char[] processResponse(byte[] rsp_buffer) {
        char[] rsp;
        int rsp_len = (int) (rsp_buffer[1] << 8 | rsp_buffer[0]);

        if ((rsp_len % 2) == 0) {
            rsp = new char[rsp_len];
        } else {
            rsp = new char[rsp_len + 1];
        }

        for (int i = 0, j = 0; i < rsp_len; i++, j += 2) {
            rsp[i] = (char) ((rsp_buffer[j] & 0xFF) << 8);
            if (j != rsp_len - 1)
                rsp[i] |= (char) (rsp_buffer[j + 1] & 0xFF);
        }
        return rsp;
    }

    private void LogMessage(byte[] msg) {
        Log.d(TAG, "Sending message to address: " + this.getAddress());
        for (int i = 0; i < msg.length; i++) {
            Log.d(TAG, "B[" + i + "]: " + String.format("0x%02X", msg[i]));
        }
        Log.d(TAG, "-----------");
    }


    public void send(char[] values) throws BluetoothException {
        byte[] msg = this.prepareMessage(values);
        LogMessage(msg);

        try {
            this.mOutputStream.write(msg);
        } catch (Exception e) {
            throw new BluetoothException();
        }
    }

    public char[] receive() throws BluetoothException {
        char[] rsp = null;
        try {
            byte[] rsp_buffer = new byte[1024];
            this.mInputStream.read(rsp_buffer);
            rsp = this.processResponse(rsp_buffer);
        } catch (Exception e) {
            throw new BluetoothException();
        }

        return rsp;
    }
}
