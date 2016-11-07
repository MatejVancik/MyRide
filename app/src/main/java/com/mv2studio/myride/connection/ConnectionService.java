package com.mv2studio.myride.connection;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.exceptions.UnsupportedCommandException;
import com.mv2studio.myride.utils.Log;
import com.mv2studio.myride.utils.Utils;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Service which handles connection to bluetooth ODB device.
 *
 * Heavily inspired by
 * https://github.com/pires/android-obd-reader/blob/master/src/main/java/com/github/pires/obd/reader/io/ObdGatewayService.java
 */
public class ConnectionService extends Service {

    private IBinder mBinder = new Binder();

    private IConnectionConsumer mConnectionConsumer;
    private ConnectionState mCurrentConnectionState = ConnectionState.UNKNOWN;

    private BluetoothDevice dev = null;
    private BluetoothSocket socket = null;
    private BlockingQueue<Command> mCommandsQueue = new LinkedBlockingQueue<>();

    // handler for delivering state updates to UI thread.
    private Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                executeQueue();
            } catch (InterruptedException e) {
                t.interrupt();
            }
        }
    });

    public class Binder extends android.os.Binder {
        ConnectionService getBoundService() {
            return ConnectionService.this;
        }
    }

    /**
     * Binds consumer to the service. This consumer is later notified about connection state change
     * and all command results.
     */
    public void bindConnectionConsumer(IConnectionConsumer consumer) {
        mConnectionConsumer = consumer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            // Bluetooth is not available. we're doomed.
            // TODO wish user a good ride and kill the app, I suppose?
            stopService();
            return;
        }

        if (!btAdapter.isEnabled()) {
            registerReceiver(mBluetoothStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableBtIntent);
        } else {
            startConnection();
        }
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothStateChangedReceiver);
    }

    private final BroadcastReceiver mBluetoothStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) return;
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            Log.d("Bluetooth received new state: " + state);

            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    propagateState(ConnectionState.BT_TURNING_ON);
                    break;
                case BluetoothAdapter.STATE_ON:
                    propagateState(ConnectionState.BT_ON);
                    startConnection();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    propagateState(ConnectionState.BT_TURNING_OFF);
                    break;
                case BluetoothAdapter.STATE_OFF:
                    propagateState(ConnectionState.BT_OFF);
                    break;
                case BluetoothAdapter.STATE_CONNECTING:
                    propagateState(ConnectionState.CONNECTING);
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    propagateState(ConnectionState.CONNECTED);
                    break;
                case BluetoothAdapter.STATE_DISCONNECTING:
                    propagateState(ConnectionState.DISCONNECTING);
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    propagateState(ConnectionState.DISCONNECTED);
                    break;

            }
        }
    };

    public void reconnect() {
        startConnection();
    }

    /**
     * Initiates connection procedure to obd bt device in separate thread. Every status change is propagated via
     * {@link ConnectionService#propagateState(ConnectionState)}. Reconnecting is not done automatically yet.
     * To do so, please use {@link ConnectionService#reconnect()}
     */
    private void startConnection() {
        final Thread initConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Starting connection service");

//        final String device = Storage.getString(Constants.STORAGE_BT_DEVICE);
                final String device = "12:34:56:88:B0:01";
                if (Utils.isEmpty(device)) {

                    Log.d("Cannot start connection. No device selected.");
                    propagateState(ConnectionState.NO_DEVICE_SELECTED);
                }

                final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                dev = adapter.getRemoteDevice(device);
                adapter.cancelDiscovery();

                startOdbConnection();
            }
        });
        initConnectionThread.start();
    }

    /**
     * Notifies bound {@link IConnectionConsumer} about connection status change.
     * Don't forget to bind your consumer via {@link ConnectionService#bindConnectionConsumer(IConnectionConsumer)}
     */
    private void propagateState(ConnectionState connectionState) {
        mCurrentConnectionState = connectionState;
        if (mConnectionConsumer != null) {
            mConnectionConsumer.onConnectionStateChanged(connectionState);
        }
    }

    /**
     * Public API to obtain current connection state.
     */
    public ConnectionState getCurrentConnectionState() {
        return mCurrentConnectionState;
    }

    /**
     * Initiates connection with obd module. Returns true if this succeeded, false otherwise.
     */
    private boolean startOdbConnection() {
        propagateState(ConnectionState.CONNECTING);
        socket = BluetoothManager.connect(dev);
        if (socket == null || !socket.isConnected()) {
            Log.e("I'm truly sorry, but connection to device did not work.");
            propagateState(ConnectionState.CANNOT_CONNECT);
            return false;
        }

        // all should be good now.
        propagateState(ConnectionState.CONNECTED);

        // some initial commands to configure connection. I have totally no idea what's going on.
        // ask this smart guy here, looks like he knows what's he doing:
        // https://github.com/pires/android-obd-reader/blob/master/src/main/java/com/github/pires/obd/reader/io/ObdGatewayService.java#L125
        enqueueCommand(new Command(CommandType.ODB_RESET));
        enqueueCommand(new Command(CommandType.ECHO_OFF));
        enqueueCommand(new Command(CommandType.ECHO_OFF));
        enqueueCommand(new Command(CommandType.LINE_FEED_OFF));
        enqueueCommand(new Command(CommandType.TIMEOUT));
        enqueueCommand(new Command(CommandType.SELECT_PROTOCOL));

        t.start();

        return true;
    }

    /**
     * Adds new {@link Command} to queue.
     */
    private void enqueueCommand(Command command) {
        final ObdCommand obdCommand = command.getObdCommand();

        try {
            mCommandsQueue.put(command);
            Log.d("Command enqueued: " + obdCommand.getName());
        } catch (InterruptedException e) {
            command.setState(CommandState.QUEUE_ERROR);
            Log.e("Could not enqueue new command: " + obdCommand.getName());
        }
    }

    /**
     * Creates new default {@link Command} for specified {@link CommandType} and adds it to queue.
     */
    public void enqueueCommand(CommandType commandType) {
        final Command command = new Command(commandType);
        enqueueCommand(command);
    }

    /**
     * This is Mordor of this Service. The center of the universe. All the power lies here.
     * It basically picks command by command from the queue and execute them.
     */
    private void executeQueue() throws InterruptedException{
        while (!Thread.currentThread().isInterrupted()) {
            Command command = null;

            try {
                command = mCommandsQueue.take();

                if (command.getState() == CommandState.NEW) {
                    command.setState(CommandState.RUNNING);
                    if (socket.isConnected()) {
                        command.getObdCommand().run(socket.getInputStream(), socket.getOutputStream());
                    } else {
                        command.setState(CommandState.EXECUTION_ERROR);
                        Log.e("Could not execute command on closed socket.");
                    }
                } else {
                    Log.e("Tried to execute command which did not have NEW state.");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (UnsupportedCommandException u) {
                if (command != null) command.setState(CommandState.NOT_SUPPORTED);
                Log.w("Command not supported. " + u.getMessage());
            } catch (IOException e) {
                command.setState(e.getMessage().contains("Broken pipe") ?
                        CommandState.BROKEN_PIPE : CommandState.EXECUTION_ERROR);
                Log.e("IOException while executing command: " + e.getMessage());
            } catch (Exception e) {
                if (command != null) {
                    command.setState(CommandState.EXECUTION_ERROR);
                }
                Log.e("Command could be not executed. " + e.getMessage());
            }

            if (command != null && mConnectionConsumer != null) {
                mConnectionConsumer.onNextCommand(command);
            }
        }
    }

    public void stopService() {
        mCommandsQueue.clear();
        if (socket != null)
            // close socket
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(e.getMessage());
            }

        // kill service
        stopSelf();
    }

}
