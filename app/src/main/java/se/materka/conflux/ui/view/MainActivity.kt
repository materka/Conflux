package se.materka.conflux.ui.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mikepenz.iconics.context.IconicsContextWrapper
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import se.materka.conflux.R
import se.materka.conflux.ui.DividerItemDecoration
import se.materka.conflux.ui.adapter.ListAdapter
import se.materka.conflux.ui.viewmodel.MainActivityViewModel
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

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener, PlayUrlFragment.PlayUrlDialogListener {

    private lateinit var listAdapter: ListAdapter

    private val metadataViewModel: MetadataViewModel by lazy {
        getViewModel<MetadataViewModel>()
    }

    private val mainActivityViewModel: MainActivityViewModel by lazy {
        getViewModel<MainActivityViewModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar as Toolbar)

        listAdapter = ListAdapter({ item -> itemClicked(item) },
                { item -> itemLongClicked(item) })

        list.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = listAdapter
            addItemDecoration(DividerItemDecoration(context, false, false, null, getSectionCallback()))
        }

        metadataViewModel.isPlaying.observe(this, Observer { playing ->
            if (playing == true) {
                setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
            } else {
                setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
            }
        })

        mainActivityViewModel.items.observe(this, Observer { items ->
            if (items != null && !items.isEmpty()) {
                listAdapter.updateDataSet(items)
            }
        })

        setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)

        newStation.setOnClickListener {
            showPlayDialog()
        }
    }

    public override fun onResume() {
        super.onResume()
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.INTERNET)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.INTERNET),
                        1)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private fun getSectionCallback(): DividerItemDecoration.SectionCallback {
        return object : DividerItemDecoration.SectionCallback {
            override fun isSection(position: Int): Boolean {
                return position == 0 || listAdapter.items[position] != listAdapter.items[position - 1]
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).also { manager ->
                    manager.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }

        return true
    }

    override fun onQueryTextChange(query: String): Boolean {
        listAdapter.filter.filter(query)
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        listAdapter.filter.filter(query)
        return false
    }

    override fun onBackPressed() {
        if (BottomSheetBehavior.from(metadata.view).state == BottomSheetBehavior.STATE_EXPANDED) {
            setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
        } else {
            finish()
        }
    }

    override fun onDialogFinished(resultCode: Int?, result: Bundle) {
        when(resultCode) {
            PLAY_URL_RESULT_CODE -> {
                result.getString(PlayUrlFragment.RESULT_URI)?.let { uri ->
                    mainActivityViewModel.select(uri.toUri())
                    if (result.getBoolean(PlayUrlFragment.RESULT_SAVE)) {
                        mainActivityViewModel.saveUri(uri.toUri(), result.getString(PlayUrlFragment.RESULT_NAME, uri))
                    }
                }
            }
        }
    }

    private fun showPlayDialog() {
        PlayUrlFragment
                .newInstance(PLAY_URL_RESULT_CODE)
                .show(supportFragmentManager, "PlayUrlFragment")
    }

    private fun setBottomSheetState(state: Int) {
        if (metadata != null) {
            BottomSheetBehavior.from(metadata.view).let {
                if (it.state != state) {
                    it.state = state
                    //(toolbar.parent as AppBarLayout).setExpanded(state != BottomSheetBehavior.STATE_EXPANDED, true)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_search) {
            true
        } else super.onOptionsItemSelected(item)

    }

    private fun itemClicked(item: MediaBrowserCompat.MediaItem) {
        mainActivityViewModel.select(item)
    }

    private fun itemLongClicked(item: MediaBrowserCompat.MediaItem) {
        /*val actionView = LayoutInflater.from(this).inflate(R.layout.menu_action, null, false)
        val actionDialog = BottomSheetDialog(this)

        actionView.info.setOnClickListener {
            InfoFragment.newInstance(station).show(supportFragmentManager, "InfoFragment")
            actionDialog.dismiss()
        }

        actionView.edit.setOnClickListener {
            EditFragment.newInstance(station).show(supportFragmentManager, "EditFragment")
            actionDialog.dismiss()
        }

        actionView.delete.setOnClickListener {
            AlertDialog.Builder(this, R.style.AppTheme_WarningDialog)
                    .setTitle("Remove")
                    .setMessage("Are you sure you want to remove this station?")
                    .setPositiveButton("REMOVE") { _, _ ->
                        stationViewModel?.delete(station)
                    }
                    .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
                    .show()
            actionDialog.dismiss()
        }

        actionDialog.apply {
            setContentView(actionView)
            show()
        }*/
    }

    companion object {
        private const val PLAY_URL_RESULT_CODE = 1
    }
}