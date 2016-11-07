package com.mv2studio.myride.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.mv2studio.myride.utils.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by matej on 30.10.16.
 */

public class BluetoothManager {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static BluetoothSocket connect(BluetoothDevice device) {
        if (device == null) return null;

        BluetoothSocket socket = null;

        Log.d("Connecting to BT device.");
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
            Log.d("Wooha! Looks like connection works!");
        } catch (IOException ioe) {
            Log.e("Could not connect to remote device. Trying some workaround.", ioe);
            Class<?> clazz = socket.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};

            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                socket = (BluetoothSocket) m.invoke(socket.getRemoteDevice(), params);
                // If this will not work try adding Thread.sleep(500);
                socket.connect();
                Log.d("*sweating* Wooha! Fallback BT connection worked.");
            } catch (Exception e) {
                Log.e("Well, connection does not work. Don't freak out, but I really have no idea what to do now...", e);
            }
        }

        return socket;
    }

}
