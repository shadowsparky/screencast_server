package ru.shadowsparky.screencast

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import java.util.concurrent.LinkedBlockingQueue

class ProjectionCallback(
        private val mSendingBuffers: LinkedBlockingQueue<ByteArray>,
        private val mCodec: MediaCodec
) : MediaCodec.Callback() {
    private val TAG = javaClass.name

    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        Log.d(TAG, "OutputBufferAvailable")
        try {
            Thread {
                val buffer = codec.getOutputBuffer(index)
                buffer!!.position(info.offset)
                buffer.limit(info.offset + info.size)
                val buf = ByteArray(buffer.remaining())
                buffer.get(buf)
                mSendingBuffers.add(buf)
                mCodec.releaseOutputBuffer(index, false)
            }.start()
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