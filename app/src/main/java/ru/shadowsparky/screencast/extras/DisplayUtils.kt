/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.extras

import android.graphics.Point
import android.view.Display
import java.io.ByteArrayInputStream


class DisplayUtils {
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