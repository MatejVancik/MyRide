package com.mv2studio.myride.extensions

import java.text.Normalizer

/**
 * Created by matej on 16/11/2016.
 */
fun String.normalizeString(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("[^\\p{ASCII}]", "")
}

fun String.isValidPhoneNumber(): Boolean {
    return length == 13 && matches("\\+[0-9]{12}".toRegex()) ||
            length == 10 && matches("[0-9]{10}".toRegex())
}

fun String.compareAsNormalized(other: String): Int {
    return normalizeString().toLowerCase().compareTo(other.normalizeString())
}