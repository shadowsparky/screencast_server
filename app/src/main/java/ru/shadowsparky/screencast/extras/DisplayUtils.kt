/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.extras

import android.graphics.Point
import android.view.Display
import java.io.ByteArrayInputStream

/**
 * Используется для получения информации о дисплее
 *
 * @author shadowsparky
 * @since v1.0.0
 */
class DisplayUtils {

    /**
     * Получение информации о дисплее
     *
     * @param display экзепляр текущего дисплея
     * @param outSize возвращаемый экземляр Point, в котором будет лежать информация о дисплее
     */
    fun overrideGetSize(display: Display, outSize: Point) {
        try {
            val pointClass = Class.forName("android.graphics.Point")
            val newGetSize = Display::class.java.getMethod("getSize", *arrayOf(pointClass))
            newGetSize.invoke(display, outSize)
        } catch (ex: NoSuchMethodException) {
            outSize.x = display.width
            outSize.y = display.height
        }
    }
}