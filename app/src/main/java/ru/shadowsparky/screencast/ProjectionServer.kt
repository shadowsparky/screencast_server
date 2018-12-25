package ru.shadowsparky.screencast

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.async
import ru.shadowsparky.screencast.Utils.Constants.Companion.DATA
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_BITRATE
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_DPI
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_HEIGHT
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_NOTIFICATION_ID
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_PORT
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_WIDTH
import ru.shadowsparky.screencast.Utils.Injection
import ru.shadowsparky.screencast.Utils.Logger
import ru.shadowsparky.screencast.Utils.Notifications
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket


@SuppressLint("Registered")
class ProjectionServer : Service() {
    private lateinit var mData: Intent
    private lateinit var mProjectionManager: MediaProjectionManager
    private var mProjection: MediaProjection? = null
    private var mServerSocket: ServerSocket? = null
    private var mClientSocket: Socket? = null
    private var mClientStream: DataOutputStream? = null
    private var mSurface: Surface? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mDisplay: Display? = null
    private var mHandler: Handler? = null
    private var mHandlerThread = HandlerThread("CallbackThread")
    private var mCodec: MediaCodec? = null
    private var mFormat: MediaFormat? = null
    private var mCallback: MediaCodec.Callback? = null
    private val mSendingBuffers = Injection.provideByteQueue()
    private val log: Logger = Injection.provideLogger()


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log.printDebug("STARTED")
        mData = intent!!.getParcelableExtra(DATA)
        mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = Notifications(this).provideNotification(notificationService)
        startForeground(DEFAULT_NOTIFICATION_ID, notification)
        startServer()
        return START_NOT_STICKY
    }

    fun startServer() = GlobalScope.async(Dispatchers.IO) {
        mServerSocket = ServerSocket(DEFAULT_PORT)
        log.printDebug("Waiting connection...")
        mClientSocket = mServerSocket!!.accept()
        log.printDebug("CONNECTION ACCEPTED")
        mClientStream = DataOutputStream(mClientSocket!!.getOutputStream())
        startProjection()
        sendProjectionData()
    }

    fun sendProjectionData() = GlobalScope.async {
        val byteOut = ByteArrayOutputStream()
        val out = DataOutputStream(byteOut)
        while (true) {
            val buf = mSendingBuffers.take()
            out.write(buf)
            byteOut.writeTo(mClientStream)
            log.printDebug("Array sent $buf")
        }
    }

    fun startProjection() {
        mProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mData)
        mDisplay = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        mFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, DEFAULT_WIDTH, DEFAULT_HEIGHT)
        mFormat!!.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_BITRATE)
        mFormat!!.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mFormat!!.setFloat(MediaFormat.KEY_FRAME_RATE, mDisplay!!.refreshRate)
        mFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mCallback = object : MediaCodec.Callback() {
            override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                log.printDebug("OutputBufferAvailable")
                try {
                    Thread {
                        val buffer = codec.getOutputBuffer(index)
                        buffer!!.position(info.offset)
                        val buf = ByteArray(buffer.remaining())
                        log.printDebug(buffer.remaining().toString())
                        mSendingBuffers.add(buf)
                        mCodec!!.releaseOutputBuffer(index, false)
                    }.start()
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
        mCodec!!.setCallback(mCallback)
        mCodec!!.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mSurface = mCodec!!.createInputSurface()
        mCodec!!.start()
        mVirtualDisplay = mProjection!!.createVirtualDisplay("Projection", DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DPI, 0, mSurface, null, null)
    }
}
