package com.mv2studio.myride.connection

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.github.pires.obd.exceptions.UnsupportedCommandException
import com.mv2studio.myride.extensions.d
import com.mv2studio.myride.extensions.e
import com.mv2studio.myride.extensions.w
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

/**
 * Service which handles connection to bluetooth ODB device.
 *
 * Heavily inspired by
 * https://github.com/pires/android-obd-reader/blob/master/src/main/java/com/github/pires/obd/reader/io/ObdGatewayService.java
 */
class ConnectionService : Service() {

    private val mBinder = Binder()

    private var mConnectionConsumer: IConnectionConsumer? = null
    var currentConnectionState = ConnectionState.UNKNOWN
        private set

    private var dev: BluetoothDevice? = null
    private var socket: BluetoothSocket? = null
    private val mCommandsQueue = LinkedBlockingQueue<Command>()

    // handler for delivering state updates to UI thread.
    private val t = thread(start = false, block = { executeQueue() })

    inner class Binder : android.os.Binder() {
        val boundService: ConnectionService
            get() = this@ConnectionService
    }

    /**
     * Binds consumer to the service. This consumer is later notified about connection state change
     * and all command results.
     */
    fun bindConnectionConsumer(consumer: IConnectionConsumer) {
        mConnectionConsumer = consumer
    }

    override fun onCreate() {
        super.onCreate()
        val btAdapter = BluetoothAdapter.getDefaultAdapter()

        if (btAdapter == null) {
            // Bluetooth is not available. we're doomed.
            // TODO wish user a good ride and kill the app, I suppose?
            stopService()
            return
        }

        if (!btAdapter.isEnabled) {
            registerReceiver(mBluetoothStateChangedReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            startConnection()
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBluetoothStateChangedReceiver)
    }

    private val mBluetoothStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED != intent?.action) return
            val state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
            d("Bluetooth received new state: $state")

            when (state) {
                BluetoothAdapter.STATE_TURNING_ON -> propagateState(ConnectionState.BT_TURNING_ON)
                BluetoothAdapter.STATE_ON -> {
                    propagateState(ConnectionState.BT_ON)
                    startConnection()
                }
                BluetoothAdapter.STATE_TURNING_OFF -> propagateState(ConnectionState.BT_TURNING_OFF)
                BluetoothAdapter.STATE_OFF -> propagateState(ConnectionState.BT_OFF)
                BluetoothAdapter.STATE_CONNECTING -> propagateState(ConnectionState.CONNECTING)
                BluetoothAdapter.STATE_CONNECTED -> propagateState(ConnectionState.CONNECTED)
                BluetoothAdapter.STATE_DISCONNECTING -> propagateState(ConnectionState.DISCONNECTING)
                BluetoothAdapter.STATE_DISCONNECTED -> propagateState(ConnectionState.DISCONNECTED)
            }
        }
    }

    fun reconnect() {
        startConnection()
    }

    /**
     * Initiates connection procedure to obd bt device in separate thread. Every status change is propagated via
     * {@link ConnectionService#propagateState(ConnectionState)}. Reconnecting is not done automatically yet.
     * To do so, please use {@link ConnectionService#reconnect()}
     */
    private fun startConnection() {
        thread {
            d("Starting connection service")
            // TODO Do not forget to set correct device id!
            val device = "12:34:56:88:B0:01"
            if (device.isNullOrEmpty()) {
                d("Cannot start connection. No device selected.")
                propagateState(ConnectionState.NO_DEVICE_SELECTED)
            }

            val adapter = BluetoothAdapter.getDefaultAdapter()
            dev = adapter.getRemoteDevice(device)
            adapter.cancelDiscovery()

            startOdbConnection()
        }
    }

    /**
     * Notifies bound {@link IConnectionConsumer} about connection status change.
     * Don't forget to bind your consumer via {@link ConnectionService#bindConnectionConsumer(IConnectionConsumer)}
     */
    private fun propagateState(connectionState: ConnectionState) {
        currentConnectionState = connectionState
        mConnectionConsumer?.onConnectionStateChanged(connectionState)
    }

    fun startOdbConnection(): Boolean {
        propagateState(ConnectionState.CONNECTING)
        socket = dev?.let { BluetoothManager.connect(it) }
        if (socket == null || !socket!!.isConnected) {
            e("I'm truly sorry, but connection to device did not work.")
            propagateState(ConnectionState.CANNOT_CONNECT)
            return false
        }

        // all should be good now.
        propagateState(ConnectionState.CONNECTED);

        // some initial commands to configure connection. I have totally no idea what's going on.
        // ask this smart guy here, looks like he knows what's he doing:
        // https://github.com/pires/android-obd-reader/blob/master/src/main/java/com/github/pires/obd/reader/io/ObdGatewayService.java#L125
        enqueueCommand(Command(CommandType.ODB_RESET));
        enqueueCommand(Command(CommandType.ECHO_OFF))
        enqueueCommand(Command(CommandType.ECHO_OFF))
        enqueueCommand(Command(CommandType.LINE_FEED_OFF))
        enqueueCommand(Command(CommandType.TIMEOUT))
        enqueueCommand(Command(CommandType.SELECT_PROTOCOL))

        t.start()

        return true
    }

    /**
     * Adds new {@link Command} to queue.
     */
    private fun enqueueCommand(command: Command) {
        val obdCommand = command.obdCommand

        try {
            mCommandsQueue.put(command)
            d("Command enqueued: " + obdCommand.getName());
        } catch (e: InterruptedException) {
            command.state = State.QUEUE_ERROR
            e("Could not enqueue new command: " + obdCommand.name)
        }
    }

    /**
     * Creates new default {@link Command} for specified {@link CommandType} and adds it to queue.
     */
    fun enqueueCommand(commandType: CommandType) {
        enqueueCommand(Command(commandType))
    }

    /**
     * This is Mordor of this Service. The center of the universe. All the power lies here.
     * It basically picks command by command from the queue and execute them.
     */
    fun executeQueue() {
        while (!Thread.currentThread().isInterrupted) {

            var command: Command? = null
            try {
                command = mCommandsQueue.take()

                if (command.state == State.NEW) {
                    command.state = State.RUNNING
                    if (socket!!.isConnected) {
                        command.obdCommand.run(socket?.inputStream, socket?.outputStream)
                    } else {
                        command.state = State.EXECUTION_ERROR
                        e("Could not execute command on closed socket.")
                    }
                } else {
                    e("Tried to execute command which did not have NEW state.")
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: UnsupportedCommandException) {
                command?.state = State.NOT_SUPPORTED
                w("Command not supported. " + e.message)
            } catch (e: IOException) {
                command?.state = if (e.message?.contains("Broken pipe") ?: false)
                    State.BROKEN_PIPE else State.EXECUTION_ERROR
                e("IOException while executing command: " + e.message)
            } catch (e: Exception) {
                command?.state = State.EXECUTION_ERROR
                e("Command could be not executed. " + e.message)
            }

            if (command != null) {
                mConnectionConsumer?.onNextCommand(command)
            }
        }
    }

    fun stopService() {
        mCommandsQueue.clear()
        try {
            socket?.close()
        } catch (e: IOException) {
            e(e.message)
        }

        // kill service
        stopSelf()
    }
}