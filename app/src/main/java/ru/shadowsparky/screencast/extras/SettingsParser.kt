/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.extras

import android.content.Context
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.SettingsChoose
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_BITRATE
import ru.shadowsparky.screencast.views.SettingsFragment

/**
 * Вспомогательный класс для парсинга выбранных пользователем настроек, чтобы работать с ними
 *
 * @param context [Context] из Android SDK
 * @property shared переменная, предназначенная для работы с SharedPreferences из Android SDK [SharedUtils]
 * @since v1.0.0
 * @author shadowsparky
 */
class SettingsParser(private val context: Context) {
    private val shared = Injection.provideSharedUtils(context)

    companion object {
        /**
         * Конвертация [SettingsChoose] в строковое значение
         * @param choose подробнее: [SettingsChoose]
         * @since v1.0.0
         * @author shadowsparky
         */
        fun getSectionName(choose: SettingsChoose, context: Context): String {
            return when (choose) {
                SettingsChoose.IMAGE_QUALITY -> context.getString(R.string.bitrate)
                SettingsChoose.EXPANSION -> context.getString(R.string.screen_expansion)
                SettingsChoose.FRAMERATE -> context.getString(R.string.framerate)
                SettingsChoose.PASSWORD -> context.getString(R.string.password)
                SettingsChoose.WAITING -> context.getString(R.string.waiting)
            }
        }
    }

    /**
     * Получение выбранного пользователем Framerate
     * @return выбранный Framerate в настройках. По умолчанию максимально поддерживаемый Android устройством Framerate.
     * @since v1.0.0
     * @author shadowsparky
     */
    fun getFramerate() : Int {
        val framerate_str = shared.read(SettingsChoose.FRAMERATE.name)
        return Integer.parseInt(framerate_str)
    }

    /**
     * Парсинг битрейта.
     *
     * @return пропаршенный Bitrate
     * @since v1.0.0
     * @author shadowsparky
     */
    private fun parseBitrate() : Float = parseValue(SettingsChoose.IMAGE_QUALITY, " ")

    /**
     * Получение битрейта.
     *
     * @return выбранный Bitrate в настройках. По умолчанию: [DEFAULT_BITRATE]
     * @since v1.0.0
     * @author shadowsparky
     */
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
    /**
     * Парсинг времени ожидания.
     *
     * @return пропаршенное время ожидания, умноженное на 1000 (чтобы получить время в секундах)
     * @since v1.0.0
     * @author shadowsparky
     */
    fun getWaiting() : Int = parseValue(SettingsChoose.WAITING, " ").toInt() * 1000

    /**
     * Удобный метод для парсинга
     *
     * @param choose выбранный параметр настроек
     * @param delimiter разделитель
     * @param index индекс пропаршенного значения
     * @return Пропаршенное значение
     * @since v1.0.0
     * @author shadowsparky
     */
    private fun parseValue(choose: SettingsChoose, delimiter: String = "", index: Int = 0) : Float {
        val str = shared.read(choose.name)
        val parsed_value = str.split(delimiter)
        return Integer.parseInt(parsed_value[index]).toFloat()
    }
}