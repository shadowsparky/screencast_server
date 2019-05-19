/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.google.protobuf.ByteString
import ru.shadowsparky.screencast.extras.*
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_HEIGHT
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_PORT
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_WIDTH
import ru.shadowsparky.screencast.interfaces.Printeable
import ru.shadowsparky.screencast.interfaces.Sendeable
import ru.shadowsparky.screencast.proto.HandledPictureOuterClass
import ru.shadowsparky.screencast.proto.PreparingDataOuterClass
import java.io.Closeable
import java.net.*

abstract class ServerBase : Service(), Sendeable, Closeable {
    protected abstract val TAG: String
    protected lateinit var mData: Intent
    protected lateinit var mProjectionManager: MediaProjectionManager
    protected var mProjection: MediaProjection? = null
    protected var mServer: ServerSocket? = null
    protected var mClient: Socket? = null
    protected var width = DEFAULT_WIDTH
    protected var height = DEFAULT_HEIGHT
    protected var mSurface: Surface? = null
    protected var mVirtualDisplay: VirtualDisplay? = null
    protected var mDisplay: Display? = null
    protected var mCodec: MediaCodec? = null
    protected var mFormat: MediaFormat? = null
    protected var mCallback: ProjectionCallback? = null
    protected val log: Logger = Injection.provideLogger()
    protected val mUtils: Utils = Injection.provideUtils()
    protected lateinit var mNotification: Notification
    protected lateinit var mShared: SharedUtils
    protected lateinit var mSettingsParser: SettingsParser
    protected var mEncoderThread = HandlerThread("EncoderThread", Process.THREAD_PRIORITY_URGENT_DISPLAY)
    var printeable: Printeable? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log.printDebug("Sever Base Created")
        mShared = Injection.provideSharedUtils(baseContext)
        mSettingsParser = Injection.provideSettingsParser(baseContext)
        mProjectionManager = baseContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mEncoderThread.start()
    }

    open fun createServer() : Boolean {
        try {
            mServer = ServerSocket(DEFAULT_PORT, 1)
            mServer?.soTimeout = mSettingsParser.getWaiting()
        } catch (e: BindException) {
            printeable?.print("Данный адрес уже используется")
            return false
        }
        printeable?.print("Ожидание подключения...")
        return true
    }

    open fun accept() : Boolean {
        try {
            mClient = mServer?.accept()
            mClient?.tcpNoDelay = true
        } catch (e: SocketTimeoutException) {
            printeable?.print("Превышено время ожидания")
            return false
        } catch (e: SocketException) {
            printeable?.print("Соединение неожиданно прервалось")
            return false
        }
        printeable?.print("Соединение с пользователем установлено")
        return true
    }

    open fun createNotification() {
        val notificationService = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotification = Notifications(baseContext).provideNotification(notificationService)
        startForeground(Constants.DEFAULT_NOTIFICATION_ID, mNotification)
    }

    open fun updateDisplayInfo() {
        val size = Point()
        mUtils.overrideGetSize(mDisplay!!, size)
        width = size.x
        height = size.y
    }

    open fun setupProjection() {
        mProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mData)
        mDisplay = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        updateDisplayInfo()
        sendPreparingData()
        mFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        mFormat!!.setInteger(MediaFormat.KEY_BIT_RATE,mSettingsParser.getBitrate())
        mFormat!!.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mFormat!!.setFloat(MediaFormat.KEY_FRAME_RATE, mSettingsParser.getFramerate().toFloat())
        mFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mCallback = ProjectionCallback(this, mCodec!!)
        mCodec!!.setCallback(mCallback, Handler(mEncoderThread.looper))
        mCodec!!.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mSurface = mCodec!!.createInputSurface()
    }

    open fun start() {
        mCodec!!.start()
        mCallback?.handling = true
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
        if (mServer?.isClosed == false)
            mServer?.close()
        if (mClient?.isClosed == false)
            mClient?.close()
        release()
        printeable?.print("Соединение с сервером было разорвано")
    }

    override fun sendPicture(picture: ByteArray) {
        try {
            val item = HandledPictureOuterClass.HandledPicture.newBuilder()
                    .setEncodedPicture(ByteString.copyFrom(picture))
                    .build()
            item.writeDelimitedTo(mClient?.getOutputStream())
        } catch (e: SocketException) {
            printeable?.print("Соединение с сервером было разорвано")
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
            printeable?.print("Соединение с сервером было разорвано")
        }
    }
}