/*
 * Simple TV Launcher
 * Copyright 2017 Alexandre Del Bigio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cosinus.launchertv.views

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.StateSet
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.cosinus.launchertv.R
import org.cosinus.launchertv.Setup
import java.util.*

class ApplicationView : LinearLayout {
    private var mMenuClickListener: View.OnClickListener? = null
    private var mIcon: ImageView? = null
    private var mText: TextView? = null
    var packageName: String? = null
        private set
    var position = 0

    constructor(context: Context) : super(context) {
        initialize(context, null, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs, null)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initialize(context, attrs, defStyle)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.d(ContentValues.TAG, "keyCode => $keyCode")
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mMenuClickListener != null) {
                mMenuClickListener!!.onClick(this)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun setOnMenuOnClickListener(clickListener: View.OnClickListener?) {
        mMenuClickListener = clickListener
    }

    private fun setBackgroundStateDrawable(transparency: Float) {
        val stateListDrawable = StateListDrawable()
        val drawableEnabled = createTileShape(
                Color.argb(getTransparency(transparency, 0.0f), 0xF0, 0xF0, 0xF0),
                Color.argb(0xFF, 0x90, 0x90, 0x90)
        )
        val drawableFocused = createTileShape(
                Color.argb(getTransparency(transparency, 0.4f), 0xE0, 0xE0, 0xFF),
                Color.argb(0xFF, 0x90, 0x90, 0x90)
        )
        val drawablePressed = createTileShape(
                Color.argb(getTransparency(transparency, 0.8f), 0xE0, 0xE0, 0xFF),
                Color.argb(0xFF, 0x00, 0x00, 0x00)
        )

        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePressed)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_focused), drawableFocused)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_hovered), drawableFocused)
        stateListDrawable.addState(StateSet.WILD_CARD, drawableEnabled)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setBackground(stateListDrawable)
        } else {
            setBackgroundDrawable(stateListDrawable)
        }
    }

    private fun getTransparency(transparency: Float, add: Float): Int {
        val trans = ((transparency + add) * 255.0).toInt()
        if (trans > 255) return 255
        return if (trans < 0) 0 else trans
    }

    private fun initialize(context: Context, attrs: AttributeSet?, defStyle: Int?) {
        val setup = Setup(context)
        inflate(context, R.layout.application, this)
        setClickable(true)
        setFocusable(true)
        if (!setup.isDefaultTransparency) {
            setBackgroundStateDrawable(setup.transparency)
        } else {
            setBackgroundResource(R.drawable.application_selector)
        }
        mIcon = findViewById<View>(R.id.application_icon) as ImageView?
        mText = findViewById<View>(R.id.application_name) as TextView?
    }

    fun setImageResource(@DrawableRes res: Int): ApplicationView {
        mIcon!!.setImageResource(res)
        return this
    }

    fun setImageDrawable(drawable: Drawable?): ApplicationView {
        mIcon!!.setImageDrawable(drawable)
        return this
    }

    fun setText(text: CharSequence?): ApplicationView {
        mText?.setText(text)
        return this
    }

    fun showName(show: Boolean) {
        mText?.setVisibility(
                if (show) View.VISIBLE else View.GONE
        )
    }

    fun setPackageName(packageName: String?): ApplicationView {
        this.packageName = packageName
        return this
    }

    val name: String
        get() = mText?.getText().toString()

    fun hasPackage(): Boolean {
        return !TextUtils.isEmpty(packageName)
    }

    val preferenceKey: String
        get() = getPreferenceKey(position)

    companion object {
        fun getPreferenceKey(appNum: Int): String {
            return String.format(Locale.getDefault(), "application_%02d", appNum)
        }

        private fun createTileShape(backgroundColor: Int, borderColor: Int): Drawable {
            val shape = GradientDrawable()
            shape.setShape(GradientDrawable.RECTANGLE)
            shape.setCornerRadii(floatArrayOf(7f, 7f, 7f, 7f, 0f, 0f, 0f, 0f))
            shape.setColor(backgroundColor)
            shape.setStroke(1, borderColor)
            shape.setBounds(7, 7, 7, 7)
            return shape
        }
    }
}