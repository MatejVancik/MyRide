package com.mv2studio.myride.extensions

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created by matej on 16/11/2016.
 */

fun InputStream.streamToString(): String {
    val r = BufferedReader(InputStreamReader(this))
    val total = StringBuilder()
    var line = r.readLine()
    while (line != null) {
        total.append(line)
        line = r.readLine()
    }
    return total.toString()
}

fun Int.colorAsHex(): String {
    return String.format("#%06X", (0xFFFFFF and this))
}


