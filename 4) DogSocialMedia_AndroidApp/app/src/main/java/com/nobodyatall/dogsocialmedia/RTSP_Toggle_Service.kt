package com.nobodyatall.dogsocialmedia

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.pedro.rtspserver.RtspServerCamera1
import android.R

import android.app.Activity
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.*
import android.view.MotionEvent

import android.view.WindowManager

import android.view.ViewGroup

import android.view.Gravity
import android.widget.*


class RTSP_Toggle_Service : Service(), SurfaceHolder.Callback, ConnectCheckerRtsp {

    val TAG = "RTSPToggleService"
    private lateinit var rtspCamera: RtspServerCamera1
    var wm: WindowManager? = null
    var ll: LinearLayout? = null


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate(){
        ShowLog("onCreate")
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        ll = LinearLayout(this)
        var llParameters: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        ll!!.setBackgroundColor(Color.argb(66,255,0,0))
        ll!!.setLayoutParams(llParameters)

        var parameters :WindowManager.LayoutParams = WindowManager.LayoutParams(400,150, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)
        parameters.gravity = Gravity.CENTER or Gravity.CENTER
        parameters.x = 0
        parameters.y = 0

        //add surfaceview into floating windows
        val surfaceView = SurfaceView(this)
        val surfaceViewParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        surfaceView.setLayoutParams(surfaceViewParams)

        ll!!.addView(surfaceView)
        wm!!.addView(ll, parameters)

        ll!!.setOnTouchListener(object : View.OnTouchListener {
            var updatedParameters = parameters
            var x = 0.0
            var y = 0.0
            var pressedX = 0.0
            var pressedY = 0.0

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = updatedParameters.x.toDouble()
                        y = updatedParameters.y.toDouble()
                        pressedX = event.rawX.toDouble()
                        pressedY = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        updatedParameters.x = (x + (event.rawX - pressedX)).toInt()
                        updatedParameters.y = (y + (event.rawY - pressedY)).toInt()
                        wm!!.updateViewLayout(ll, updatedParameters)
                    }
                    else -> {
                    }
                }
                return false
            }
        })

        //start rtsp server
        rtspCamera = RtspServerCamera1(surfaceView, this, 1935)
        super.onCreate()
    }

    override fun onDestroy() {
        ShowLog("onDestroy")

        if (rtspCamera.isStreaming) {
            //statusTF.text = Editable.Factory.getInstance().newEditable("Stream Stopped")
            //urlTF.text = Editable.Factory.getInstance().newEditable("")

            rtspCamera.stopStream()
            rtspCamera.stopPreview()
        }

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ShowLog("onStartCommand")


            //val surfaceView: SurfaceView = findViewById(R.id.cameraPreview_serverStatus)

            //val statusTF: TextView = findViewById(R.id.statusTF_serverStatus)
            //val urlTF: EditText = findViewById(R.id.urlTF_serverStatus)
            ShowLog("Im here")
            if (rtspCamera.isRecording || rtspCamera.prepareAudio() && rtspCamera.prepareVideo()) {
                //statusTF.text = Editable.Factory.getInstance().newEditable("Stream on live")
                ShowLog("Im here starting stream")
                rtspCamera.startStream()
                ShowLog(rtspCamera.getEndPointConnection())


                //urlTF.text = Editable.Factory.getInstance().newEditable(rtspCamera.getEndPointConnection())
            } else {
                ShowLog("Error preparing stream, This device cant do it")
            }

        return super.onStartCommand(intent, flags, startId)
    }

    fun ShowLog(msg: String){
        Log.d(TAG, msg)
    }

    //RTSP Library functions
    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        rtspCamera.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    override fun onAuthErrorRtsp() {
        ShowLog("Auth Error")
    }

    override fun onAuthSuccessRtsp() {
        ShowLog("Auth Success")
    }

    override fun onConnectionFailedRtsp(reason: String) {
        ShowLog("Connection Failed")
    }

    override fun onConnectionStartedRtsp(rtspUrl: String) {

    }

    override fun onConnectionSuccessRtsp() {
        ShowLog("Connection Success")
    }

    override fun onDisconnectRtsp() {
        ShowLog("Disconnected")
    }

    override fun onNewBitrateRtsp(bitrate: Long) {

    }

}