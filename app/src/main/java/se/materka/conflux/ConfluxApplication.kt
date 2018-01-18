package se.materka.conflux

import android.app.Application
import com.facebook.stetho.Stetho
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.asReference
import org.koin.android.ext.android.startKoin
import se.materka.conflux.module.BaseModule
import se.materka.conflux.module.ListModule
import se.materka.conflux.module.PlayerModule
import se.materka.conflux.service.CreateStation
import timber.log.Timber

class ConfluxApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(BaseModule, ListModule, PlayerModule))

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
            deleteDatabase("conflux")

            asReference().let { ref ->
                async(CommonPool) {
                    CreateStation(AppDatabase.instance(ref()).stationDataSource()).call()
                }
            }

            Timber.plant(Timber.DebugTree())
        }
    }
}