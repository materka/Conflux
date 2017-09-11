package se.materka.conflux.ui.main

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.mikepenz.community_material_typeface_library.CommunityMaterial.Icon
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.context.IconicsContextWrapper
import com.mikepenz.iconics.typeface.IIcon
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.itemsSequence
import se.materka.conflux.PlayService
import se.materka.conflux.R
import se.materka.conflux.ui.browse.BrowseFragment
import se.materka.conflux.ui.browse.BrowseViewModel
import se.materka.conflux.ui.player.PlayerViewModel
import se.materka.conflux.ui.station.PlayStationFragment
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata
import timber.log.Timber

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

class MainActivity : AppCompatActivity(), LifecycleRegistryOwner {
    private lateinit var mediaBrowser: MediaBrowserCompat

    private val playerViewModel: PlayerViewModel by lazy {
        ViewModelProviders.of(this).get(PlayerViewModel::class.java)
    }

    private val browseViewModel: BrowseViewModel by lazy {
        ViewModelProviders.of(this).get(BrowseViewModel::class.java)
    }

    private val lifecycleRegistry by lazy {
        LifecycleRegistry(this)
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Create a MediaControllerCompat
            MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken).let { controller ->
                MediaControllerCompat.setMediaController(this@MainActivity, controller)
                controller.registerCallback(controllerCallback)
            }
        }

        override fun onConnectionSuspended() {
            MediaControllerCompat.getMediaController(this@MainActivity)?.let { controller ->
                controller.unregisterCallback(controllerCallback)
                MediaControllerCompat.setMediaController(this@MainActivity, null)
            }
        }

        override fun onConnectionFailed() {
            Timber.e("connection failed")
        }
    }

    private val controllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(data: MediaMetadataCompat?) {
            Timber.i("New metadata")
            val metadata = ShoutcastMetadata(
                    artist = data?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST),
                    song = data?.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                    show = null,
                    channels = null,
                    station = null,
                    bitrate = null,
                    genre = null,
                    url = null)
            playerViewModel.setMetadata(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            MediaControllerCompat.getMediaController(this@MainActivity)?.let { controller ->
                playerViewModel.isPlaying(controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING)
            }
        }
    }


    private val drawerToggle by lazy {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        ActionBarDrawerToggle(this, drawer, toolbar as Toolbar, R.string.drawer_open, R.string.drawer_close)
    }

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }

    private fun setupDrawerContent() {
        navigation_view.menu.itemsSequence().forEach { menuItem ->
            var icon: IIcon? = null
            when (menuItem.itemId) {
                R.id.nav_play_station -> icon = Icon.cmd_play
                R.id.nav_favorites -> icon = Icon.cmd_heart
                R.id.nav_settings -> icon = Icon.cmd_settings
            }
            menuItem.icon = IconicsDrawable(this@MainActivity, icon)
                    .actionBar()
                    .paddingDp(2)
        }
        navigation_view.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    fun selectDrawerItem(menuItem: MenuItem) {
        var fragment: Fragment? = null
        val fragmentClass: Class<*> = when (menuItem.itemId) {
            R.id.nav_play_station -> PlayStationFragment::class.java
            else -> BrowseFragment::class.java
        }

        try {
            fragment = fragmentClass.newInstance() as Fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Insert the fragment by replacing any existing fragment
        supportFragmentManager.beginTransaction().replace(R.id.content, fragment).commit()

        // Highlight the selected item has been done by NavigationView
        menuItem.isChecked = true
        // Set action bar title
        title = menuItem.title
        // Close the navigation drawer
        drawer.closeDrawers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar as Toolbar)
        setupDrawerContent()
        playerViewModel.isPlaying.observe(this, Observer<Boolean> { playing ->
            BottomSheetBehavior.from(player.view).state = if (playing != false)
                BottomSheetBehavior.STATE_EXPANDED
            else
                BottomSheetBehavior.STATE_COLLAPSED
        })
        browseViewModel.selected.observe(this, Observer {
            val uri: Uri = Uri.parse(it?.url)
            MediaControllerCompat.getMediaController(this).transportControls?.playFromUri(uri, null)
        })
        supportFragmentManager.beginTransaction().replace(R.id.content, BrowseFragment()).commit()
        mediaBrowser = MediaBrowserCompat(this,
                ComponentName(this, PlayService::class.java),
                connectionCallback, null)
        mediaBrowser.connect()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE 1: Make sure to override the method with only a single `Bundle` argument
    // Note 2: Make sure you implement the correct `onPostCreate(Bundle savedInstanceState)` method.
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }
}

