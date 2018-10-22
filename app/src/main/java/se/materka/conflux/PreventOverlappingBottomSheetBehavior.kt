package se.materka.conflux

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PreventOverlappingBottomSheetBehavior : CoordinatorLayout.Behavior<View> {
    constructor() : super()
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior is BottomSheetBehavior
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        if (BottomSheetBehavior.from(dependency).state != BottomSheetBehavior.STATE_HIDDEN) {
            child.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                height = dependency.top
            }
        }
        return true
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        super.onDependentViewRemoved(parent, child, dependency)
    }
}