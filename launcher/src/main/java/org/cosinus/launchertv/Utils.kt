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
import android.util.TypedValue
import java.util.*

object Utils {
    fun loadApplications(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        var mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val intentActivities = packageManager.queryIntentActivities(mainIntent, 0)
        mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        intentActivities.addAll(packageManager.queryIntentActivities(mainIntent, 0))
        val knownPackages: MutableSet<String> = HashSet()
        val entries: MutableList<AppInfo> = ArrayList()
        if (intentActivities != null) {
            for (resolveInfo in intentActivities) {
                if (context.packageName != resolveInfo.activityInfo.packageName &&
                        !knownPackages.contains(resolveInfo.activityInfo.packageName)) {
                    entries.add(AppInfo(packageManager, resolveInfo))
                    knownPackages.add(resolveInfo.activityInfo.packageName)
                }
            }
        }
        Collections.sort(entries) { lhs, rhs -> lhs.name.compareTo(rhs.name, ignoreCase = true) }
        return entries
    }

    fun SettingAppIntent(context: Context): AppInfo? {
        var result: AppInfo? = null
        for (app in loadApplications(context)) {
            if (app.packageName === "com.android.settings") result = app
        }
        return result
    }

    fun getPixelFromDp(context: Context, dp: Int): Int {
        val r = context.resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics).toInt()
    }
}