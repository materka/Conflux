package se.materka.conflux.ui

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso


/**
 * Created by Mattias on 2/2/2018.
 */
@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, url: String?) {
    Picasso.with(view.context)
            .load(url)
            .fit()
            .centerCrop()
            .into(view)
}