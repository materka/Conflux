package se.materka.conflux

import android.content.Context
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.util.AttributeSet
import android.widget.ImageButton

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