/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.shadowsparky.screencast.views.MainFragment
import ru.shadowsparky.screencast.views.SettingsFragment

/**
 * Используется для вставки фрагментов в Tab. Исключительно для дизайна.
 *
 * @param manager менеджер фраментов, использующийся для "вставки" фрагметов в Tab
 * @param tab_count количество табов
 * @constructor Создает Tab адаптер
 */
class TabAdapter(
        manager: FragmentManager,
        private val tab_count: Int
) : FragmentStatePagerAdapter(manager) {

    /**
     * Получает текущий элемент Tab'a
     *
     * @param position позиция Tab'a
     * @return возвращает фрагмент, являющийся текущим элементом таба
     */
    override fun getItem(position: Int): Fragment {
        val fragment: Fragment = when(position) {
            0 -> MainFragment()
            1 -> SettingsFragment()
            else -> throw NullPointerException("Unrecognized position $position")
        }
        fragment.retainInstance = true
        return fragment
    }

    /**
     * Возвращает количество табов
     *
     * @return возвращает количество табов
     */
    override fun getCount(): Int {
        return tab_count
    }
}