/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.shadowsparky.screencast.extras.Injection
import ru.shadowsparky.screencast.extras.Logger
import ru.shadowsparky.screencast.interfaces.Sendeable
import java.lang.Exception
import java.util.concurrent.LinkedBlockingQueue

/**
 * Реализация [MediaCodec.Callback]
 *
 * @param sender подробнее: [Sendeable]
 * @param mCodec подробнее: [MediaCodec]
 * @property handling статус подключения
 * @property log подробнее: [Logger]
 * @since v1.0.0
 * @author shadowsparky
 */
class ProjectionCallback(
        private val sender: Sendeable,
        private val mCodec: MediaCodec
) : MediaCodec.Callback() {
    var handling: Boolean = false
    private val log = Injection.provideLogger()
    private val TAG = javaClass.name

    /**
     * Вызывается, когда выходной буфер становится доступным.
     * @since v1.0.0
     * @author shadowsparky
     */
    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        if (handling) {
            try {
                val buffer = codec.getOutputBuffer(index)
                buffer!!.position(info.offset)
                val buf = ByteArray(buffer.remaining())
                buffer.get(buf, 0, info.size)
                sender.sendPicture(buf)
                mCodec.releaseOutputBuffer(index, false)
            } catch (e: IllegalStateException) {
                log.printDebug("IllegalStateException on ProjectionCallback", TAG)
            }
        }
    }

    /**
     * Вызывается, когда входной буфер становится доступным.
     * @since v1.0.0
     * @author shadowsparky
     */
    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        Log.d(TAG,"InputBufferAvailable")
    }

    /**
     * Вызывается при изменении формата вывода
     * @since v1.0.0
     * @author shadowsparky
     */
    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
        Log.d(TAG,"OutputFormatChanged")
    }

    /**
     * Вызывается, когда MediaCodec обнаружил ошибку
     * @since v1.0.0
     * @author shadowsparky
     */
    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        Log.d(TAG,"OnError")
        /*
            Здесь пусто, потому что я прочитал, что игнорирование исключений - это хороший тон.
            (прочитал в интернете, естественно)
         */
    }
}