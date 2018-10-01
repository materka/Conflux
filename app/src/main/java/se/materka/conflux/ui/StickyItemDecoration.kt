package se.materka.conflux.ui

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StickyItemDecoration(private val listener: StickyItemListener) : RecyclerView.ItemDecoration() {

    private var selectedItem: View? = null
    private var drawTop = false
    private var drawBottom = false

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val firstItemPosition = (parent.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val lastItemPosition = (parent.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

        // Check if any of the visible items are selected, excluding first and last item
        for (position: Int in firstItemPosition..lastItemPosition) {
            if (listener.isSelected(position) && (position != firstItemPosition && position != lastItemPosition)) {
                drawTop = false
                drawBottom = false
                selectedItem = null
                return
            }
        }

        if (listener.isSelected(firstItemPosition)) {
            selectedItem = getHeaderViewForItem(firstItemPosition, parent).also { view ->
                fixLayoutSize(parent, view)
            }
            drawTop = true
            drawBottom = false
        } else if (listener.isSelected(lastItemPosition)) {
            selectedItem = getHeaderViewForItem(lastItemPosition, parent).also { view ->
                fixLayoutSize(parent, view)
            }
            drawBottom = true
            drawTop = false
        }

        selectedItem?.run {
            if (drawTop) {
                drawView(c, this)
            } else if (drawBottom) {
                drawView(c, this, 0f, parent.bottom - this.height.toFloat())
            }
        }
    }

    private fun getHeaderViewForItem(itemPosition: Int, parent: RecyclerView): View {
        var selectedItemPosition: Int = RecyclerView.NO_POSITION
        return listener.getPositionForSelectedItem(itemPosition).let { position ->
            selectedItemPosition = position
            listener.getSelectedItemLayout(position)
        }.let { layoutId ->
            LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        }.also {
            listener.bindSelectedItemData(it, selectedItemPosition)
        }
    }

    private fun drawView(c: Canvas, view: View, dx: Float = 0f, dy: Float = 0f) {
        view.run {
            c.save()
            c.translate(dx, dy)
            draw(c)
            c.restore()
        }
    }

    /**
     * Properly measures and layouts the selected item.
     * @param parent ViewGroup: RecyclerView in this case.
     */
    private fun fixLayoutSize(parent: ViewGroup, view: View) {

        // Specs for parent (RecyclerView)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        // Specs for selected item
        val childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, parent.paddingLeft + parent.paddingRight, view.layoutParams.width)
        val childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, parent.paddingTop + parent.paddingBottom, view.layoutParams.height)

        view.measure(childWidthSpec, childHeightSpec)

        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    interface StickyItemListener {

        /**
         * This method gets called by [StickyItemDecoration] to fetch the position of the selected item in the adapter
         * that is used for (represents) item at specified position.
         * @param itemPosition int. Adapter's position of the item for which to do the search of the position of the selected item.
         * @return int. Position of the selected item in the adapter.
         */
        fun getPositionForSelectedItem(itemPosition: Int): Int

        /**
         * This method gets called by [StickyItemDecoration] to get layout resource id for the selected item at specified adapter's position.
         * @param itemPosition int. Position of the selected item in the adapter.
         * @return int. Layout resource id.
         */
        fun getSelectedItemLayout(itemPosition: Int): Int

        /**
         * This method gets called by [StickyItemDecoration] to setup the selectedItem View.
         * @param selectedItem View. View to set the data on.
         * @param selectedItemPosition int. Position of the selected item in the adapter.
         */
        fun bindSelectedItemData(selectedItem: View, selectedItemPosition: Int)

        /**
         * This method gets called by [StickyItemDecoration] to verify whether the item is selected.
         * @param itemPosition int.
         * @return true, if item at the specified adapter's position is selected.
         */
        fun isSelected(itemPosition: Int): Boolean
    }
}