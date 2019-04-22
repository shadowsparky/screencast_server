/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.adapters

import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.shadowsparky.screencast.views.MainFragment
import ru.shadowsparky.screencast.views.SettingsFragment

class TabAdapter(
        val manager: androidx.fragment.app.FragmentManager,
        private val tab_count: Int
) : androidx.fragment.app.FragmentStatePagerAdapter(manager) {

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        var fragment: androidx.fragment.app.Fragment? = null
        when(position) {
            0 -> fragment = MainFragment()
            1 -> fragment = SettingsFragment()
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return tab_count
    }
}