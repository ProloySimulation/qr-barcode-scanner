package com.proloy.qrscanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anythink.core.api.ATSDK
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var resultTextView: AppCompatTextView
    private lateinit var surfaceView: SurfaceView
    private lateinit var cameraSource: CameraSource
    private lateinit var cardWebsite: CardView
    private lateinit var cardCopy: CardView
    private lateinit var webLink: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultTextView = findViewById(R.id.resultTextView)
        surfaceView = findViewById(R.id.surfaceView)
        cardWebsite = findViewById(R.id.btnWebsite)
        cardCopy = findViewById(R.id.btnCopy)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            createCameraSource()
        }

        cardWebsite.setOnClickListener {
            if(::webLink.isInitialized)
            {
                goToWeb(webLink)
            }
            else
            {
                Toast.makeText(this,"At First Scan Something",Toast.LENGTH_SHORT).show()
            }
        }

        cardCopy.setOnClickListener {
            if(::webLink.isInitialized)
            {
                copyToClipboard(webLink)
            }
            else
            {
                Toast.makeText(this,"At First Scan Something",Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize Audience Network Ads
        AudienceNetworkAds.initialize(this)

        // Initialize ATSDK (replace APP_ID and APP_KEY with your actual values)
        ATSDK.init(this, getString(R.string.app_id), getString(R.string.app_key))
    }

    private fun goToWeb(text: String) {
        val sendIntent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
        val chooser = Intent.createChooser(sendIntent, "Choose Your Browser")
        if (sendIntent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun createCameraSource() {
        val barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(com.google.android.gms.vision.barcode.Barcode.ALL_FORMATS)
            .build()

        if (!barcodeDetector.isOperational) {
            // Handle error
        }

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setAutoFocusEnabled(true)
            .build()

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            android.Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraSource.start(holder)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() > 0) {
                    val value = barcodes.valueAt(0).displayValue
                    handleScanResult(value)
                }
            }
        })
    }

    private fun handleScanResult(contents: String) {
        runOnUiThread {
            resultTextView.text = contents
            webLink = contents
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                createCameraSource()
            } else {
                // Handle permission denied
            }
        }
    }

/*    override fun onDestroy() {
        super.onDestroy()
        cameraSource.release()
    }*/

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
}