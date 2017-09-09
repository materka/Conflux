package se.materka.conflux

import android.app.Application
import com.facebook.stetho.Stetho
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import se.materka.conflux.database.AppDatabase
import se.materka.conflux.database.CreateStation
import timber.log.Timber

class ConfluxApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        deleteDatabase("monkey")

        launch(CommonPool) {
            CreateStation(AppDatabase.instance(this@ConfluxApplication).stationDao()).call()
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}