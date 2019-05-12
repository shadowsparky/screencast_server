/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.shadowsparky.screencast.views.MainFragment
import ru.shadowsparky.screencast.views.SettingsFragment

class TabAdapter(
        manager: FragmentManager,
        private val tab_count: Int
) : FragmentStatePagerAdapter(manager) {

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
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