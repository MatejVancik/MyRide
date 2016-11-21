package com.mv2studio.myride.connection

import android.support.annotation.IntDef
import kotlin.annotation.Retention

/**
 * Created by matej on 15/11/2016.
 */
@Retention(AnnotationRetention.RUNTIME)
@IntDef(State.NEW, State.RUNNING, State.FINISHED,
    State.EXECUTION_ERROR, State.BROKEN_PIPE,
    State.QUEUE_ERROR, State.NOT_SUPPORTED)
annotation class CommandState