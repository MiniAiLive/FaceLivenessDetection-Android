package com.miniai.liveness

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.fm.face.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UserActivity : AppCompatActivity() {

    companion object {
        private val TAG = UserActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, CameraActivity::class.java))

        FaceSDK.createInstance(this)
        val ret = FaceSDK.getInstance().init(assets)
        if(ret != FaceSDK.SDK_SUCCESS) {
            Log.i("SYSTEM Log : ", ret.toString())
        }
    }
    override fun onResume() {
        super.onResume()
    }
}
