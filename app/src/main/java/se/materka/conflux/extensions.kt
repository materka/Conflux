package se.materka.conflux

/**
 * Created by Mattias on 2017-08-06.
 */


inline val <reified T : Any>T.TAG
    get() = T::class.java.simpleName