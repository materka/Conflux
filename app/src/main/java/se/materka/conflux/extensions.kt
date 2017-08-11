package se.materka.conflux

/**
 * Created by Mattias on 2017-08-06.
 */


inline fun <reified T : Any>T.TAG() = T::class.java.simpleName