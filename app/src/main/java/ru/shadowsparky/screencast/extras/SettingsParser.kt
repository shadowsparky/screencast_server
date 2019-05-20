/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.extras

import android.content.Context
import ru.shadowsparky.screencast.SettingsChoose
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_BITRATE
import ru.shadowsparky.screencast.views.SettingsFragment

class SettingsParser(context: Context) {
    private val shared = Injection.provideSharedUtils(context)

    companion object {
        fun getSectionName(choose: SettingsChoose): String {
            return when (choose) {
                SettingsChoose.IMAGE_QUALITY -> "Битрейт"
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

    private fun parseBitrate() : Float = parseValue(SettingsChoose.IMAGE_QUALITY, " ")

    fun getBitrate() : Int {
        val bitrate = parseBitrate().toInt()
        val existsBit = SettingsFragment.BITRATE
        return when (bitrate) {
            existsBit[0] -> DEFAULT_BITRATE / 16
            existsBit[1] -> DEFAULT_BITRATE / 8
            existsBit[2] -> DEFAULT_BITRATE / 4
            existsBit[3] -> DEFAULT_BITRATE / 2
            existsBit[4] -> DEFAULT_BITRATE
            existsBit[5] -> DEFAULT_BITRATE * 3
            existsBit[6] -> DEFAULT_BITRATE * 6
            existsBit[7] -> DEFAULT_BITRATE * 10
            else -> DEFAULT_BITRATE
        }

    }

    fun getWaiting() : Int = parseValue(SettingsChoose.WAITING, " секунд").toInt() * 1000

    private fun parseValue(choose: SettingsChoose, delimiter: String = "", index: Int = 0) : Float {
        val str = shared.read(choose.name)
        val parsed_value = str.split(delimiter)
        return Integer.parseInt(parsed_value[index]).toFloat()
    }

}