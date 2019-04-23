/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.view.*
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
import ru.shadowsparky.screencast.extras.Constants.NOTHING
import ru.shadowsparky.screencast.extras.Constants.REASON
import ru.shadowsparky.screencast.extras.Constants.RECEIVER_CODE
import ru.shadowsparky.screencast.extras.Constants.RECEIVER_DEFAULT_CODE
import ru.shadowsparky.screencast.proto.HandledPictureOuterClass
import ru.shadowsparky.screencast.proto.PreparingDataOuterClass
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.net.*

class ProjectionServer : Service() {
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
    private val mSendingBuffers = Injection.provideByteQueue()
    private val log: Logger = Injection.provideLogger()
    private val mUtils: Utils = Injection.provideUtils()
    private var notification: Notification? = null
    private var reason = NOTHING
    private var broadcast: Intent? = null
    private lateinit var shared: SharedUtils

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
                mCodec?.stop()
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
        mData = intent!!.getParcelableExtra(DATA)
        mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        createNotification()
        startServer()
        return START_NOT_STICKY
    }

    private fun createNotification() {
        val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification = Notifications(this).provideNotification(notificationService)
        startForeground(DEFAULT_NOTIFICATION_ID, notification)
    }

    private fun startServer() = GlobalScope.launch(Dispatchers.IO)  {
        try {
            mServerSocket = ServerSocket(DEFAULT_PORT)
        } catch (e: BindException) {
            reason = "Данный адрес уже используется"
            handling = false
            return@launch
        }
        mServerSocket!!.soTimeout = 30000
        log.printDebug("Waiting connection...", TAG)
        try {
            mClientSocket = mServerSocket!!.accept()
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
        log.printDebug("Connection accepted.", TAG)
        configureProjection()
        startProjection()
        sendProjectionData()
    }

    private fun sendProjectionInfo() {
        val data = PreparingDataOuterClass.PreparingData.newBuilder()
                .setWidth(width)
                .setHeight(height)
                .setPassword("")
                .build()
        data.writeDelimitedTo(mClientSocket?.getOutputStream())
    }


    private fun sendProjectionData() = GlobalScope.launch(Dispatchers.IO) {
        try {
            sendProjectionInfo()
            mClientSocket!!.tcpNoDelay = true
            handling = true
            while (handling) {
                val data = mSendingBuffers.take()
                val item = HandledPictureOuterClass.HandledPicture.newBuilder()
                        .setEncodedPicture(ByteString.copyFrom(data))
                        .build()
                item.writeDelimitedTo(mClientSocket?.getOutputStream())
            }
        } catch (e: InterruptedException) {
            log.printError("InterruptedException")
            handling = false
            return@launch
        } catch (e: IOException) {
            handling = false
            log.printError("IOException")
            return@launch
        }
    }.start()

    private fun configureProjection() {
        mProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mData)
        mDisplay = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        updateDisplayInfo()
        mFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        mFormat!!.setInteger(MediaFormat.KEY_BIT_RATE, 12000000)
        mFormat!!.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mFormat!!.setFloat(MediaFormat.KEY_FRAME_RATE, shared.getFramerate().toFloat())
        mFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mCallback = ProjectionCallback(mSendingBuffers, mCodec!!)
        mCodec!!.setCallback(mCallback)
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
    }

    private fun stopProjection() {
        mCodec?.stop()
        mProjection?.stop()
        mSurface?.release()
        mVirtualDisplay?.release()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        log.printDebug("Configuration changed")
        stopProjection()
        configureProjection()
        startProjection()
        log.printDebug("$width $height")
    }

    private fun startProjection() {
        mCodec!!.start()
        mVirtualDisplay = mProjection!!.createVirtualDisplay(DEFAULT_PROJECTION_NAME, width, height, DEFAULT_DPI, 0, mSurface, null, null)
    }
}
