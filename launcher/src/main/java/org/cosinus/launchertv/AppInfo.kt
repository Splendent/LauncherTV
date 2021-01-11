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

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

class AppInfo {
    private val mIcon: Drawable
    private var mName: String? = null
    val packageName: String

    internal constructor(packageManager: PackageManager?, resolveInfo: ResolveInfo) {
        packageName = resolveInfo.activityInfo.packageName
        mIcon = resolveInfo.loadIcon(packageManager)
        mName = try {
            resolveInfo.loadLabel(packageManager).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    constructor(packageManager: PackageManager?, applicationInfo: ApplicationInfo) {
        packageName = applicationInfo.packageName
        mIcon = applicationInfo.loadIcon(packageManager)
        mName = try {
            applicationInfo.loadLabel(packageManager!!).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    val name: String
        get() = mName ?: ""
    val icon: Drawable?
        get() = mIcon
}