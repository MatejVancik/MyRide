package com.mv2studio.myride.connection;

/**
 * Created by matej on 1.11.16.
 */

interface IConnectionConsumer {

    void onNextCommand(Command command);

    void onConnectionStateChanged(ConnectionState state);

}
