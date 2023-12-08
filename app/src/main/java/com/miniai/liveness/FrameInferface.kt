package com.miniai.liveness

import android.graphics.Bitmap

interface FrameInferface {
    fun onRegister(faceImage: Bitmap, featData: ByteArray?)
    fun onVerify(msg: String)
}