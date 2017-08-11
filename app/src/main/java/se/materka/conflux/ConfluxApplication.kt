

package se.materka.conflux

import android.app.Application
import com.facebook.stetho.Stetho
import org.jetbrains.anko.doAsync
import se.materka.conflux.database.AppDatabase
import se.materka.conflux.database.CreateStation

class ConfluxApplication : Application() {
    val radioManager: RadioManager by lazy {
        RadioManager.init(this)
        RadioManager
    }

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        ConfluxApplication.DEBUG = true
        deleteDatabase("monkey")
        val db = AppDatabase.instance(this)
        doAsync {
            CreateStation(db.stationDao()).call()
        }
    }

    companion object {
        var DEBUG = BuildConfig.DEBUG
    }
}