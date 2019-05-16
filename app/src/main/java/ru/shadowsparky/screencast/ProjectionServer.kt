/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.shadowsparky.screencast.extras.*
import ru.shadowsparky.screencast.extras.Constants.ACTION
import ru.shadowsparky.screencast.extras.Constants.CONNECTION_CLOSED_CODE
import ru.shadowsparky.screencast.extras.Constants.CONNECTION_STARTED_CODE
import ru.shadowsparky.screencast.extras.Constants.DATA
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_DPI
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_HEIGHT
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_NOTIFICATION_ID
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_PORT
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_PROJECTION_NAME
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_WIDTH
import ru.shadowsparky.screencast.extras.Constants.DISABLE_PROJECTION_ACTION
import ru.shadowsparky.screencast.extras.Constants.DISABLE_PROJECTION_VALUE
import ru.shadowsparky.screencast.extras.Constants.NOTHING
import ru.shadowsparky.screencast.extras.Constants.REASON
import ru.shadowsparky.screencast.extras.Constants.RECEIVER_CODE
import ru.shadowsparky.screencast.extras.Constants.RECEIVER_DEFAULT_CODE
import ru.shadowsparky.screencast.proto.HandledPictureOuterClass
import ru.shadowsparky.screencast.proto.PreparingDataOuterClass
import java.io.IOException
import java.net.*

interface Writeable {
    fun write(array: ByteArray, flags: Int)
}

class ProjectionServer : Service(), Writeable {
    private lateinit var mData: Intent
    private lateinit var mProjectionManager: MediaProjectionManager
    private val TAG = "ProjectionServer"
    private var mProjection: MediaProjection? = null
    private var mServerSocket: ServerSocket? = null
    private var mClientSocket: Socket? = null
    private var width = DEFAULT_WIDTH
    private var height = DEFAULT_HEIGHT
    private var mSurface: Surface? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mDisplay: Display? = null
    private var mCodec: MediaCodec? = null
    private var mFormat: MediaFormat? = null
    private var mCallback: MediaCodec.Callback? = null
    private val log: Logger = Injection.provideLogger()
    private val mUtils: Utils = Injection.provideUtils()
    private var notification: Notification? = null
    private var reason = NOTHING
    private var broadcast: Intent? = null
    private lateinit var shared: SharedUtils
    private lateinit var settingsParser: SettingsParser
    private var encoderThread = HandlerThread("EncoderThread", Process.THREAD_PRIORITY_URGENT_DISPLAY)

    override fun onCreate() {
        super.onCreate()
        encoderThread.start()
    }

    private var handling: Boolean = false
        set(value) {
            if (value) {
                initBroadcast()
                broadcast!!.putExtra(RECEIVER_CODE, RECEIVER_DEFAULT_CODE)
                broadcast!!.putExtra(ACTION, CONNECTION_STARTED_CODE)
                sendBroadcast(broadcast!!)
                log.printDebug("Handling enabled")
            } else {
                initBroadcast()
                broadcast!!.putExtra(RECEIVER_CODE, RECEIVER_DEFAULT_CODE)
                broadcast!!.putExtra(ACTION, CONNECTION_CLOSED_CODE)
                broadcast!!.putExtra(REASON, reason)
                sendBroadcast(broadcast!!)
                reason = NOTHING
                log.printDebug("Handling disabled")
                socketSafeClosing()
                clientSocketSafeClosing()
                try {
                mCodec?.stop()
                } catch (e: IllegalStateException) {
                    // ignore
                }
                this.stopSelf()
            }
            field = value
        }

    private fun initBroadcast() {
        broadcast = Intent(Constants.BROADCAST_ACTION)
    }
    private fun socketSafeClosing() {
        try {
            if (mServerSocket != null) {
                if (!mServerSocket!!.isClosed) {
                    mServerSocket!!.close()
                }
            }
        } catch (e: SocketException) {
            log.printDebug("Socket exception in Socket Safe Closing")
        }
    }

    private fun clientSocketSafeClosing() {
        try {
            if (mClientSocket != null) {
                if (!mClientSocket!!.isClosed) {
                    mClientSocket!!.close()
                }
            }
        } catch (e: SocketException) {
            log.printDebug("Socket exception in Socket Safe Closing")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        shared = Injection.provideSharedUtils(this)
        settingsParser = Injection.provideSettingsParser(this)
        mData = intent!!.getParcelableExtra(DATA)
        mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startServer()
        return START_NOT_STICKY
    }

    private fun createNotification() {
        val intent = Intent(baseContext, CommunicationReceiver::class.java)
        intent.putExtra(DISABLE_PROJECTION_ACTION, DISABLE_PROJECTION_VALUE)
        val pi = PendingIntent.getBroadcast(baseContext, 1, intent, 0)
        val action = NotificationCompat.Action.Builder(0, "Остановить", pi)
                .build()
        val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification = Notifications(this, action).provideNotification(notificationService)
        startForeground(DEFAULT_NOTIFICATION_ID, notification)
    }

    private fun startServer() = GlobalScope.launch(Dispatchers.IO)  {
        try {
            mServerSocket = ServerSocket(DEFAULT_PORT, 1)
        } catch (e: BindException) {
            reason = "Данный адрес уже используется"
            handling = false
            return@launch
        }
        mServerSocket!!.soTimeout = settingsParser.getWaiting()
        log.printDebug("Waiting connection...", TAG)
        try {
            mClientSocket = mServerSocket!!.accept()
            mClientSocket!!.tcpNoDelay = true
        } catch (e: SocketTimeoutException) {
            reason = "Превышено время ожидания подключения"
            handling = false
            log.printDebug("SocketTimeoutException")
            return@launch
        } catch (e: SocketException) {
            reason = "Соединение неожиданно прервалось"
            handling = false
            return@launch
        }
        createNotification()
        log.printDebug("Connection accepted.", TAG)
        configureProjection()
        startProjection()
        sendProjectionInfo()
    }

    private fun sendProjectionInfo() {
        handling = true
        val data = PreparingDataOuterClass.PreparingData.newBuilder()
                .setWidth(width)
                .setHeight(height)
                .setPassword("")
                .build()
        data.writeDelimitedTo(mClientSocket?.getOutputStream())
    }

    override fun write(array: ByteArray, flags: Int) {
        try {
            val item = HandledPictureOuterClass.HandledPicture.newBuilder()
                    .setEncodedPicture(ByteString.copyFrom(array))
                    .setFlags(flags)
                    .build()
            item.writeDelimitedTo(mClientSocket?.getOutputStream())
            log.printError("Message sent ${item.flags}", TAG, true)
        } catch (e: InterruptedException) {
            log.printError("InterruptedException")
            handling = false
            return
        } catch (e: IOException) {
            handling = false
            log.printError("IOException")
            return
        }
    }


//    private fun sendProjectionData() {
//        try {
//            sendProjectionInfo()
//            handling = true
//            while (handling) {
//                val data = mSendingBuffers.take()
//                val item = HandledPictureOuterClass.HandledPicture.newBuilder()
//                        .setEncodedPicture(ByteString.copyFrom(data))
//                        .build()
//                item.writeDelimitedTo(mClientSocket?.getOutputStream())
//                log.printError("Message sent ${item.serializedSize}", TAG, true)
//            }
//        } catch (e: InterruptedException) {
//            log.printError("InterruptedException")
//            handling = false
//            return
//        } catch (e: IOException) {
//            handling = false
//            log.printError("IOException")
//            return
//        }
//    }

    private fun configureProjection() {
        mProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mData)
        mDisplay = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        updateDisplayInfo()
        mFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        mFormat!!.setInteger(MediaFormat.KEY_BIT_RATE, settingsParser.getBitrate())
        mFormat!!.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mFormat!!.setFloat(MediaFormat.KEY_FRAME_RATE, settingsParser.getFramerate().toFloat())
        mFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mCallback = ProjectionCallback(this, mCodec!!)
        mCodec!!.setCallback(mCallback, Handler(encoderThread.looper))
        mCodec!!.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mSurface = mCodec!!.createInputSurface()
    }

    private fun updateDisplayInfo() {
        val size = Point()
        mUtils.overrideGetSize(mDisplay!!, size)
        width = size.x
        height = size.y
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProjection()
        encoderThread.quit()
    }

    private fun stopProjection(disableHandling: Boolean = true) {
        mProjection?.stop()
        mSurface?.release()
        mVirtualDisplay?.release()
        if (disableHandling)
            handling = false
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        log.printDebug("Configuration changed")
        stopProjection(false)
        configureProjection()
        startProjection()
        log.printDebug("$width $height")
    }

    private fun startProjection() {
        mCodec!!.start()
        mVirtualDisplay = mProjection!!.createVirtualDisplay(DEFAULT_PROJECTION_NAME, width, height, DEFAULT_DPI, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, Handler(encoderThread.looper))
    }
}
