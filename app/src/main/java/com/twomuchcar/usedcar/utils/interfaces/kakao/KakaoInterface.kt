package com.twomuchcar.usedcar.utils.interfaces.kakao

import android.content.Context
import android.webkit.JavascriptInterface

class KakaoInterface (context: Context){

    private val LoginInterface = LoginInterface(context)

    // 카카오 소셜 로그인 인터페이스
    @JavascriptInterface
    fun kakaoLogin() {
        LoginInterface.kakaoLogin()
    }

    // TODO 카카오페이 인터페이스
}
