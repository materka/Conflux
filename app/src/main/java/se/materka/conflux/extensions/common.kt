package se.materka.conflux.extensions

/**
 * Created by Mattias on 2017-08-06.
 */

/**
 * Properties
 */


/**
 * Functions
 */

fun <T> nvl(value: T?, default: T): T {
    return value ?: default
}
