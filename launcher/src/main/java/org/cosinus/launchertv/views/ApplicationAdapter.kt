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

import android.content.*
import android.view.*
import android.widget.*
import org.cosinus.launchertv.AppInfo
import org.cosinus.launchertv.R

class ApplicationAdapter(context: Context?, private val mResource: Int, items: Array<AppInfo>?) : ArrayAdapter<AppInfo?>(context!!, R.layout.list_item, items!!) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        view = convertView ?: View.inflate(context, mResource, null)
        val packageImage = view.findViewById<View>(R.id.application_icon) as ImageView
        val packageName = view.findViewById<View>(R.id.application_name) as TextView
        val appInfo = getItem(position)
        if (appInfo != null) {
            view.tag = appInfo
            packageName.text = appInfo.name
            if (appInfo.icon != null) packageImage.setImageDrawable(appInfo.icon)
        }
        return view
    }
}