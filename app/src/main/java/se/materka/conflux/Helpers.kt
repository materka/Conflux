package se.materka.conflux

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.io.BufferedWriter

fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

inline val <reified T: Any> T.TAG: String
    get() = T::class.java.name

fun BufferedWriter.writeLn(line: String) {
    this.write(line)
    this.newLine()
}