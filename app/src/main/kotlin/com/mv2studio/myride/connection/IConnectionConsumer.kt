package com.mv2studio.myride.connection

/**
 * Created by matej on 15/11/2016.
 */
interface IConnectionConsumer {

    fun onNextCommand(command: Command)

    fun onConnectionStateChanged(state: ConnectionState)

}