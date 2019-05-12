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
import java.util.concurrent.LinkedBlockingQueue

class ProjectionCallback(
        private val callback: Writeable,
        private val mCodec: MediaCodec
) : MediaCodec.Callback() {
    private val TAG = javaClass.name

    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        try {
            val buffer = codec.getOutputBuffer(index)
            buffer!!.position(info.offset)
            val buf = ByteArray(buffer.remaining())
            buffer.get(buf, 0, info.size)
            callback.write(buf, info.flags)
            mCodec.releaseOutputBuffer(index, false)
        } catch (e: Exception) {
            Log.d(TAG,"exception: $e")
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