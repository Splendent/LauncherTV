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
package org.cosinus.launchertv

import android.content.*
import android.preference.PreferenceManager
import org.cosinus.launchertv.activities.Preferences

class Setup(private val mContext: Context) {
    private var mPreferences: SharedPreferences? = null
    private val preferences: SharedPreferences?
        private get() {
            if (mPreferences == null) {
                mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
            }
            return mPreferences
        }

    private fun getInt(name: String, defaultValue: Int): Int {
        try {
            val value = preferences!!.getString(name, null)
            if (value != null) return value.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return defaultValue
    }

    val isDefaultTransparency: Boolean
        get() {
            try {
                return preferences!!.getBoolean(Preferences.Companion.PREFERENCE_DEFAULT_TRANSPARENCY, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return true
        }
    val transparency: Float
        get() {
            try {
                return preferences!!.getFloat(Preferences.Companion.PREFERENCE_TRANSPARENCY, 0.5f)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return 0.5f
        }

    fun keepScreenOn(): Boolean {
        try {
            return preferences!!.getBoolean(Preferences.Companion.PREFERENCE_SCREEN_ON, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun iconsLocked(): Boolean {
        try {
            return preferences!!.getBoolean(Preferences.Companion.PREFERENCE_LOCKED, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun showDate(): Boolean {
        try {
            return preferences!!.getBoolean(Preferences.Companion.PREFERENCE_SHOW_DATE, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    fun showBattery(): Boolean {
        try {
            return preferences!!.getBoolean(Preferences.Companion.PREFERENCE_SHOW_BATTERY, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun showNames(): Boolean {
        try {
            return preferences!!.getBoolean(Preferences.Companion.PREFERENCE_SHOW_NAME, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    val gridX: Int
        get() = getInt(Preferences.Companion.PREFERENCE_GRID_X, DEFAULT_GRID_X)
    val gridY: Int
        get() = getInt(Preferences.Companion.PREFERENCE_GRID_Y, DEFAULT_GRID_Y)
    val marginX: Int
        get() = getInt(Preferences.Companion.PREFERENCE_MARGIN_X, DEFAULT_MARGIN_X)
    val marginY: Int
        get() = getInt(Preferences.Companion.PREFERENCE_MARGIN_Y, DEFAULT_MARGIN_Y)

    companion object {
        private const val DEFAULT_GRID_X = 3
        private const val DEFAULT_GRID_Y = 2
        private const val DEFAULT_MARGIN_X = 5
        private const val DEFAULT_MARGIN_Y = 5
    }
}