package se.materka.conflux.ui.player

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_player_detail.*
import se.materka.conflux.R

class PlayerDetailActivity : AppCompatActivity(), LifecycleRegistryOwner {
    private val lifecycleRegistry by lazy {
        LifecycleRegistry(this)
    }

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_detail)

        toolbar.let { t ->
            t.navigationIcon = IconicsDrawable(this@PlayerDetailActivity, CommunityMaterial.Icon.cmd_arrow_left)
                    .color(ContextCompat.getColor(this@PlayerDetailActivity, R.color.icons))
                    .actionBar()
                    .paddingDp(4)
            t.setNavigationOnClickListener { onBackPressed() }
        }
    }
}
