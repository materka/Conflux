package se.materka.conflux.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.transition.Scene
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.fragment_metadata.view.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import se.materka.conflux.R
import se.materka.conflux.databinding.FragmentMetadataBinding
import se.materka.conflux.ui.viewmodel.MetadataViewModel

/**
 * Copyright Mattias Karlsson

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class MetadataFragment : Fragment() {

    private val metadataViewModel: MetadataViewModel? by lazy {
        activity?.getViewModel<MetadataViewModel>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentMetadataBinding>(layoutInflater, R.layout.fragment_metadata, container, false)
        binding.setLifecycleOwner(this)
        binding.viewmodel = metadataViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*view.btn_play.setOnClickListener {
            Toast.makeText(context, "Play Clicked", Toast.LENGTH_SHORT).show()
        }*/
    }

    private fun transition(destination: View) {
        val scene = Scene(view as ViewGroup, destination)
        TransitionManager.go(scene)
    }
}