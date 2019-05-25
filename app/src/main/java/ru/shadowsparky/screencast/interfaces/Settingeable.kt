/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.interfaces

import ru.shadowsparky.screencast.SettingsChoose

/**
 * Обратный вызов смены настроек
 */
interface Settingeable {
    /**
     * Вызов метода callback'a при смене настроек
     *
     * @param choose наименование раздела, в котором произошло изменение
     * @since v1.0.0
     * @author shadowsparky
     */
    fun onSettingChoosed(choose: SettingsChoose)
}