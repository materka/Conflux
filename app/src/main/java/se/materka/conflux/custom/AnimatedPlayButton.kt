package se.materka.conflux.custom

import android.content.Context
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.util.AttributeSet
import android.widget.ImageButton
import se.materka.conflux.R


/**
 * Created by Privat on 7/1/2017.
 */
class AnimatedPlayButton : ImageButton {

    private val playToPauseAnim: AnimatedVectorDrawableCompat? by lazy {
        AnimatedVectorDrawableCompat.create(context, R.drawable.play_to_pause_anim)
    }

    private val pauseToPlayAnim: AnimatedVectorDrawableCompat? by lazy {
        AnimatedVectorDrawableCompat.create(context, R.drawable.pause_to_play_anim)
    }

    private var isShowingPlay = true
    private var currentDrawable :AnimatedVectorDrawableCompat? = null

    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs)

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(ctx, attrs, defStyleAttr, defStyleRes)

    fun toggle() {
        currentDrawable = if (isShowingPlay) playToPauseAnim else pauseToPlayAnim
        this.setImageDrawable(currentDrawable)
        currentDrawable?.start()
        isShowingPlay = !isShowingPlay
    }
}