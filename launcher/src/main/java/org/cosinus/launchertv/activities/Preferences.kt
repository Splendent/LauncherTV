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
package org.cosinus.launchertv.activities

import android.content.Intent
import android.content.pm.*
import android.net.Uri
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceActivity
import android.widget.Toast
import org.cosinus.launchertv.R
import org.cosinus.launchertv.Setup
import java.util.*

class Preferences : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val setup = Setup(this)
        addPreferencesFromResource(R.xml.preferences)
        bindSummary(PREFERENCE_GRID_X, R.string.summary_grid_x)
        bindSummary(PREFERENCE_GRID_Y, R.string.summary_grid_y)
        bindSummary(PREFERENCE_MARGIN_X, R.string.summary_margin_x)
        bindSummary(PREFERENCE_MARGIN_Y, R.string.summary_margin_y)
        findPreference(PREFERENCE_TRANSPARENCY).isEnabled = !setup.isDefaultTransparency
        findPreference(PREFERENCE_DEFAULT_TRANSPARENCY).onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
            findPreference(PREFERENCE_TRANSPARENCY).isEnabled = !(newValue as Boolean)
            true
        }
        findPreference(PREFERENCE_GOOGLE_PLUS).onPreferenceClickListener = OnPreferenceClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/b/104214327962194685169/104214327962194685169/posts")))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, String.format(getString(R.string.error_opening_link), "Google+", e.message),
                        Toast.LENGTH_LONG).show()
            }
            true
        }
        findPreference(PREFERENCE_GITHUB).onPreferenceClickListener = OnPreferenceClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/alescdb/LauncherTV")))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, String.format(getString(R.string.error_opening_link), "Github", e.message),
                        Toast.LENGTH_LONG).show()
            }
            true
        }
        val pInfo: PackageInfo
        var version = "#Err"
        try {
            pInfo = packageManager.getPackageInfo(packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        findPreference(PREFERENCE_ABOUT).title = getString(R.string.app_name) + " version " + version
        findPreference(PREFERENCE_ABOUT).onPreferenceClickListener = OnPreferenceClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.cosinus.launchertv")))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, String.format(getString(R.string.error_opening_link), "Play Store", e.message),
                        Toast.LENGTH_LONG).show()
            }
            true
        }
    }

    private fun bindSummary(key: String, resId: Int) {
        val p = findPreference(key) as ListPreference
        setPreferenceSummaryValue(p, resId, p.value)
        p.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
            setPreferenceSummaryValue(p, resId, newValue as String)
            true
        }
    }

    private fun setPreferenceSummaryValue(prefs: ListPreference, resId: Int, value: String) {
        prefs.summary = String.format(Locale.getDefault(), getString(resId), value)
    }

    public override fun onDestroy() {
        if (parent == null) {
            setResult(RESULT_OK, null)
        } else {
            parent.setResult(RESULT_OK, null)
        }
        super.onDestroy()
    }

    companion object {
        const val PREFERENCE_DEFAULT_TRANSPARENCY = "preference_default_transparency"
        const val PREFERENCE_TRANSPARENCY = "preference_transparency"
        const val PREFERENCE_SCREEN_ON = "preference_screen_always_on"
        const val PREFERENCE_SHOW_DATE = "preference_show_date"
        const val PREFERENCE_SHOW_BATTERY = "preference_show_battery"
        const val PREFERENCE_GRID_X = "preference_grid_x"
        const val PREFERENCE_GRID_Y = "preference_grid_y"
        const val PREFERENCE_SHOW_NAME = "preference_show_name"
        const val PREFERENCE_MARGIN_X = "preference_margin_x"
        const val PREFERENCE_MARGIN_Y = "preference_margin_y"
        const val PREFERENCE_LOCKED = "preference_locked"
        private const val PREFERENCE_GOOGLE_PLUS = "preference_google_plus"
        private const val PREFERENCE_GITHUB = "preference_github"
        private const val PREFERENCE_ABOUT = "preference_about"
    }
}