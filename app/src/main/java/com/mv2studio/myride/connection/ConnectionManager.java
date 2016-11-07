package com.mv2studio.myride.connection;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.mv2studio.myride.App;
import com.mv2studio.myride.utils.CollectionUtils;
import com.mv2studio.myride.utils.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.ReplaySubject;

/**
 * Link between UI and background ConnectionService
 */
public class ConnectionManager implements IConnectionConsumer {

    private long mConnectionDelay = 4000;
    private static ConnectionManager sInstance;

    private ConnectionService mConnectionService;

    private ReplaySubject<Command> mCommandsObservable;
    private ReplaySubject<ConnectionState> mConnectionStateObservable;

    private HashMap<CommandType, Integer> mRegisteredCommands;
    private Handler mConnectionHandler;
    private boolean mConnectionLoopIsRunning;

    private ConnectionManager() {
        mCommandsObservable = ReplaySubject.create(1);
        mConnectionStateObservable = ReplaySubject.create(1);
        mConnectionHandler = new Handler();
        mRegisteredCommands = new HashMap<>();

        startConnectionService();
    }

    public static ConnectionManager getInstance() {
        if (sInstance == null) {
            throw new RuntimeException("Connection Manager needs to be initiated via init() method before use!");
        }
        return sInstance;
    }

    public static void init() {
        sInstance = new ConnectionManager();
    }

    //region Connection service
    @Override
    public void onNextCommand(Command command) {
        mCommandsObservable.onNext(command);
    }

    @Override
    public void onConnectionStateChanged(ConnectionState state) {
        if (state == null) {
            throw new IllegalArgumentException("New state cannot be null!");
        }

        mConnectionStateObservable.onNext(state);
        Log.d("Connection state updated: " + state.name());
        // start connection loop if it was not running and we are now connected
        if (state == ConnectionState.CONNECTED && !mConnectionLoopIsRunning) {
            mConnectionLoopIsRunning = true;
            mConnectionHandler.post(ConnectionManager.this::runConnectionLoop);
        }
    }
    //endregion

    /**
     * Enqueues required commands to {@link ConnectionService}. Surprisingly, it runs in loop
     * using {@link Handler#postDelayed(Runnable, long)}.
     */
    private void runConnectionLoop() {
        // enqueue all commands required right now
        CollectionUtils.iterateMap(mRegisteredCommands, (key, value) ->
                mConnectionService.enqueueCommand(key));

        mConnectionHandler.postDelayed(this::runConnectionLoop, mConnectionDelay);
    }

    private void startConnectionService() {
        // start connection service if not running
        Intent intent = new Intent(App.getAppContext(), ConnectionService.class);

        App.getAppContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mConnectionService = ((ConnectionService.Binder) iBinder).getBoundService();

            // bind this connection manager to service to recive updates.
            mConnectionService.bindConnectionConsumer(ConnectionManager.this);

            // Update connection state at this point to start communication loop if necessary.
            onConnectionStateChanged(mConnectionService.getCurrentConnectionState());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // stop update loop
            mConnectionHandler.removeCallbacksAndMessages(null);
            mConnectionLoopIsRunning = false;

            // this service is useless now.
            mConnectionService = null;
        }
    };

    public void reconnect() {
        mConnectionService.reconnect();
    }

    //region updates observation
    public Disposable registerForStateUpdates(Consumer<ConnectionState> onNext) {
        return mConnectionStateObservable.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext);
    }

    public void registerForCommandUpdates(Observer<Command> observer, CommandType... obdCommandsFilters) {
        registerForCommandUpdates(observer, Arrays.asList(obdCommandsFilters));
    }

    public void registerForCommandUpdates(Observer<Command> observer, List<CommandType> obdCommandsFilters) {
        addCommandType(obdCommandsFilters);
        mCommandsObservable.observeOn(AndroidSchedulers.mainThread())
                .filter(command -> obdCommandsFilters.contains(command.getCommandType()))
                .doOnDispose(() -> removeCommandType(obdCommandsFilters))
                .subscribe(observer);
    }

    public Disposable registerForCommandUpdates(Consumer<Command> onNext, CommandType... obdCommandsFilters) {
        return registerForCommandUpdates(onNext, Arrays.asList(obdCommandsFilters));
    }

    public Disposable registerForCommandUpdates(Consumer<Command> onNext, List<CommandType> obdCommandsFilters) {
        addCommandType(obdCommandsFilters);
        return mCommandsObservable.observeOn(AndroidSchedulers.mainThread())
                .filter(command -> obdCommandsFilters.contains(command.getCommandType()))
                .doOnDispose(() -> removeCommandType(obdCommandsFilters))
                .subscribe(onNext);
    }

    private void addCommandType(List<CommandType> commands) {
        CollectionUtils.forEachOptimized(commands, command -> {
            Integer count = mRegisteredCommands.get(command);
            count = count == null ? 1 : count++;
            mRegisteredCommands.put(command, count);
        });
    }

    private void removeCommandType(List<CommandType> commands) {
        CollectionUtils.forEachOptimized(commands, command -> {
            Integer count = mRegisteredCommands.get(command);
            if (count != null) {
                count -= 1;
                if (count <= 0) {
                    mRegisteredCommands.remove(command);
                } else {
                    mRegisteredCommands.put(command, count);
                }
            }
        });
    }
    //endregion
}
