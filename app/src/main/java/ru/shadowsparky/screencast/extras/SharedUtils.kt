/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.extras

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Point
import android.view.WindowManager
import ru.shadowsparky.screencast.SettingsChoose
import ru.shadowsparky.screencast.views.SettingsFragment

class SharedUtils(val context: Context) {

    fun initialize() {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        Injection.provideUtils().overrideGetSize(display, size)
        val refreshRating = display.refreshRate
        if (read(SettingsChoose.IMAGE_QUALITY.name) == "")
            write(SettingsChoose.IMAGE_QUALITY.name, SettingsFragment.quality_list[3])
        if (read(SettingsChoose.EXPANSION.name) == "")
            write(SettingsChoose.EXPANSION.name, "${size.y}:${size.x}")
        if (read(SettingsChoose.FRAMERATE.name) == "")
            write(SettingsChoose.FRAMERATE.name, "${Math.round(refreshRating)}")
        if (read(SettingsChoose.WAITING.name) == "")
            write(SettingsChoose.WAITING.name, SettingsFragment.waiting_list[2])
    }

    val preferences: SharedPreferences = context.getSharedPreferences("", MODE_PRIVATE)

    fun write(key: String, content: String) : Boolean = preferences.edit().putString(key, content).commit()
    fun remove(key: String) : Boolean = preferences.edit().remove(key).commit()
    fun removeAll() : Boolean {
        preferences.edit().clear().apply()
        initialize()
        return true
    }
    fun read(key: String) : String = preferences.getString(key, "")!!
}