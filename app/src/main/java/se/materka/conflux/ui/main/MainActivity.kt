package se.materka.conflux.ui.main

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
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
import se.materka.conflux.R
import se.materka.conflux.ui.player.PlayerViewModel
import se.materka.conflux.ui.station.PlayStationFragment
import se.materka.conflux.ui.station.StationListFragment
import se.materka.conflux.ui.station.StationViewModel


class MainActivity : AppCompatActivity(), LifecycleRegistryOwner {

    private val lifecycleRegistry by lazy {
        LifecycleRegistry(this)
    }

    private val stationViewModel: StationViewModel by lazy {
        ViewModelProviders.of(this).get(StationViewModel::class.java)
    }

    private val playerViewModel: PlayerViewModel by lazy {
        ViewModelProviders.of(this).get(PlayerViewModel::class.java)
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
        // Create a new fragment and specify the fragment to show based on nav item clicked
        var fragment: Fragment? = null
        val fragmentClass: Class<*>
        when (menuItem.itemId) {
            R.id.nav_play_station -> fragmentClass = PlayStationFragment::class.java
            else -> fragmentClass = StationListFragment::class.java
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
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar as Toolbar)
        setupDrawerContent()
        playerViewModel.isPlaying.observe(this, Observer<Boolean> { playing ->
            BottomSheetBehavior.from(player.view).state = if (playing ?: true)
                BottomSheetBehavior.STATE_EXPANDED
            else
                BottomSheetBehavior.STATE_COLLAPSED
        })
        supportFragmentManager.beginTransaction().replace(R.id.content, StationListFragment()).commit()
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

