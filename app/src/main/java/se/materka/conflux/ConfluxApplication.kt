package se.materka.conflux

import android.app.Application
import com.facebook.stetho.Stetho
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.asReference
import se.materka.conflux.domain.CreateStation
import timber.log.Timber

class ConfluxApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
            deleteDatabase("monkey")

            asReference().let { ref ->
                async(CommonPool) {
                    CreateStation(AppDatabase.instance(ref()).stationDao()).call()
                }
            }

            Timber.plant(Timber.DebugTree())
        }
    }
}