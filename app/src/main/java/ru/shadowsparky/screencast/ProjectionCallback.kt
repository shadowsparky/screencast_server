/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import ru.shadowsparky.screencast.extras.Injection
import java.util.concurrent.LinkedBlockingQueue

class ProjectionCallback(
        private val mSendingBuffers: LinkedBlockingQueue<TransferByteArray>,
        private val mCodec: MediaCodec
) : MediaCodec.Callback() {
    private val TAG = javaClass.name
    private val log = Injection.provideLogger()

    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        Log.d(TAG, "OutputBufferAvailable")
        Thread {
            try {

                val buffer = codec.getOutputBuffer(index)
                buffer!!.position(info.offset)
                val buf = TransferByteArray(ByteArray(buffer.remaining()), buffer.remaining())
                buffer.get(buf.data, 0, info.size)
                mSendingBuffers.add(buf)
                mCodec.releaseOutputBuffer(index, false)
            } catch (e: Exception) {
                Log.d(TAG,"exception: $e")
            }
        }.start()
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