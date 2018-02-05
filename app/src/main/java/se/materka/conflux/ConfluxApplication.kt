package se.materka.conflux

import android.app.Application
import com.facebook.stetho.Stetho
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin
import se.materka.conflux.db.repository.StationRepository
import se.materka.conflux.module.BaseModule
import se.materka.conflux.module.ListModule
import se.materka.conflux.module.MetadataModule
import timber.log.Timber

class ConfluxApplication : Application() {

    private val stationRepository: StationRepository by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(BaseModule, ListModule, MetadataModule))

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
            deleteDatabase("conflux")
            Timber.plant(Timber.DebugTree())
        }
    }
}