package se.materka.conflux.module

import android.content.ComponentName
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import se.materka.conflux.RadioService
import se.materka.conflux.RadioSession
import se.materka.conflux.db.AppDatabase
import se.materka.conflux.db.entity.Station
import se.materka.conflux.db.repository.Repository
import se.materka.conflux.db.repository.StationFileRepository
import se.materka.conflux.db.repository.StationRoomRepository
import se.materka.conflux.ui.viewmodel.MainActivityViewModel
import se.materka.conflux.ui.viewmodel.MetadataViewModel

/**
 * Created by Mattias on 1/17/2018.
 */

val BaseModule = module {
    //single { StationFileRepository(androidApplication().applicationContext) as Repository<Station> }
    single { StationRoomRepository(get()) as Repository<Station> }
    single { AppDatabase.instance(get()).stationDao() }
    single { RadioSession(androidApplication().applicationContext, ComponentName(androidApplication().applicationContext, RadioService::class.java)) }
    viewModel { MainActivityViewModel(get(), get()) }
    viewModel { MetadataViewModel(get()) }
}
