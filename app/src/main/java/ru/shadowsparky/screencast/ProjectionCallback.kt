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
import ru.shadowsparky.screencast.interfaces.Sendeable
import java.util.concurrent.LinkedBlockingQueue

class ProjectionCallback(
        private val sender: Sendeable,
        private val mCodec: MediaCodec
) : MediaCodec.Callback() {
    var handling: Boolean = false
    private val TAG = javaClass.name

    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        if (handling) {
            val buffer = codec.getOutputBuffer(index)
            buffer!!.position(info.offset)
            val buf = ByteArray(buffer.remaining())
            buffer.get(buf, 0, info.size)
            sender.sendPicture(buf)
            mCodec.releaseOutputBuffer(index, false)
        }
    }

    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        Log.d(TAG,"InputBufferAvailable")
    }

    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
        Log.d(TAG,"OutputFormatChanged")
    }

    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        Log.d(TAG,"OnError")
    }
}