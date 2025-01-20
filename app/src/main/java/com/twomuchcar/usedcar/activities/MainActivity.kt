package com.twomuchcar.usedcar.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.twomuchcar.usedcar.R
import com.twomuchcar.usedcar.service.ApiClient
import com.twomuchcar.usedcar.service.ApiService
import com.twomuchcar.usedcar.service.TokenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_CODE = 100
    private val REQUEST_IMAGE_CAPTURE = 1
    private val PICK_IMAGE_REQUEST = 486
    private var currentCameraIndex = 0
    private lateinit var webView: WebView

    private var backPressedOnce = false
    private val handler = Handler(Looper.getMainLooper())
    private val homePageUrl = "https://autoever.site/"

    // WebView와의 통신 인터페이스 클래스입니다.
    inner class WebAppInterface(private val context: Context) {
        @JavascriptInterface
        fun openCamera(index: Int) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                currentCameraIndex = index;
                startCamera()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }

        @JavascriptInterface
        fun openGallery() {
            runOnUiThread {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }
        }
    }

    // 카메라를 실행합니다.
    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    // 권한 허용 요청에 대한 결과를 반환합니다.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupWebView()
        setupFCM()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    if (webView.url == homePageUrl) { // 현재 페이지가 홈 화면인지 확인
                        handleHomeBackPressed()
                    } else {
                        webView.goBack() // 이전 페이지로 이동
                    }
                } else {
                    handleHomeBackPressed() // 더 이상 뒤로 갈 페이지가 없는 경우 처리
                }
            }
        })
    }


    private fun handleHomeBackPressed() {
        if (backPressedOnce) {
            finish() // 앱 종료
        } else {
            backPressedOnce = true
            Toast.makeText(this, "한번 더 누르면 앱이 종료됩니다", Toast.LENGTH_SHORT).show()
            handler.postDelayed({ backPressedOnce = false }, 2000) // 2초 후 상태 초기화
        }
    }

    private fun setupFCM(){
        // 알림 허용 수락
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        // FCM 토큰 발급 및 서버로 전송
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "Token: ${task.result}")
                Log.d("FCM", "Token: $token") // Logcat에서 확인 가능
                sendTokenToServer(token)
            } else {
                Log.e("FCM", "Token error: ${task.exception}")
            }
        }
    }

    // 서버로 FCM 토큰 전송
    private fun sendTokenToServer(token: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.api.updateFcmToken(TokenRequest(token))
                if (!response.isSuccessful) {
                    Log.e("FCM", "Failed to send token: ${response.message()}")
                } else {
                    Log.d("FCM", "Token sent successfully")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error sending token", e)
            }
        }
    }

    // 웹뷰를 세팅합니다.
    private fun setupWebView() {
        WebView.setWebContentsDebuggingEnabled(true)  // 디버깅 활성화

        webView = findViewById(R.id.webView)
        webView.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url == null) return false

                    try {
                        // intent:// 스킴 처리
                        if (url.startsWith("intent://")) {
                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            if (intent.getPackage() != null) {
                                val existPackage = packageManager.getLaunchIntentForPackage(intent.getPackage()!!)
                                if (existPackage != null) {
                                    startActivity(intent)
                                    return true
                                } else {
                                    val marketIntent = Intent(Intent.ACTION_VIEW)
                                    marketIntent.data = Uri.parse("market://details?id=${intent.getPackage()}")
                                    startActivity(marketIntent)
                                    return true
                                }
                            }
                        }

                        // 특수 스킴 처리
                        when {
                            url.startsWith("market://") -> {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                return true
                            }
                            url.startsWith("tel:") -> {
                                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                                return true
                            }
                        }

                        // 모든 http/https URL은 웹뷰에서 처리
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            view?.loadUrl(url)
                            return true
                        }

                        return false
                    } catch (e: Exception) {
                        Log.e("WebView", "URL 처리 중 오류 발생", e)
                        return false
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Log.d("WebView", "페이지 로딩 시작: $url")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("WebView", "페이지 로딩 완료: $url")
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    Log.e("WebView", "에러 발생: $errorCode - $description")
                }

            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {
                    Log.d("WebView Console", "$message -- From line $lineNumber of $sourceID")
                }
            }

            addJavascriptInterface(WebAppInterface(this@MainActivity), "Android")

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true

                cacheMode = WebSettings.LOAD_NO_CACHE
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                allowContentAccess = true
                allowFileAccess = true

                // SSL/HTTPS 관련 추가 설정
                databaseEnabled = true
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = true
            }


            loadUrl("https://autoever.site/login")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            // Bitmap을 Base64로 변환
            val outputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

            // JavaScript 문자열로 적절히 이스케이프하여 전송
            val javascriptString = """
                javascript:window.receiveImageFromCamera${currentCameraIndex}("data:image/jpeg;base64,${base64Image.replace("\\", "\\\\").replace("\"", "\\\"")}");
            """.trimIndent()

            // WebView로 이미지 전송
            webView.post {
                webView.loadUrl(javascriptString)
            }
        }
        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val imageUri = data?.data
            imageUri?.let { uri ->
                // S3에 이미지 업로드
                uploadImageToS3(uri)
            }
        }
    }
    private fun uploadImageToS3(imageUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val imageFile = getFileFromUri(imageUri)
                val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                // ResponseBody에서 문자열 추출
                val response = ApiClient.api.uploadImage(body)
                val imageUrl = response.toString().trim()  // 응답 본문을 문자열로 변환

                Log.d("Upload", "Raw response: $imageUrl")

                withContext(Dispatchers.Main) {
                    webView.evaluateJavascript(
                        "window.receiveImageFromGallery('$imageUrl')",
                        null
                    )
                }
            } catch (e: Exception) {
                Log.e("Upload", "Error during upload", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    webView.evaluateJavascript(
                        "console.error('Image upload failed: ${e.message}')",
                        null
                    )
                }
            }
        }
    }
    private fun getFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        inputStream.use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        return file
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        webView.destroy()
        super.onDestroy()
    }



}
