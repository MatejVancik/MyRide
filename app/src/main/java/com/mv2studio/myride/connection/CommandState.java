package com.mv2studio.myride.connection;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by matej on 30.10.16.
 */

@Retention(RetentionPolicy.SOURCE)
@IntDef({CommandState.NEW, CommandState.RUNNING, CommandState.FINISHED,
        CommandState.EXECUTION_ERROR, CommandState.BROKEN_PIPE,
        CommandState.QUEUE_ERROR, CommandState.NOT_SUPPORTED})
public @interface CommandState {

    int NEW = 0;
    int RUNNING = 1;
    int FINISHED = 2;
    int EXECUTION_ERROR = 3;
    int BROKEN_PIPE = 4;
    int QUEUE_ERROR = 5;
    int NOT_SUPPORTED = 6;

}
