package org.cosinus.launchertv.views

import android.annotation.SuppressLint
import android.content.*
import android.content.res.TypedArray
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import org.cosinus.launchertv.R

/*
* Copyright 2012 Jay Weisskopf
*
* Licensed under the MIT License (see LICENSE.txt)
*/ /**
 * @author Jay Weisskopf
 */
class SliderPreference : DialogPreference {
    protected var mValue = 0f
    protected var mSeekBarValue = 0
    protected var mSummaries: Array<CharSequence>? = null

    /**
     * @param context
     * @param attrs
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context, attrs)
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setup(context, attrs)
    }

    private fun setup(context: Context, attrs: AttributeSet) {
        dialogLayoutResource = R.layout.slider_preference_dialog
        val a = context.obtainStyledAttributes(attrs, R.styleable.SliderPreference)
        try {
            setSummary(a.getTextArray(R.styleable.SliderPreference_android_summary))
        } catch (e: Exception) {
            // Do nothing
        }
        a.recycle()
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getFloat(index, 0f)
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any) {
        setValue((if (restoreValue) getPersistedFloat(mValue) else defaultValue as Float))
    }

    override fun getSummary(): CharSequence {
        return if (mSummaries != null && mSummaries!!.size > 0) {
            var index = (mValue * mSummaries!!.size).toInt()
            index = Math.min(index, mSummaries!!.size - 1)
            mSummaries!![index]
        } else {
            super.getSummary()
        }
    }

    fun setSummary(summaries: Array<CharSequence>?) {
        mSummaries = summaries
    }

    override fun setSummary(summary: CharSequence) {
        super.setSummary(summary)
        mSummaries = null
    }

    @SuppressLint("ResourceType")
    override fun setSummary(summaryResId: Int) {
        try {
            val a = context.resources.getStringArray(summaryResId)
            this.setSummary(a as? Array<CharSequence>)
        } catch (e: Exception) {
            super.setSummary(summaryResId)
        }
    }

    fun setValue(value: Float) {
        var value = value
        value = Math.max(0f, Math.min(value, 1f)) // clamp to [0, 1]
        if (shouldPersist()) {
            persistFloat(value)
        }
        if (value != mValue) {
            mValue = value
            notifyChanged()
        }
    }

    override fun onCreateDialogView(): View {
        mSeekBarValue = (mValue * SEEKBAR_RESOLUTION).toInt()
        val view = super.onCreateDialogView()
        val seekbar = view.findViewById<View>(R.id.slider_preference_seekbar) as SeekBar
        seekbar.max = SEEKBAR_RESOLUTION
        seekbar.progress = mSeekBarValue
        seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mSeekBarValue = progress
                }
            }
        })
        return view
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        val newValue = mSeekBarValue.toFloat() / SEEKBAR_RESOLUTION
        if (positiveResult && callChangeListener(newValue)) {
            setValue(newValue)
        }
        super.onDialogClosed(positiveResult)
    } // TODO: Save and restore preference state.

    companion object {
        protected const val SEEKBAR_RESOLUTION = 10000
    }
}