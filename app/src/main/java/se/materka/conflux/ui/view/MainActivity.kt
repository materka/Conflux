package se.materka.conflux.ui.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_action.view.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import se.materka.conflux.R
import se.materka.conflux.ui.StickyItemDecoration
import se.materka.conflux.ui.adapter.ListAdapter
import se.materka.conflux.ui.viewmodel.MainActivityViewModel


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

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener,
        PlayUrlFragment.PlayUrlDialogListener, ListAdapter.ListAdapterListener, MetadataFragment.MetadataFragmentListener {

    private lateinit var listAdapter: ListAdapter

    private val mainActivityViewModel: MainActivityViewModel by lazy {
        getViewModel<MainActivityViewModel>()
    }

    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar as Toolbar)

        listAdapter = ListAdapter(this)

        rvList.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = listAdapter
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(this.context, RecyclerView.VERTICAL))
            addItemDecoration(StickyItemDecoration(listAdapter))
        }
        mainActivityViewModel.items.observe(this, Observer { items ->
            if (items != null && !items.isEmpty()) {
                listAdapter.updateDataSet(items)
            }
        })
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        (menu.findItem(R.id.action_search)?.actionView as SearchView).apply {
            setOnQueryTextListener(this@MainActivity)
            setOnQueryTextFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).also { manager ->
                        manager.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
            }
        }

        menu.findItem(R.id.action_add)?.apply {
            setOnMenuItemClickListener {
                showPlayDialog()
                true
            }
        }

        menu.findItem(R.id.action_save)?.apply {
            setOnMenuItemClickListener {
                Toast.makeText(this@MainActivity, "Saving station", Toast.LENGTH_SHORT).show()
                true
            }
        }

        this.menu = menu

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

    override fun onDialogFinished(resultCode: Int?, result: Bundle) {
        when (resultCode) {
            PLAY_URL_RESULT_CODE -> {
                result.getString(PlayUrlFragment.RESULT_URI)?.let { uri ->
                    mainActivityViewModel.select(uri.toUri())
                    if (result.getBoolean(PlayUrlFragment.RESULT_SAVE)) {
                        mainActivityViewModel.saveUrl(uri.toUri(), result.getString(PlayUrlFragment.RESULT_NAME, uri))
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_search || id == R.id.action_add) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onItemClicked(item: MediaBrowserCompat.MediaItem) {
        mainActivityViewModel.select(item)
    }

    override fun onItemLongClicked(item: MediaBrowserCompat.MediaItem) {
        val actionView = LayoutInflater.from(this).inflate(R.layout.menu_action, null, false)
        val actionDialog = BottomSheetDialog(this)

        actionView.info.setOnClickListener {
            //InfoFragment.newInstance(station).show(supportFragmentManager, "InfoFragment")
            actionDialog.dismiss()
        }

        actionView.edit.setOnClickListener {
            //EditFragment.newInstance(station).show(supportFragmentManager, "EditFragment")
            actionDialog.dismiss()
        }

        actionView.delete.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle("Remove")
                    .setMessage("Are you sure you want to remove this station?")
                    .setPositiveButton("REMOVE") { _, _ ->
                        //stationViewModel?.delete(station)
                    }
                    .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
                    .show()
            actionDialog.dismiss()
        }

        actionDialog.apply {
            setContentView(actionView)
            show()
        }
    }

    override fun onPlayButtonClicked() {
        mainActivityViewModel.togglePlayback()
    }

    companion object {
        private const val PLAY_URL_RESULT_CODE = 1
    }
}