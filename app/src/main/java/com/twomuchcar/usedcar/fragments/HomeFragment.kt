package com.twomuchcar.usedcar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.twomuchcar.usedcar.R

class HomeFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        webView = rootView.findViewById(R.id.webView)
        setupWebView()

        return rootView
    }

    private fun setupWebView() {
        webView.webViewClient = WebViewClient() // 기본 WebViewClient 사용
        webView.webChromeClient = WebChromeClient() // WebChromeClient 설정 (옵션)
        webView.settings.apply {
            javaScriptEnabled = true // JavaScript 사용 가능하도록 설정
            domStorageEnabled = true // DOM 저장소 활성화
            useWideViewPort = true // HTML 콘텐츠가 화면 크기에 맞게 조정되도록 설정
            loadWithOverviewMode = true // 페이지 전체를 화면에 맞게 조정
        }

        webView.loadUrl("https://autoever.site") // 원하는 URL 설정
    }

    override fun onDestroyView() {
        webView.destroy() // WebView 리소스 정리
        super.onDestroyView()
    }
}