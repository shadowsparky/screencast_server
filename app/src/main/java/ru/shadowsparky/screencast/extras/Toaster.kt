/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import android.content.Context
import android.widget.Toast

/**
 * Класс для удобной работы с toast уведомлениями
 *
 * @since v1.0.0
 * @author shadowsparky
 */
class Toaster {
    /**
     * Вывод Toast уведомления
     *
     * @param context [Context] из Android SDK
     * @param message сообщение
     * @sample show(context, "Тестовое сообщение")
     * @since v1.0.0
     * @author shadowsparky
     */
    fun show(context: Context, message: String) =
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}