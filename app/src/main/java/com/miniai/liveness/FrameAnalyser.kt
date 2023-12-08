/*
 * Copyright 2021 Shubham Panchal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.miniai.liveness

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.fm.face.FaceBox
import com.fm.face.FaceSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// Analyser class to process frames and produce detections.
class FrameAnalyser( private var context: Context ,
                     private var boundingBoxOverlay: BoundingBoxOverlay,
                     private var viewBackgroundOfMessage: View,
                     private var textViewMessage: TextView
                     ) : ImageAnalysis.Analyzer {

    companion object {
        private val TAG = FrameAnalyser::class.simpleName
        const val LIVENESS_THRESHOLD = 0.5f
    }

    enum class PROC_MODE {
        VERIFY, REGISTER
    }

    var mode = PROC_MODE.VERIFY
    var startVerifyTime: Long = 0

    private var isRunning = false
    private var isProcessing = false
    private var isRegistering = false
    private var frameInterface: FrameInferface? = null
    fun cancelRegister() {
        mode = PROC_MODE.VERIFY
        isRegistering = false
    }

    fun setRunning(running: Boolean) {
        isRunning = running

        viewBackgroundOfMessage.alpha = 0f
        textViewMessage.alpha = 0f
        boundingBoxOverlay.faceBoundingBoxes = null
        boundingBoxOverlay.invalidate()
    }
    fun addOnFrameListener(frameInterface: FrameInferface) {
        this.frameInterface = frameInterface
    }
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {

        if(!isRunning) {
            boundingBoxOverlay.faceBoundingBoxes = null
            boundingBoxOverlay.invalidate()
            image.close()
            return
        }

        if (isProcessing) {
            image.close()
            return
        }
        else {
            isProcessing = true

            // Rotated bitmap for the FaceNet model
            val frameBitmap = BitmapUtils.imageToBitmap( image.image!! , image.imageInfo.rotationDegrees )

            // Configure frameHeight and frameWidth for output2overlay transformation matrix.
            if ( !boundingBoxOverlay.areDimsInit ) {
                boundingBoxOverlay.frameHeight = frameBitmap.height
                boundingBoxOverlay.frameWidth = frameBitmap.width
            }

            var livenessScore = 0.0f;
            var faceResult: List<FaceBox>? = FaceSDK.getInstance().detectFace(frameBitmap)
            if(!faceResult.isNullOrEmpty()) {
                if (faceResult!!.size == 1) {
                    hideMessage()
                    livenessScore =
                        FaceSDK.getInstance().checkLiveness(frameBitmap, faceResult!!.get(0))
                    Log.i("liveness score : ", livenessScore.toString())

                    if (livenessScore > LIVENESS_THRESHOLD) {
                        boundingBoxOverlay.livenessResult = 1
                    } else {
                        boundingBoxOverlay.livenessResult = 0
                        hideMessage()
                    }
                } else {
                    boundingBoxOverlay.livenessResult = 2
                    showMessage(context.getString(R.string.multiple_face_detected))
                }
            }

            CoroutineScope( Dispatchers.Default ).launch {
                withContext( Dispatchers.Main ) {
                    // Clear the BoundingBoxOverlay and set the new results ( boxes ) to be displayed.
                    boundingBoxOverlay.faceBoundingBoxes = faceResult
                    boundingBoxOverlay.livenessScore = livenessScore
                    boundingBoxOverlay.invalidate()
                }
            }

            isProcessing = false
            image.close()
        }
    }

    private fun showMessage(msg: String) {
        CoroutineScope( Dispatchers.Default ).launch {
            withContext( Dispatchers.Main ) {
                textViewMessage.text = msg
                viewBackgroundOfMessage.alpha = 1.0f
                textViewMessage.alpha = 1.0f
            }
        }
    }

    private fun hideMessage() {
        CoroutineScope( Dispatchers.Default ).launch {
            withContext( Dispatchers.Main ) {
                viewBackgroundOfMessage.alpha = 0.0f
                textViewMessage.alpha = 0.0f

            }
        }
    }
}