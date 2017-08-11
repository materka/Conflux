package se.materka.conflux.ui.station

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_play_station.*
import se.materka.conflux.R
import se.materka.conflux.model.Station
import se.materka.conflux.ui.player.PlayerViewModel

class PlayStationFragment : Fragment() {

    private val playerViewModel: PlayerViewModel by lazy {
        ViewModelProviders.of(activity).get(PlayerViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_play_station, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_play.setOnClickListener {
            val station = Station().apply {
                url = text_url.text.toString()
                name = text_name.text.toString()
            }
            playerViewModel.play(station)
        }
    }
}