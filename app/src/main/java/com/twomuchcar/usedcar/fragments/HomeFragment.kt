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
import com.twomuchcar.usedcar.utils.interfaces.kakao.KakaoInterface

class HomeFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        webView = rootView.findViewById(R.id.webView)

        return rootView
    }

    override fun onDestroyView() {
        webView.destroy() // WebView 리소스 정리
        super.onDestroyView()
    }
}


