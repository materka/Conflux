package se.materka.conflux.module

import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import se.materka.conflux.ui.viewmodel.StationViewModel

/**
 * Created by Mattias on 1/18/2018.
 */

val ListModule = module {
    viewModel { StationViewModel(get(), get()) }
}