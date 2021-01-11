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

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import org.cosinus.launchertv.AppInfo
import org.cosinus.launchertv.R
import org.cosinus.launchertv.Utils
import org.cosinus.launchertv.activities.ApplicationList
import org.cosinus.launchertv.views.ApplicationAdapter

class ApplicationList : Activity(), OnItemClickListener, View.OnClickListener {
    //
    private var mApplication = -1
    private var mViewType = 0
    private var listView: AbsListView? = null
    private val mApplicationLoader: AsyncTask<Void, Void, Array<AppInfo>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val args = intent.extras
        if (args != null) {
            if (args.containsKey(APPLICATION_NUMBER)) mApplication = args.getInt(APPLICATION_NUMBER)
            if (args.containsKey(VIEW_TYPE)) mViewType = args.getInt(VIEW_TYPE)
        }
        setContentView(if (mViewType == VIEW_LIST) R.layout.listview else R.layout.gridview)
        listView = findViewById<View>(R.id.list) as AbsListView
        mApplicationLoader.execute()
        var v: View
        if (args != null && args.containsKey(SHOW_DELETE)) {
            if (!args.getBoolean(SHOW_DELETE)) {
                if (findViewById<View>(R.id.bottom_panel).also { v = it } != null) v.visibility = View.GONE
            }
        }
        if (findViewById<View>(R.id.delete).also { v = it } != null) v.setOnClickListener(this)
        if (findViewById<View>(R.id.cancel).also { v = it } != null) v.setOnClickListener(this)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val appInfo = view.tag as AppInfo
        val data = Intent()
        data.putExtra(PACKAGE_NAME, appInfo.packageName)
        data.putExtra(APPLICATION_NUMBER, mApplication)
        if (getParent() == null) {
            setResult(RESULT_OK, data)
        } else {
            getParent().setResult(RESULT_OK, data)
        }
        finish()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.delete -> {
                val data = Intent()
                data.putExtra(DELETE, true)
                data.putExtra(APPLICATION_NUMBER, mApplication)
                if (parent == null) setResult(RESULT_OK, data) else parent.setResult(RESULT_OK, data)
                finish()
            }
            R.id.cancel -> {
                if (parent == null) setResult(RESULT_CANCELED) else parent.setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    companion object {
        const val PACKAGE_NAME = "package_name"
        const val APPLICATION_NUMBER = "application"
        const val VIEW_TYPE = "view_type"
        const val DELETE = "delete"
        const val SHOW_DELETE = "show_delete"

        //
        const val VIEW_GRID = 0
        const val VIEW_LIST = 1
    }

    init {
        mApplicationLoader = object : AsyncTask<Void, Void, Array<AppInfo>>() {
            override fun doInBackground(vararg params: Void?): Array<AppInfo> {
                return Utils.loadApplications(this@ApplicationList).toTypedArray()
            }

            override fun onPostExecute(result: Array<AppInfo>) {
                listView!!.onItemClickListener = this@ApplicationList
                listView!!.adapter = ApplicationAdapter(this@ApplicationList,
                        if (mViewType == VIEW_LIST) R.layout.list_item else R.layout.grid_item,
                        result)
            }
        }
    }
}