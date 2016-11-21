package com.mv2studio.myride.connection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import com.mv2studio.myride.App
import com.mv2studio.myride.extensions.d
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.ReplaySubject
import java.util.*

/**
 * Created by matej on 14/11/2016.
 */
object ConnectionManager : IConnectionConsumer {

    private val mConnectionDelay = 4000L

    private var mConnectionService: ConnectionService? = null

    private val mCommandsObservable: ReplaySubject<Command> = ReplaySubject.create(1)
    private val mConnectionStateObservable: ReplaySubject<ConnectionState> = ReplaySubject.create(1)

    private val mRegisteredCommands: HashMap<CommandType, Int> = HashMap()
    private val mConnectionHandler: Handler = Handler()
    private var mConnectionLoopIsRunning = false

    //region Connection service
    override fun onNextCommand(command: Command) {
        mCommandsObservable.onNext(command)
    }

    override fun onConnectionStateChanged(state: ConnectionState) {
        mConnectionStateObservable.onNext(state)
        d("Connection state updated: " + state.name)
        // start connection loop if it was not running and we are now connected
        if (state == ConnectionState.CONNECTED && !mConnectionLoopIsRunning) {
            mConnectionLoopIsRunning = true
            mConnectionHandler.post { runConnectionLoop() }
        }

    }
    //endregion

    /**
     * Enqueues required commands to {@link ConnectionService}. Surprisingly, it runs in loop
     * using {@link Handler#postDelayed(Runnable, long)}.
     */
    fun runConnectionLoop() {
        mRegisteredCommands.forEach { mConnectionService?.enqueueCommand(it.key) }

        mConnectionHandler.postDelayed({ runConnectionLoop() }, mConnectionDelay)
    }

    fun startConnectionService() {
        // start connection service if not running
        val intent = Intent(App.appContext, ConnectionService::class.java)
        App.appContext!!.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {
            // stop update loop
            mConnectionHandler.removeCallbacksAndMessages(null)
            mConnectionLoopIsRunning = false

            // this service is useless now.
            mConnectionService = null
        }

        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            mConnectionService = (iBinder as ConnectionService.Binder).boundService

            // bind this connection manager to service to recive updates.
            mConnectionService!!.bindConnectionConsumer(this@ConnectionManager)

            // Update connection state at this point to start communication loop if necessary.
            onConnectionStateChanged(mConnectionService!!.currentConnectionState)
        }
    }

    fun reconnect() {
        mConnectionService?.reconnect()
    }

    //region updates observation
    fun registerForStateUpdates(onNext: Consumer<ConnectionState>): Disposable {
        return mConnectionStateObservable.observeOn(AndroidSchedulers.mainThread()).subscribe(onNext)
    }

    fun registerForCommandUpdates(observer: Observer<Command>, obdCommandsFilters: List<CommandType>) {
        addCommandType(obdCommandsFilters)
        return mCommandsObservable.observeOn(AndroidSchedulers.mainThread())
                .filter { obdCommandsFilters.contains(it.commandType) }
                .doOnDispose { removeCommandType(obdCommandsFilters) }
                .subscribe(observer)
    }

    fun registerForCommandUpdates(onNext: Consumer<Command>, obdCommandsFilters: List<CommandType>): Disposable {
        addCommandType(obdCommandsFilters)
        return mCommandsObservable.observeOn(AndroidSchedulers.mainThread())
                .filter { obdCommandsFilters.contains(it.commandType) }
                .doOnDispose { removeCommandType(obdCommandsFilters) }
                .subscribe(onNext)
    }

    fun addCommandType(commands: List<CommandType>) {
        commands.forEach {
            var count = mRegisteredCommands.get(it) ?: 0
            count++
            mRegisteredCommands.put(it, count)
        }
    }

    fun removeCommandType(commands: List<CommandType>) {
        commands.forEach {
            var count = mRegisteredCommands.get(it) ?: return@forEach
            count--
            if (count <= 0) {
                mRegisteredCommands.remove(it)
            } else {
                mRegisteredCommands.put(it, count)
            }
        }
    }
    //endregion

}