package com.mv2studio.myride.connection

/**
 * Created by matej on 15/11/2016.
 */
class State {

    companion object {
        const val NEW = 0L
        const val RUNNING = 1L
        const val FINISHED = 2L
        const val EXECUTION_ERROR = 3L
        const val BROKEN_PIPE = 4L
        const val QUEUE_ERROR = 5L
        const val NOT_SUPPORTED = 6L
    }

}