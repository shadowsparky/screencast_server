package ru.shadowsparky.screencast

import android.media.MediaCodec
import android.media.MediaFormat
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import ru.shadowsparky.screencast.Utils.Injection
import java.util.concurrent.LinkedBlockingQueue

class ProjectionCallback(
        private val mSendingBuffers: LinkedBlockingQueue<ByteArray>,
        private val mCodec: MediaCodec
) : MediaCodec.Callback() {
    private val log = Injection.provideLogger()

    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        log.printDebug("OutputBufferAvailable")
        try {
            GlobalScope.async {
                val buffer = codec.getOutputBuffer(index)
                buffer!!.position(info.offset)
                val buf = ByteArray(buffer.remaining())
                log.printDebug(buffer.remaining().toString())
                mSendingBuffers.add(buf)
                mCodec.releaseOutputBuffer(index, false)
            }
        } catch (e: Exception) {
            log.printError("exception: ${e.toString()}")
        }
    }

    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        log.printDebug("InputBufferAvailable")
    }

    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
        log.printDebug("OutputFormatChanged")
    }

    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        log.printDebug("OnError")
    }
}