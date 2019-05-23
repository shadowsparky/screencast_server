/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.google.protobuf.ByteString
import ru.shadowsparky.screencast.extras.*
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_HEIGHT
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_NOTIFICATION_CHANNEL_NAME
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_NOTIFICATION_ID
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_PORT
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_WIDTH
import ru.shadowsparky.screencast.interfaces.Actionable
import ru.shadowsparky.screencast.interfaces.Sendeable
import ru.shadowsparky.screencast.proto.HandledPictureOuterClass
import ru.shadowsparky.screencast.proto.PreparingDataOuterClass
import java.io.Closeable
import java.net.*

abstract class ServerBase : Service(), Sendeable, Closeable {
    enum class ConnectionResult {
        ADDRESS_ALREADY_IN_USE,
        WAITING_FOR_CONNECTION,
        TIMEOUT,
        UNEXPECTEDLY_DISCONNECTED,
        ESTABLISHED,
        BROKEN
    }
    val DISMISS = "DISMISS"
    protected abstract val TAG: String
    protected lateinit var mData: Intent
    private lateinit var mProjectionManager: MediaProjectionManager
    private var mProjection: MediaProjection? = null
    protected var mServer: ServerSocket? = null
    protected var mClient: Socket? = null
    private var width = DEFAULT_WIDTH
    private var height = DEFAULT_HEIGHT
    private var mSurface: Surface? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mDisplay: Display? = null
    private var mCodec: MediaCodec? = null
    private var mFormat: MediaFormat? = null
    private var mCallback: ProjectionCallback? = null
    protected val log: Logger = Injection.provideLogger()
    private val mUtils: Utils = Injection.provideUtils()
    private lateinit var mNotification: Notification
    private lateinit var mShared: SharedUtils
    private lateinit var mSettingsParser: SettingsParser
    private var mEncoderThread = HandlerThread("EncoderThread", Process.THREAD_PRIORITY_URGENT_DISPLAY)
    var action: Actionable? = null
    var handling: Boolean = false
    protected val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            this@ServerBase.action?.invoke(ConnectionResult.UNEXPECTEDLY_DISCONNECTED)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log.printDebug("Command started: ${intent.hashCode()}", TAG)
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log.printDebug("Sever Base Created", TAG)
        mShared = Injection.provideSharedUtils(baseContext)
        mSettingsParser = Injection.provideSettingsParser(baseContext)
        mProjectionManager = baseContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        registerReceiver(receiver, IntentFilter(DISMISS))
        mEncoderThread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    open fun createServer() : Boolean {
        try {
            mServer = ServerSocket(DEFAULT_PORT, 1)
            mServer?.soTimeout = mSettingsParser.getWaiting()
        } catch (e: BindException) {
            action?.invoke(ConnectionResult.ADDRESS_ALREADY_IN_USE)
            return false
        }
        action?.invoke(ConnectionResult.WAITING_FOR_CONNECTION)
        return true
    }

    open fun accept() : Boolean {
        try {
            mClient = mServer?.accept()
            mClient?.tcpNoDelay = true
            handling = true
        } catch (e: SocketTimeoutException) {
            action?.invoke(ConnectionResult.TIMEOUT)
            return false
        } catch (e: SocketException) {
            action?.invoke(ConnectionResult.UNEXPECTEDLY_DISCONNECTED)
            return false
        }
        action?.invoke(ConnectionResult.ESTABLISHED)
        return true
    }

    protected open fun createNotification() : Notification {
        val dismissIntent = Intent(DISMISS)
        val dismissPI = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notificationService = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotification = Notifications(baseContext, dismissPI).provideNotification(notificationService)
        startForeground(DEFAULT_NOTIFICATION_ID, mNotification)
        return mNotification
    }

    protected open fun updateDisplayInfo() {
        val size = Point()
        mUtils.overrideGetSize(mDisplay!!, size)
        width = size.x
        height = size.y
    }

    protected open fun configureMediaFormat() {
        mFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        mFormat!!.setInteger(MediaFormat.KEY_BIT_RATE,mSettingsParser.getBitrate())
        mFormat!!.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mFormat!!.setFloat(MediaFormat.KEY_FRAME_RATE, mSettingsParser.getFramerate().toFloat())
        mFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }

    protected open fun configureMediaCodec() {
        mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mCallback = ProjectionCallback(this, mCodec!!)
        mCodec!!.setCallback(mCallback, Handler(mEncoderThread.looper))
        mCodec!!.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    open fun setupProjection() {
        mProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mData)
        mDisplay = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        updateDisplayInfo()
        sendPreparingData()
        configureMediaFormat()
        configureMediaCodec()
        mSurface = mCodec!!.createInputSurface()
    }

    open fun start() {
        mCodec!!.start()
        mCallback!!.handling = true
        mVirtualDisplay = mProjection!!.createVirtualDisplay(Constants.DEFAULT_PROJECTION_NAME, width, height, Constants.DEFAULT_DPI, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, Handler(mEncoderThread.looper))
    }

    open fun stop() = release()

    open fun release() {
        mCodec?.release()
        mVirtualDisplay?.release()
        mSurface?.release()
        mCodec?.release()
        mProjection?.stop()
        mCallback?.handling = false
    }

    override fun close() {
        log.printDebug("Close invoke")
        val notificationService = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mServer?.isClosed == false)
            mServer?.close()
        if (mClient?.isClosed == false)
            mClient?.close()
        release()
        stopForeground(true)
        handling = false
    }

    override fun sendPicture(picture: ByteArray) {
        try {
            val item = HandledPictureOuterClass.HandledPicture.newBuilder()
                    .setEncodedPicture(ByteString.copyFrom(picture))
                    .build()
            item.writeDelimitedTo(mClient?.getOutputStream())
        } catch (e: SocketException) {
            action?.invoke(ConnectionResult.BROKEN)
        }
    }

    override fun sendPreparingData() {
        try {
            val data = PreparingDataOuterClass.PreparingData.newBuilder()
                    .setWidth(width)
                    .setHeight(height)
                    .setPassword("")
                    .build()
            data.writeDelimitedTo(mClient?.getOutputStream())
        } catch (e: SocketException) {
            action?.invoke(ConnectionResult.BROKEN)
        }
    }
}