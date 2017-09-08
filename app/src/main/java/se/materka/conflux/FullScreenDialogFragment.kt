package se.materka.conflux

import android.app.Dialog
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable

open class FullScreenDialogFragment : DialogFragment(), LifecycleRegistryOwner {

    internal val lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window.attributes.windowAnimations = R.style.AppTheme_WindowSlideAnimation
        return dialog
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view?.findViewById<Toolbar>(R.id.toolbar)?.let { t ->
            t.navigationIcon = IconicsDrawable(context, CommunityMaterial.Icon.cmd_arrow_left)
                    .color(ContextCompat.getColor(context, R.color.icons))
                    .actionBar()
                    .paddingDp(4)
            t.setNavigationOnClickListener { dismiss() }
        }
    }
}