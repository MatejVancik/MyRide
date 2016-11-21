package com.mv2studio.myride.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.mv2studio.myride.extensions.d
import com.mv2studio.myride.extensions.e
import java.io.IOException
import java.util.*

/**
 * Created by matej on 15/11/2016.
 */
class BluetoothManager {

    companion object {
        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        fun connect(device: BluetoothDevice): BluetoothSocket? {
            var socket: BluetoothSocket? = null

            d("Connecting to BT device.")
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID)
                socket.connect()
            } catch (ioe: IOException) {
                e("Could not connect to remote device. Trying some workaround.", ioe)
                val clazz = socket?.remoteDevice?.javaClass

                try {
                    val m = clazz?.getMethod("createRfcommSocket", Integer.TYPE)
                    socket = m?.invoke(socket?.remoteDevice, 1) as? BluetoothSocket?

                    //TODO If this will not work try adding Thread.sleep(500);
                    socket?.connect()
                    d("*sweating* Wooha! Fallback BT connection worked.")
                } catch (ex: Exception) {
                    e("Well, connection does not work. Don't freak out, but I really have no idea what to do now...", ex)
                }
            }

            return socket
        }
    }

}