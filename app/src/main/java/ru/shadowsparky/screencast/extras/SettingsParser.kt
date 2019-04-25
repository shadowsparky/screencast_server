/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.extras

import android.content.Context
import ru.shadowsparky.screencast.SettingsChoose

class SettingsParser(context: Context) {
    private val shared = Injection.provideSharedUtils(context)

    companion object {
        fun getSectionName(choose: SettingsChoose): String {
            return when (choose) {
                SettingsChoose.IMAGE_QUALITY -> "Качество изображения"
                SettingsChoose.EXPANSION -> "Расширение"
                SettingsChoose.FRAMERATE -> "Кадров в секунду"
                SettingsChoose.PASSWORD -> "Пароль"
                SettingsChoose.WAITING -> "Ожидание"
            }
        }
    }

    fun getFramerate() : Int {
        val framerate_str = shared.read(SettingsChoose.FRAMERATE.name)
        return Integer.parseInt(framerate_str)
    }

    fun getQuality() : Float = parseValue(SettingsChoose.IMAGE_QUALITY, "%")
    fun getWaiting() : Float = parseValue(SettingsChoose.WAITING, " секунд")

    private fun parseValue(choose: SettingsChoose, delimiter: String = "", index: Int = 0) : Float {
        val str = shared.read(choose.name)
        val parsed_value = str.split(delimiter)
        return Integer.parseInt(parsed_value[index]).toFloat()
    }

}