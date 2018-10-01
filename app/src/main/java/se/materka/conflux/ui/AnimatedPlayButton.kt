package se.materka.conflux.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import se.materka.conflux.R

class AnimatedPlayButton : ImageButton {

    private val playToPauseAnim: AnimatedVectorDrawableCompat? by lazy {
        AnimatedVectorDrawableCompat.create(context, R.drawable.anim_play_to_pause)
    }

    private val pauseToPlayAnim: AnimatedVectorDrawableCompat? by lazy {
        AnimatedVectorDrawableCompat.create(context, R.drawable.anim_pause_to_play)
    }

    private var isPlaying: Boolean = false

    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs)

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    init {
        setImageDrawable(playToPauseAnim)
        setOnClickListener {
            setState(!isPlaying)
        }
    }

    fun setState(playing: Boolean) {
        this.isPlaying = playing
        val drawable = if (playing) playToPauseAnim else pauseToPlayAnim
        this.setImageDrawable(drawable)
        drawable?.start()
    }
}