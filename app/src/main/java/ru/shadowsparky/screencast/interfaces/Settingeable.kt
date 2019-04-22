/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.interfaces

import ru.shadowsparky.screencast.SettingsChoose

interface Settingeable {
    fun handle(choose: SettingsChoose)
}