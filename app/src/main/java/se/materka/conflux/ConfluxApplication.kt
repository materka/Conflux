package se.materka.conflux

import android.app.Application
import com.facebook.stetho.Stetho
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin
import se.materka.conflux.db.entity.Station
import se.materka.conflux.db.repository.Repository
import se.materka.conflux.module.BaseModule

class ConfluxApplication : Application() {

    private val stationRepository: Repository<Station> by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(BaseModule))

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
            deleteDatabase("conflux")

            val content = assets.open("stations.json").reader().readText()
            val stations: List<Station> = Gson().fromJson(content)
            stations.forEach { stationRepository.save(it) }
        }
        stationRepository.get()
    }


}