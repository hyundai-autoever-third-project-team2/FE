package com.twomuchcar.usedcar.utils.interfaces.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast

class CarmeraInterface(private val activity: Activity) {
    private val CAMERA_PERMISSION_CODE = 100
    private val REQUEST_IMAGE_CAPTURE = 1

    // 카메라 권한을 체크하고 카메라를 실행하는 메소드
    @JavascriptInterface
    fun openCamera() {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            activity.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    // 카메라 실행
    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    // 권한 요청 결과 처리
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(activity, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 촬영된 이미지를 처리하는 메소드 (onActivityResult에서 호출)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            // 이미지 처리 로직 (웹뷰로 보내거나 저장)
            Log.i("AndroidInterface", "촬영된 이미지 처리")
        }
    }
}
