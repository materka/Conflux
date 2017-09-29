package se.materka.conflux.ui.browse

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_browse.*
import se.materka.conflux.R
import se.materka.conflux.domain.Station
import se.materka.conflux.ui.player.PlayerViewModel
import se.materka.conflux.ui.station.ActionFragment

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

class BrowseFragment : Fragment() {

    private lateinit var stationAdapter: BrowseAdapter

    private val browseViewModel: BrowseViewModel by lazy {
        ViewModelProviders.of(activity).get(BrowseViewModel::class.java)
    }

    private val playerViewModel: PlayerViewModel by lazy {
        ViewModelProviders.of(activity).get(PlayerViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_browse, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stationAdapter = BrowseAdapter({ station -> itemClicked(station) },
                { station -> itemLongClicked(station) })

        list.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = stationAdapter
            addItemDecoration(DividerItemDecoration(context, false, false))
        }

        browseViewModel.getStations()?.observe(this, Observer<List<Station>> { stations ->
            if (stations != null) {
                stationAdapter.stations = stations
            }
        })
    }

    fun itemClicked(station: Station) {
        browseViewModel.select(station)
        playerViewModel.play(station)
    }

    fun itemLongClicked(station: Station) {
        browseViewModel.select(station)
        ActionFragment().show(activity.supportFragmentManager, "ActionFragment")
    }

    inner private class DividerItemDecoration(context: Context, val showFirstDivider: Boolean = false,
                                              val showLastDivider: Boolean = false, divider: Drawable? = null) : RecyclerView.ItemDecoration() {
        val divider: Drawable

        init {
            if (divider == null) {
                val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
                this.divider = a.getDrawable(0)
                a.recycle()
            } else {
                this.divider = divider
            }
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                    state: RecyclerView.State?) {
            super.getItemOffsets(outRect, view, parent, state)
            if (parent.getChildAdapterPosition(view) < 1) {
                return
            }

            if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
                outRect.top = divider.intrinsicHeight
            } else {
                outRect.left = divider.intrinsicWidth
            }
        }

        override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
            when (getOrientation(parent)) {
                LinearLayoutManager.VERTICAL -> drawVertical(canvas, parent)
                LinearLayoutManager.HORIZONTAL -> drawHorizontal(canvas, parent)
            }
        }

        private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
            val size = divider.intrinsicHeight
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight
            val childCount = parent.childCount

            var bottom: Int
            var top: Int

            for (i in (if (showFirstDivider) 0 else 1)..childCount - 1) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                if (isReverseLayout(parent)) {
                    bottom = child.bottom - params.bottomMargin
                    top = bottom - size
                } else {
                    top = child.top - params.topMargin
                    bottom = top + size
                }

                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }

            if (showLastDivider && childCount > 0) {
                val child = parent.getChildAt(childCount - 1)
                val params = child.layoutParams as RecyclerView.LayoutParams
                top = child.bottom + params.bottomMargin
                bottom = top + size
                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }

        }

        private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
            val size = divider.intrinsicWidth
            val top = parent.paddingTop
            val bottom = parent.height - parent.paddingBottom
            val childCount = parent.childCount

            var left: Int
            var right: Int
            for (i in (if (showFirstDivider) 0 else 1)..childCount - 1) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams

                if (isReverseLayout(parent)) {
                    right = child.right - params.rightMargin
                    left = right - size
                } else {
                    left = child.left - params.leftMargin
                    right = left + size
                }

                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }

            // show last divider
            if (showLastDivider && childCount > 0) {
                val child = parent.getChildAt(childCount - 1)
                val params = child.layoutParams as RecyclerView.LayoutParams
                left = child.right + params.rightMargin
                right = left + size
                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }
        }

        private fun getOrientation(parent: RecyclerView): Int {
            if (parent.layoutManager is LinearLayoutManager) {
                val layoutManager = parent.layoutManager as LinearLayoutManager
                return layoutManager.orientation
            } else {
                throw IllegalStateException("DividerItemDecoration can only be used with a LinearLayoutManager.")
            }
        }

        private fun isReverseLayout(parent: RecyclerView): Boolean {
            if (parent.layoutManager is LinearLayoutManager) {
                val layoutManager = parent.layoutManager as LinearLayoutManager
                return layoutManager.reverseLayout
            } else {
                throw IllegalStateException("DividerItemDecoration can only be used with a LinearLayoutManager.")
            }
        }
    }
}