package se.materka.conflux.ui.player

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_player_compact.*
import se.materka.conflux.MetadataBindable
import se.materka.conflux.databinding.FragmentPlayerCompactBinding
import se.materka.conflux.utils.hideIfEmpty

class PlayerCompactFragment : LifecycleFragment() {

    private val playerViewModel: PlayerViewModel by lazy {
        ViewModelProviders.of(activity).get(PlayerViewModel::class.java)
    }

    private val metadata = MetadataBindable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPlayerCompactBinding.inflate(inflater, container, false)
        playerViewModel.metadata.observe(this, Observer {
            metadata.setArtist(it?.artist)
            metadata.setSong(it?.song)
        })

        playerViewModel.isPlaying.observe(this, Observer {
            if (it == true) {
                btn_toggle_play.apply {
                    showPause()
                    setOnClickListener { MediaControllerCompat.getMediaController(activity).transportControls.stop() }
                }
            } else {
                btn_toggle_play.apply {
                    showPlay()
                    setOnClickListener { MediaControllerCompat.getMediaController(activity).transportControls.play() }
                }
                metadata.clear()
            }
        })

        binding.metadata = metadata
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        text_artist.hideIfEmpty(true)
        text_song.hideIfEmpty(true)
        text_show.hideIfEmpty(true)
        btn_toggle_play.showPlay()
    }
}