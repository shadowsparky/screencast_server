package ru.shadowsparky.screencast

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
import android.os.IBinder
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import ru.shadowsparky.screencast.Utils.Constants.Companion.DATA
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_BITRATE
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_DPI
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_HEIGHT
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_NOTIFICATION_ID
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_PORT
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_PROJECTION_NAME
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_WIDTH
import ru.shadowsparky.screencast.Utils.Injection
import ru.shadowsparky.screencast.Utils.Logger
import ru.shadowsparky.screencast.Utils.Notifications
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class ProjectionServer : Service() {
    private lateinit var mData: Intent
    private lateinit var mProjectionManager: MediaProjectionManager
    private val TAG = javaClass.name
    private var mProjection: MediaProjection? = null
    private var mServerSocket: ServerSocket? = null
    private var mClientSocket: Socket? = null
    private var mClientStream: DataOutputStream? = null
    private var mSurface: Surface? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mDisplay: Display? = null
    private var mCodec: MediaCodec? = null
    private var mFormat: MediaFormat? = null
    private var mCallback: MediaCodec.Callback? = null
    private val mSendingBuffers = Injection.provideByteQueue()
    private val log: Logger = Injection.provideLogger()


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mData = intent!!.getParcelableExtra(DATA)
        mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        createNotification()
        startServer()
        return START_NOT_STICKY
    }

    private fun createNotification() {
        val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = Notifications(this).provideNotification(notificationService)
        startForeground(DEFAULT_NOTIFICATION_ID, notification)
    }

    private fun startServer() = GlobalScope.async {
        mServerSocket = ServerSocket(DEFAULT_PORT)
        log.printDebug("Waiting connection...")
        mClientSocket = mServerSocket!!.accept()
        log.printDebug("CONNECTION ACCEPTED")
        mClientStream = DataOutputStream(mClientSocket!!.getOutputStream())
        configureProjection()
        startProjection()
        sendProjectionData()
        return@async "adsasd"
    }

    private fun sendProjectionData() = GlobalScope.async {
        try {
            while (true) {
                if (mClientStream != null) {
                    val data = mSendingBuffers.take()
                    mClientStream!!.write(data)
                    mClientStream!!.flush()
                    log.printDebug("Data sent $data", TAG)
                } else {
                    log.printError("CLIENT STREAM IS NULL", TAG)
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun configureProjection() {
        mProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mData)
        mDisplay = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        mFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, DEFAULT_WIDTH, DEFAULT_HEIGHT)
        mFormat!!.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_BITRATE)
        mFormat!!.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mFormat!!.setFloat(MediaFormat.KEY_FRAME_RATE, mDisplay!!.refreshRate)
        mFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mCallback = ProjectionCallback(mSendingBuffers, mCodec!!)
        mCodec!!.setCallback(mCallback)
        mCodec!!.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mSurface = mCodec!!.createInputSurface()
    }

    private fun startProjection() {
        mCodec!!.start()
        mVirtualDisplay = mProjection!!.createVirtualDisplay(DEFAULT_PROJECTION_NAME, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DPI, 0, mSurface, null, null)
    }
}