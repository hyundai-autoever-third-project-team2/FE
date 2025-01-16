package com.twomuchcar.usedcar.utils

import android.app.Activity
import android.webkit.JavascriptInterface
import com.twomuchcar.usedcar.utils.interfaces.android.CarmeraInterface

class AndroidInterface(private val activity: Activity) {
    private val CarmeraInterface = CarmeraInterface(activity)

    @JavascriptInterface
    fun openCamera() {
        CarmeraInterface.openCamera()
    }


}
