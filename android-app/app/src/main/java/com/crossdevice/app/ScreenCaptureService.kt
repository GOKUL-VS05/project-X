package com.crossdevice.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjection.Callback
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.ScreenCapturerAndroid
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var eglBase: EglBase? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var videoSource: VideoSource? = null
    private var videoTrack: VideoTrack? = null
    private var screenCapturer: ScreenCapturerAndroid? = null
    private var peerConnection: PeerConnection? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())

        val code = intent?.getIntExtra(MainActivity.EXTRA_RESULT_CODE, -1) ?: -1
        val data = extractCaptureData(intent)

        if (code == -1 || data == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = manager.getMediaProjection(code, data)

        initializePeerConnectionFactory()
        initializeSocketSignaling()
        startScreenCapture(data)
        createPeerConnection()
        createAndSendOffer()

        return START_STICKY
    }

    private fun initializePeerConnectionFactory() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions()
        )

        eglBase = EglBase.create()
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
        videoSource = peerConnectionFactory?.createVideoSource(false)
        surfaceTextureHelper =
            SurfaceTextureHelper.create("CaptureThread", eglBase?.eglBaseContext)
    }

    private fun initializeSocketSignaling() {
        SocketManager.connect(
            getString(R.string.server_url),
            "mobile-user",
            null,
            { answer ->
                val sessionDescription = SessionDescription(SessionDescription.Type.ANSWER, answer)
                peerConnection?.setRemoteDescription(SimpleSdpObserver(), sessionDescription)
            },
            { candidate ->
                val candidateJson = JSONObject(candidate)
                val iceCandidate = IceCandidate(
                    candidateJson.getString("sdpMid"),
                    candidateJson.getInt("sdpMLineIndex"),
                    candidateJson.getString("candidate")
                )
                peerConnection?.addIceCandidate(iceCandidate)
            },
            {}
        )
    }

    private fun startScreenCapture(data: Intent) {
        screenCapturer = ScreenCapturerAndroid(data, object : Callback() {})
        screenCapturer?.initialize(
            surfaceTextureHelper,
            this,
            videoSource?.capturerObserver
        )
        screenCapturer?.startCapture(WIDTH, HEIGHT, FPS)
        videoTrack = peerConnectionFactory?.createVideoTrack("VIDEO", videoSource)
    }

    private fun createPeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        peerConnection = peerConnectionFactory?.createPeerConnection(
            iceServers,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    SocketManager.socket.emit(
                        "candidate",
                        JSONObject()
                            .put("to", "desktop-user")
                            .put(
                                "candidate",
                                JSONObject()
                                    .put("sdpMid", candidate.sdpMid)
                                    .put("sdpMLineIndex", candidate.sdpMLineIndex)
                                    .put("candidate", candidate.sdp)
                            )
                    )
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState?) = Unit

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) = Unit

                override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit

                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) = Unit

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) = Unit

                override fun onAddStream(stream: MediaStream?) = Unit

                override fun onRemoveStream(stream: MediaStream?) = Unit

                override fun onDataChannel(channel: DataChannel?) = Unit

                override fun onRenegotiationNeeded() = Unit

                override fun onAddTrack(
                    receiver: RtpReceiver?,
                    mediaStreams: Array<out MediaStream>?
                ) = Unit
            }
        )

        videoTrack?.let { track ->
            peerConnection?.addTrack(track)
        }
    }

    private fun createAndSendOffer() {
        peerConnection?.createOffer(
            object : SdpObserver {
                override fun onCreateSuccess(desc: SessionDescription) {
                    peerConnection?.setLocalDescription(SimpleSdpObserver(), desc)
                    SocketManager.socket.emit(
                        "offer",
                        JSONObject()
                            .put("to", "desktop-user")
                            .put("offer", desc.description)
                    )
                }

                override fun onSetSuccess() = Unit

                override fun onCreateFailure(error: String?) = Unit

                override fun onSetFailure(error: String?) = Unit
            },
            MediaConstraints()
        )
    }

    private fun extractCaptureData(intent: Intent?): Intent? {
        if (intent == null) {
            return null
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(MainActivity.EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(MainActivity.EXTRA_RESULT_DATA)
        }
    }

    private fun createNotification(): Notification {
        val channelId = "capture_channel"
        val manager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "Capture",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        return Notification.Builder(this, channelId)
            .setContentTitle("Screen Sharing Active")
            .setContentText("Connected to ${getString(R.string.server_url)}")
            .setSmallIcon(android.R.drawable.presence_video_online)
            .build()
    }

    override fun onDestroy() {
        runCatching { screenCapturer?.stopCapture() }
        screenCapturer?.dispose()
        videoSource?.dispose()
        surfaceTextureHelper?.dispose()
        peerConnection?.dispose()
        peerConnectionFactory?.dispose()
        eglBase?.release()
        mediaProjection?.stop()
        SocketManager.disconnect()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val WIDTH = 720
        private const val HEIGHT = 1280
        private const val FPS = 30
    }
}

private open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(sessionDescription: SessionDescription?) = Unit

    override fun onSetSuccess() = Unit

    override fun onCreateFailure(error: String?) = Unit

    override fun onSetFailure(error: String?) = Unit
}
