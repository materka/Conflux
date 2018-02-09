package se.materka.conflux.module

import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.applicationContext
import se.materka.conflux.ui.viewmodel.StationViewModel

/**
 * Created by Mattias on 1/18/2018.
 */

val ListModule = applicationContext {
    viewModel { StationViewModel(get(), get()) }
}