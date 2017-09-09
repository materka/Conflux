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

/**
 * Copyright 2017 Mattias Karlsson

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
