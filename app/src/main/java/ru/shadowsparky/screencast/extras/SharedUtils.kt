/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.extras

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Point
import android.view.WindowManager
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.SettingsChoose
import ru.shadowsparky.screencast.views.SettingsFragment

/**
 * Обёртка используемая для работы с [SharedPreferences]
 *
 * @param context [Context] из Android SDK
 * @property preferences [SharedPreferences] из Android SDK
 * @since v1.0.0
 * @author shadowsparky
 */
class SharedUtils(val context: Context) {

    /**
     * Инициализация стандартных настроек
     *
     * @since v1.0.0
     * @author shadowsparky
     */
    fun initialize() {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        Injection.provideUtils().overrideGetSize(display, size)
        val refreshRating = display.refreshRate
        val quality = read(SettingsChoose.IMAGE_QUALITY.name)
        if ((quality == "") or (!quality.contains(context.getString(R.string.mb))) and (!quality.contains(context.getString(R.string.kb))))
            write(SettingsChoose.IMAGE_QUALITY.name, SettingsFragment.quality_list[3])
        if (read(SettingsChoose.EXPANSION.name) == "")
            write(SettingsChoose.EXPANSION.name, "${size.y}:${size.x}")
        if (read(SettingsChoose.FRAMERATE.name) == "")
            write(SettingsChoose.FRAMERATE.name, "${Math.round(refreshRating)}")
        val waiting = read(SettingsChoose.WAITING.name)
        if ((waiting == "") or (!waiting.contains(context.getString(R.string.seconds))))
            write(SettingsChoose.WAITING.name, SettingsFragment.waiting_list[2])
    }

    private val preferences: SharedPreferences = context.getSharedPreferences("", MODE_PRIVATE)

    /**
     * Записывание параметра в SharedPreferences
     *
     * @param key ключ
     * @param content значение параметра
     * @return true если запись прошла успешно, иначе false
     * @since v1.0.0
     * @author shadowsparky
     */
    fun write(key: String, content: String) : Boolean = preferences.edit().putString(key, content).commit()
    /**
     * Удаление параметра из SharedPreferences
     *
     * @param key ключ
     * @return true если удаление прошло успешно, иначе false
     * @since v1.0.0
     * @author shadowsparky
     */
    fun remove(key: String) : Boolean = preferences.edit().remove(key).commit()

    /**
     * Полная очистка [SharedPreferences]
     *
     * @return всегда true
     * @since v1.0.0
     * @author shadowsparky
     */
    fun removeAll() : Boolean {
        preferences.edit().clear().apply()
        initialize()
        return true
    }

    /**
     * Чтение параметра из [SharedPreferences]
     *
     * @param key ключ
     * @return прочитанный параметр. Если он пустой, то возвращается пустая строка
     * @since v1.0.0
     * @author shadowsparky
     */
    fun read(key: String) : String = preferences.getString(key, "")!!
}