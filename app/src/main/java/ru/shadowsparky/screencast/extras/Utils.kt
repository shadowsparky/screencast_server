/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.extras

import android.opengl.ETC1.getHeight
import android.R.attr.y
import android.opengl.ETC1.getWidth
import android.R.attr.x
import android.graphics.Point
import android.view.Display



class Utils {
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