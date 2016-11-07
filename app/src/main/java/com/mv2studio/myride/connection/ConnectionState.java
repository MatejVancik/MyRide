package com.mv2studio.myride.connection;

/**
 * Created by matej on 1.11.16.
 */

public enum ConnectionState {

    UNKNOWN,
    BT_OFF,
    BT_TURNING_OFF,
    BT_ON,
    BT_TURNING_ON,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    DISCONNECTED,
    NO_DEVICE_SELECTED,
    CANNOT_CONNECT

}
