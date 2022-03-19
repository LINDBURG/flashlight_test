package com.linbug.flashlight

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.linbug.flashlight.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private var cameraManager: CameraManager? = null
    private var isFlashOn = false
    private var cameraId: String? = null
    private var binding: ActivityMainBinding? = null
    private var permissionGranted = false
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        permissionGranted = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding?.root)

        initFlashlight()
        initPermission()

        // button turn on off
        binding?.flashButton?.setOnClickListener {
            if (isFlashOn) {
                flashLightOff()
            } else {
                flashLightOn()
            }
        }
        
    }

    private fun initPermission() {
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Lindbug", "permission ok")
                isFlashOn = false
                cameraId?.let {
                    cameraManager?.setTorchMode(cameraId!!, false)
                }
                binding?.flashButton?.text = getString(R.string.turn_on_text)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Log.d("Lindbug", "message")
                Toast.makeText(this, "please give permission", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.d("Lindbug", "request")
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA)
            }
        }
    }


    private fun initFlashlight() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(
                applicationContext,
                "This device is not support camera flash.\n The app will be terminated!",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        if (cameraId == null && cameraManager?.cameraIdList?.isNotEmpty() == true) {
            for (id in cameraManager!!.cameraIdList) {
                val c: CameraCharacteristics = cameraManager!!.getCameraCharacteristics(id)
                val flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                val lensFacing = c.get(CameraCharacteristics.LENS_FACING)
                if (flashAvailable != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    break
                }
            }
            binding?.flashButton?.text = getString(R.string.turn_on_text)
        }

    }

    private fun flashLightOn() {
        if (!permissionGranted) {
            return
        }

        isFlashOn = true
        cameraId?.let {
            cameraManager?.setTorchMode(cameraId!!, true)
        }
        binding?.flashButton?.text = getString(R.string.turn_off_text)
    }

    private fun flashLightOff() {
        if (!permissionGranted) {
            return
        }

        isFlashOn = false
        cameraId?.let {
            cameraManager?.setTorchMode(cameraId!!, false)
        }
        binding?.flashButton?.text = getString(R.string.turn_on_text)
    }

}