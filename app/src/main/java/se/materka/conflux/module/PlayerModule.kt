package se.materka.conflux.module

import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.applicationContext
import se.materka.conflux.viewmodel.PlayerViewModel

/**
 * Created by Mattias on 1/17/2018.
 */


val PlayerModule = applicationContext {
    viewModel { PlayerViewModel(get(), get()) }
}