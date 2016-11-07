package com.mv2studio.myride.connection;

/**
 * Created by matej on 30.10.16.
 */

public enum CommandType {

    RPM,
    THROTTLE_POSITION,
    CONSUMPTION_RATE,
    TEMPERATURE_OIL,
    // ELM does not provide ODO. With little workaround, we can keep track of odo if user occasionally provides it manually.
    // When this happens, keep track of DISTANCE_SINCE_CC and once this is resets, let user note that ODO in the app may not be correct.
    DISTANCE_SINCE_CC,
    TEMPERATURE_AMBIENT_AIR,
    TEMPERATURE_INTAKE,
    SPEED,
    FUEL_TRIM,
    FUEL_LEVEL,

    // These are used for initiating connection. Not aware of any other use yet.
    ODB_RESET,
    ECHO_OFF,
    LINE_FEED_OFF,
    TIMEOUT,
    SELECT_PROTOCOL;

}
