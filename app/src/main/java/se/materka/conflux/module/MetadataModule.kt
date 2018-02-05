package se.materka.conflux.module

import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.applicationContext
import se.materka.conflux.ui.viewmodel.MetadataViewModel

/**
 * Created by Mattias on 1/17/2018.
 */


val MetadataModule = applicationContext {
    viewModel { MetadataViewModel(get(), get()) }
}