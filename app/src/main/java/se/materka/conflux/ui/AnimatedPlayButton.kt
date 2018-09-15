package se.materka.conflux.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import se.materka.conflux.R

class AnimatedPlayButton : ImageButton {

    private val playToPauseAnim: AnimatedVectorDrawableCompat? by lazy {
        AnimatedVectorDrawableCompat.create(context, R.drawable.play_to_pause_anim)
    }

    private val pauseToPlayAnim: AnimatedVectorDrawableCompat? by lazy {
        AnimatedVectorDrawableCompat.create(context, R.drawable.pause_to_play_anim)
    }

    private var isShowingPlay = true

    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs)

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(ctx, attrs, defStyleAttr, defStyleRes)

    init {
        setImageDrawable(playToPauseAnim)
    }

    fun toggle() {
        val drawable = if (isShowingPlay) playToPauseAnim else pauseToPlayAnim
        this.setImageDrawable(drawable)
        drawable?.start()
        isShowingPlay = !isShowingPlay
    }

    fun showPlay() {
        if (!isShowingPlay) {
            toggle()
        }
    }

    fun showPause() {
        if (isShowingPlay) {
            toggle()
        }
    }
}