package se.materka.conflux.module

import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.applicationContext
import se.materka.conflux.AppDatabase
import se.materka.conflux.viewmodel.ListViewModel

/**
 * Created by Mattias on 1/17/2018.
 */

val BaseModule = applicationContext {
    viewModel { ListViewModel(get(), get()) }
    provide { AppDatabase.Companion.instance(get()).stationRepository() }
}
