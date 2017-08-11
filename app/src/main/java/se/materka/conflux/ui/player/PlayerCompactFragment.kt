package se.materka.conflux.ui.player

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_player_compact.*
import se.materka.conflux.MetadataBindable
import se.materka.conflux.databinding.FragmentPlayerCompactBinding
import se.materka.conflux.utils.hideIfEmpty

/**
 * Created by Privat on 6/8/2017.
 */

class PlayerCompactFragment : LifecycleFragment() {
    val playerViewModel: PlayerViewModel by lazy {
        ViewModelProviders.of(this).get(PlayerViewModel::class.java)
    }

    val metadata = MetadataBindable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPlayerCompactBinding.inflate(inflater, container, false)
        playerViewModel.bindMetadata(metadata)
        binding.metadata = metadata
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        playerViewModel.isPlaying.observe(this, Observer<Boolean> { playing ->
            btn_toggle_play.toggle()

            /*if (playing == true) {
                btn_toggle_play.setImageDrawable(icon(context, CommunityMaterial.Icon.cmd_pause))
            } else {
                btn_toggle_play.setImageDrawable(icon(context, CommunityMaterial.Icon.cmd_play))
            }*/
        })

        btn_toggle_play.setOnClickListener {
            playerViewModel.togglePlay()
        }

        text_artist.hideIfEmpty(true)
        text_song.hideIfEmpty(true)
        text_show.hideIfEmpty(true)
    }
}