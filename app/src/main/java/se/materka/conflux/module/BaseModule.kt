package se.materka.conflux.module

import org.koin.dsl.module.applicationContext
import se.materka.conflux.AppDatabase
import se.materka.conflux.service.repository.StationRepository
import se.materka.conflux.service.repository.StationRepositoryImpl

/**
 * Created by Mattias on 1/17/2018.
 */

val BaseModule = applicationContext {
    provide { StationRepositoryImpl(get()) as StationRepository }
    provide { AppDatabase.Companion.instance(get()).stationDataSource() }
}
