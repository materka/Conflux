package se.materka.conflux.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DividerItemDecoration(context: Context,
                            private val showFirstDivider: Boolean = false,
                            private val showLastDivider: Boolean = false,
                            divider: Drawable? = null,
                            private val sectionCallback: SectionCallback,
                            private val sticky: Boolean = true) : RecyclerView.ItemDecoration() {

    interface SectionCallback {

        fun isSection(position: Int): Boolean
    }

    private val divider: Drawable

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
                                state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val pos = parent.getChildAdapterPosition(view)

        when {
            pos < 1 -> return
            sectionCallback.isSection(pos) -> outRect.top = 30
            getOrientation(parent) == LinearLayoutManager.VERTICAL -> outRect.top = divider.intrinsicHeight
            else -> outRect.left = divider.intrinsicWidth
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        when (getOrientation(parent)) {
            LinearLayoutManager.VERTICAL -> drawVertical(canvas, parent)
            LinearLayoutManager.HORIZONTAL -> drawHorizontal(canvas, parent)
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)

            if (sectionCallback.isSection(position)) {
                drawHeader(c, child, child)

            }
        }
    }

    private fun drawHeader(c: Canvas, child: View, headerView: View) {
        c.save()
        val dy = if (sticky) {
            Math.max(0, (child.top - headerView.height))
        } else {
            child.top - headerView.height
        }
        c.translate(0F, dy.toFloat())
        headerView.draw(c)
        c.restore()
    }

    private fun fixLayoutSize(view: View, parent: ViewGroup) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width,
                View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height,
                View.MeasureSpec.UNSPECIFIED)

        val childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.paddingLeft + parent.paddingRight,
                view.layoutParams.width)
        val childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.paddingTop + parent.paddingBottom,
                view.layoutParams.height)

        view.measure(childWidth, childHeight)

        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        val size = divider.intrinsicHeight
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount

        var bottom: Int
        var top: Int

        for (i in (if (showFirstDivider) 0 else 1) until childCount) {
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
        for (i in (if (showFirstDivider) 0 else 1) until childCount) {
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
