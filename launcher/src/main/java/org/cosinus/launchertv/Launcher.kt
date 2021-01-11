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

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import android.view.WindowManager
import org.cosinus.launchertv.fragments.ApplicationFragment

class Launcher : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE)
        setContentView(R.layout.activity_launcher)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ApplicationFragment.Companion.newInstance(), ApplicationFragment.Companion.TAG)
                .commit()
    }

    override fun onResume() {
        super.onResume()
        setFullScreen()
    }

    private fun setFullScreen() {
        try {
            if (Build.VERSION.SDK_INT < 19) {
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
                )
            } else {
                val decorView: View = getWindow().getDecorView()
                val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        or View.SYSTEM_UI_FLAG_IMMERSIVE)
                decorView.systemUiVisibility = uiOptions
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                        WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}