/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.interfaces

import ru.shadowsparky.screencast.SettingsChoose
import ru.shadowsparky.screencast.views.SettingsFragment

/**
 * Обратный вызов [SettingsChoose]
 *
 * @sample [SettingsFragment]
 * @since v1.0.0
 * @author shadowsparky
 */
interface ChangeSettingsHandler {
    /**
     * вызов метода callback'a
     * @param choose действие
     * @param value значение
     * @since v1.0.0
     * @author shadowsparky
     */
    fun onSettingsChanged(choose: SettingsChoose, value: String)
}