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
package org.cosinus.launchertv.fragments

import android.app.Activity
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.cosinus.launchertv.AppInfo
import org.cosinus.launchertv.R
import org.cosinus.launchertv.Setup
import org.cosinus.launchertv.Utils
import org.cosinus.launchertv.activities.ApplicationList
import org.cosinus.launchertv.activities.Preferences
import org.cosinus.launchertv.views.ApplicationView
import java.text.DateFormat
import java.util.*


class ApplicationFragment : Fragment(), View.OnClickListener, View.OnLongClickListener {

    companion object {
        val TAG = "ApplicationFragment"
        private val PREFERENCES_NAME = "applications"
        private val REQUEST_CODE_APPLICATION_LIST = 0x1E
        private val REQUEST_CODE_WALLPAPER = 0x1F
        private val REQUEST_CODE_APPLICATION_START = 0x20
        private val REQUEST_CODE_PREFERENCES = 0x21
        fun newInstance(): ApplicationFragment {
            return ApplicationFragment()
        }
    }

    private var mClock: TextView? = null
    private var mDate: TextView? = null
    private var mTimeFormat: DateFormat? = null
    private var mDateFormat: DateFormat? = null
    private var mBatteryLevel: TextView? = null
    private var mBatteryIcon: ImageView? = null
    private val mBatteryChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            mBatteryLevel?.setText(
                    java.lang.String.format(getResources().getString(R.string.battery_level_text), level)
            )
            val batteryIconId: Int = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0)
            mBatteryIcon!!.setImageDrawable(getResources().getDrawable(batteryIconId))
        }
    }
    private var mBatteryChangedReceiverRegistered = false
    private val mHandler = Handler()
    private val mTimerTick = Runnable { setClock() }
    private var mGridX = 3
    private var mGridY = 2
    private var mContainer: LinearLayout? = null
    private var mApplications: Array<Array<ApplicationView?>>? = null
    private var mSettings: View? = null
    private var mGridView: View? = null
    private var mSetup: Setup? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_application, container, false)
        mSetup = Setup(getContext())
        mContainer = view.findViewById<View>(R.id.container) as LinearLayout
        mSettings = view.findViewById(R.id.settings)
        mGridView = view.findViewById(R.id.application_grid)
        mClock = view.findViewById<View>(R.id.clock) as TextView
        mDate = view.findViewById<View>(R.id.date) as TextView
        val batteryLayout: LinearLayout = view.findViewById<View>(R.id.battery_layout) as LinearLayout
        mBatteryLevel = view.findViewById<View>(R.id.battery_level) as TextView
        mBatteryIcon = view.findViewById<View>(R.id.battery_icon) as ImageView
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(getActivity())
        mDateFormat = android.text.format.DateFormat.getLongDateFormat(getActivity())
        if (mSetup!!.keepScreenOn()) mContainer!!.setKeepScreenOn(true)
        if (mSetup!!.showDate() == false) mDate!!.setVisibility(View.GONE)
        if (mSetup!!.showBattery()) {
            batteryLayout.setVisibility(View.VISIBLE)
            getActivity().registerReceiver(mBatteryChangedReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            mBatteryChangedReceiverRegistered = true
        } else {
            batteryLayout.setVisibility(View.INVISIBLE)
            if (mBatteryChangedReceiverRegistered) {
                getActivity().unregisterReceiver(mBatteryChangedReceiver)
                mBatteryChangedReceiverRegistered = false
            }
        }
        mSettings?.setOnClickListener(this)
        mGridView?.setOnClickListener(this)
        createApplications()
        return view
    }

    private fun createApplications() {
        mContainer?.removeAllViews()
        mGridX = mSetup!!.gridX
        mGridY = mSetup!!.gridY
        if (mGridX < 2) mGridX = 2
        if (mGridY < 1) mGridY = 1
        val marginX = Utils.getPixelFromDp(getContext(), mSetup!!.marginX)
        val marginY = Utils.getPixelFromDp(getContext(), mSetup!!.marginY)
        val showNames: Boolean = mSetup!!.showNames()
        mApplications = Array<Array<ApplicationView?>>(mGridY) { arrayOfNulls<ApplicationView>(mGridX) }
        var position = 0
        for (y in 0 until mGridY) {
            val ll = LinearLayout(getContext())
            ll.setOrientation(LinearLayout.HORIZONTAL)
            ll.setGravity(Gravity.CENTER_VERTICAL)
            ll.setFocusable(false)
            ll.setLayoutParams(LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1F
            ))
            for (x in 0 until mGridX) {
                val av = ApplicationView(getContext())
                av.setOnClickListener(this)
                //SP: bad
                if (x != 0) {
                    av.setOnLongClickListener(this)
                    av.setOnMenuOnClickListener(View.OnClickListener { v -> onLongClick(v) })
                }
                av.position = position++
                av.showName(showNames)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    av.setId(0x00FFFFFF + position)
                } else {
                    av.setId(View.generateViewId())
                }
                val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1F)
                lp.setMargins(marginX, marginY, marginX, marginY)
                av.setLayoutParams(lp)
                ll.addView(av)
                mApplications!![y][x] = av
            }
            mContainer?.addView(ll)
        }
        updateApplications()
        setApplicationOrder()
    }

    private fun setApplicationOrder() {
        for (y in 0 until mGridY) {
            for (x in 0 until mGridX) {
                var upId: Int = R.id.application_grid
                var downId: Int = R.id.settings
                var leftId: Int = R.id.application_grid
                var rightId: Int = R.id.settings
                if (y > 0) upId = mApplications!![y - 1][x]!!.getId()
                if (y + 1 < mGridY) downId = mApplications!![y + 1][x]!!.getId()
                if (x > 0) leftId = mApplications!![y][x - 1]!!.getId() else if (y > 0) leftId = mApplications!![y - 1][mGridX - 1]!!.getId()
                if (x + 1 < mGridX) rightId = mApplications!![y][x + 1]!!.getId() else if (y + 1 < mGridY) rightId = mApplications!![y + 1][0]!!.getId()
                mApplications!![y][x]!!.setNextFocusLeftId(leftId)
                mApplications!![y][x]!!.setNextFocusRightId(rightId)
                mApplications!![y][x]!!.setNextFocusUpId(upId)
                mApplications!![y][x]!!.setNextFocusDownId(downId)
            }
        }
        mGridView!!.nextFocusLeftId = R.id.settings
        mGridView!!.nextFocusRightId = mApplications!![0][0]!!.getId()
        mGridView!!.nextFocusUpId = R.id.settings
        mGridView!!.nextFocusDownId = mApplications!![0][0]!!.getId()
        mSettings!!.nextFocusLeftId = mApplications!![mGridY - 1][mGridX - 1]!!.getId()
        mSettings!!.nextFocusRightId = R.id.application_grid
        mSettings!!.nextFocusUpId = mApplications!![mGridY - 1][mGridX - 1]!!.getId()
        mSettings!!.nextFocusDownId = R.id.application_grid
    }

    private fun querySettingPkgName(): String? {
        val intent = Intent(Settings.ACTION_SETTINGS)
        val resolveInfos: List<ResolveInfo> = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (resolveInfos == null || resolveInfos.size == 0) {
            ""
        } else resolveInfos[0].activityInfo.packageName
    }

    private fun updateApplications() {
        val pm: PackageManager = getActivity().getPackageManager()
        val prefs: SharedPreferences = getActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

        for (y in 0 until mGridY) {
            for (x in 0 until mGridX) {
                val app: ApplicationView? = mApplications!![y][x]
                setApplication(pm, app, prefs.getString(app?.preferenceKey, null))
            }
        }
    }

    private fun restartActivity() {
        if (mBatteryChangedReceiverRegistered) {
            getActivity().unregisterReceiver(mBatteryChangedReceiver)
            mBatteryChangedReceiverRegistered = false
        }
        val intent: Intent = getActivity().getIntent()
        getActivity().finish()
        startActivity(intent)
    }

    private fun writePreferences(appNum: Int, packageName: String?) {
        val prefs: SharedPreferences = getActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        val key: String = ApplicationView.Companion.getPreferenceKey(appNum)
        if (TextUtils.isEmpty(packageName)) {
            editor.remove(key)
        } else {
            editor.putString(key, packageName)
        }
        editor.apply()
    }

    private fun setApplication(pm: PackageManager, app: ApplicationView?, packageName: String?) {
        try {
            if (TextUtils.isEmpty(packageName) == false) {
                val pi: PackageInfo = pm.getPackageInfo(packageName!!, 0)
                if (pi != null) {
                    val appInfo = AppInfo(pm, pi.applicationInfo)
                    app?.setImageDrawable(appInfo.icon)
                            ?.setText(appInfo.name)
                            ?.setPackageName(appInfo.packageName)
                }
            } else {
                app?.setImageResource(R.drawable.ic_add)
                        ?.setText("")
                        ?.setPackageName(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        setClock()
        if (mSetup?.showBattery() == true && !mBatteryChangedReceiverRegistered) {
            getActivity().registerReceiver(mBatteryChangedReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            mBatteryChangedReceiverRegistered = true
        }
        mHandler.postDelayed(mTimerTick, 1000)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(mTimerTick)
        if (mBatteryChangedReceiverRegistered) {
            getActivity().unregisterReceiver(mBatteryChangedReceiver)
        }
    }

    private fun setClock() {
        val date = Date(System.currentTimeMillis())
        mClock?.setText(mTimeFormat!!.format(date))
        mDate?.setText(mDateFormat!!.format(date))
        mHandler.postDelayed(mTimerTick, 1000)
    }

    override fun onLongClick(v: View): Boolean {
        if (v is ApplicationView) {
            val appView: ApplicationView = v as ApplicationView
            if (appView.hasPackage() && mSetup?.iconsLocked() == true) {
                Toast.makeText(getActivity(), R.string.home_locked, Toast.LENGTH_SHORT).show()
            } else {
                openApplicationList(ApplicationList.Companion.VIEW_LIST, appView.position, appView.hasPackage(), REQUEST_CODE_APPLICATION_LIST)
            }
            return true
        }
        return false
    }

    override fun onClick(v: View) {
        if (v is ApplicationView) {
            openApplication(v as ApplicationView)
            return
        }
        when (v.id) {
            R.id.application_grid -> {
                openApplicationList(ApplicationList.Companion.VIEW_GRID, 0, false, REQUEST_CODE_APPLICATION_START)
            }
            R.id.settings -> startActivityForResult(Intent(getContext(), Preferences::class.java), REQUEST_CODE_PREFERENCES)
        }
    }

    private fun openApplication(v: ApplicationView) {
        if (v.hasPackage() == false) {
            openApplicationList(ApplicationList.Companion.VIEW_LIST, v.position, false, REQUEST_CODE_APPLICATION_LIST)
            return
        }
        try {
            Toast.makeText(getActivity(), v.name, Toast.LENGTH_SHORT).show()
            v.packageName?.let { startActivity(getLaunchIntentForPackage(it)) }
        } catch (e: Exception) {
            if (v.packageName?.endsWith("settings") == true) {
                Toast.makeText(getActivity(), v.name + " action", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } else {
                Toast.makeText(getActivity(), v.name + " : " + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openApplication(packageName: String) {
        try {
            val startApp: Intent? = getLaunchIntentForPackage(packageName)
            Toast.makeText(getActivity(), packageName, Toast.LENGTH_SHORT).show()
            startActivity(startApp)
        } catch (e: Exception) {
            Toast.makeText(getActivity(), packageName + " : " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun openApplicationList(viewType: Int, appNum: Int, showDelete: Boolean, requestCode: Int) {
        val intent = Intent(getActivity(), ApplicationList::class.java)
        intent.putExtra(ApplicationList.Companion.APPLICATION_NUMBER, appNum)
        intent.putExtra(ApplicationList.Companion.VIEW_TYPE, viewType)
        intent.putExtra(ApplicationList.Companion.SHOW_DELETE, showDelete)
        startActivityForResult(intent, requestCode)
    }

    private fun getLaunchIntentForPackage(packageName: String): Intent? {
        val pm: PackageManager = getActivity().getPackageManager()
        var launchIntent: Intent? = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                launchIntent = pm.getLeanbackLaunchIntentForPackage(packageName)
            }
        }
        return launchIntent
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            REQUEST_CODE_WALLPAPER -> {
            }
            REQUEST_CODE_PREFERENCES -> restartActivity()
            REQUEST_CODE_APPLICATION_START -> if (intent != null) {
                intent.getExtras()?.getString(ApplicationList.Companion.PACKAGE_NAME)?.let { openApplication(it) }
            }
            REQUEST_CODE_APPLICATION_LIST -> if (resultCode == Activity.RESULT_OK) {
                val extra: Bundle? = intent?.getExtras()
                val appNum: Int? = intent?.getExtras()?.getInt(ApplicationList.Companion.APPLICATION_NUMBER)
                appNum?.let {
                    if (extra?.containsKey(ApplicationList.Companion.DELETE) == true && extra?.getBoolean(ApplicationList.Companion.DELETE)) {
                        writePreferences(it, null)
                    } else {
                        writePreferences(it,
                                intent?.getExtras()?.getString(ApplicationList.Companion.PACKAGE_NAME)
                        )
                    }
                }
                updateApplications()
            }

        }
    }
}