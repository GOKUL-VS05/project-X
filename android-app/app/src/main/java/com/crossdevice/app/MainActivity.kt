package com.crossdevice.app

import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var statusText: TextView
    private val screenCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data

            if (result.resultCode == RESULT_OK && data != null) {
                val intent = Intent(this, ScreenCaptureService::class.java).apply {
                    putExtra(EXTRA_RESULT_CODE, result.resultCode)
                    putExtra(EXTRA_RESULT_DATA, data)
                }

                startForegroundService(intent)
                statusText.text = getString(R.string.capture_status_started)
            } else {
                statusText.text = getString(R.string.capture_status_cancelled)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        statusText = findViewById(R.id.statusText)

        findViewById<Button>(R.id.captureButton).setOnClickListener {
            statusText.text = getString(R.string.capture_status_requesting)
            screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent())
        }

        findViewById<Button>(R.id.socketButton).setOnClickListener {
            SocketManager.connect(
                getString(R.string.server_url),
                "mobile-user",
                null,
                null,
                null
            ) { message ->
                runOnUiThread {
                    statusText.text = getString(R.string.capture_status_control, message)
                }
            }
            statusText.text = getString(
                R.string.capture_status_socket,
                getString(R.string.server_url)
            )
        }
    }

    companion object {
        const val EXTRA_RESULT_CODE = "code"
        const val EXTRA_RESULT_DATA = "data"
    }
}
