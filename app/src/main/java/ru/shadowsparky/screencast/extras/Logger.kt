/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import android.util.Log
import android.util.Log.DEBUG
import android.util.Log.ERROR

/**
 * Используется как обёртка для стандартного класса из Android SDK [Log]
 * У меня [Log.d] никогда нормально не работал, поэтому я всегда использовал [Log.println].
 * Постоянно писать Log.println(DEBUG, TAG, message) достаточно утомительно, плюс singleton в коде как-то совсем не очень, то было решено сделать свой
 * собственный [Logger], который поддерживает вывод текущего потока из коробки. Такого точно раньше нигде не было.
 *
 * @since v1.0.0
 * @author shadowsparky
 */
class Logger {

    /**
     * Используется для получения имени текущего потока
     *
     * @return имя текущего потока
     * @see [Thread]
     * @since v1.0.0
     * @author shadowsparky
     */
    private fun getThreadName() : String = "THREAD: ${Thread.currentThread().name}"

    /**
     * Используется для склеивания сообщения с названием потока (при необходимости)
     *
     * @param message непосредственно сообщение
     * @param threadDump флаг, указывающий нужно ли выводить наименование потока
     * @return сообщение для вывода в отладчик
     * @since v1.0.0
     * @author shadowsparky
     */
    private fun getMessage(message: String, threadDump: Boolean) : String = if (threadDump) "${getThreadName()} $message" else message

    /**
     * Используется для вывода сообщений по приоритету.
     *
     * Копия [Log.println]
     * @param priority приоритет сообщения
     * @param message сообщение
     * @param TAG тэг сообщения
     * @since v1.0.0
     * @author shadowsparky
     */
    private fun print(priority: Int, message: String, TAG: String = Constants.TAG) = Log.println(priority, TAG, message)

    /**
     * Используется для вывода ошибок (в [ERROR] priority)
     *
     * @param message сообщение
     * @param XTAG тэг сообщения
     * @param threadDump флаг, указывающий нужно ли выводить наименование потока
     * @since v1.0.0
     * @author shadowsparky
     */
    fun printError(message: String, XTAG: String = Constants.TAG, threadDump: Boolean = false) = print(ERROR, getMessage(message, threadDump), XTAG)


    /**
     * Используется для вывода debug сообщений (в [DEBUG] priority)
     *
     * @param message сообщение
     * @param XTAG тэг сообщения
     * @param threadDump флаг, указывающий нужно ли выводить наименование потока
     * @since v1.0.0
     * @author shadowsparky
     */
    fun printDebug(message: String, XTAG: String = Constants.TAG, threadDump: Boolean = false) = print(DEBUG, getMessage(message, threadDump), XTAG)
}