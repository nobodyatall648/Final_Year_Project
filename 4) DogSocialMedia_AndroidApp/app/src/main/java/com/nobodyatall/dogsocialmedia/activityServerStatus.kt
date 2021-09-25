package com.nobodyatall.dogsocialmedia

import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.snackbar.Snackbar
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.pedro.rtspserver.RtspServerCamera1
import kotlinx.coroutines.runBlocking
import org.json.JSONObject


class activityServerStatus : AppCompatActivity(), SurfaceHolder.Callback, ConnectCheckerRtsp {


    private lateinit var rtspCamera: RtspServerCamera1
    var wm: WindowManager? = null
    var ll: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_status)

        var actionBar = getSupportActionBar()
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        //get from prev interface
        val LOGIN_USER: String? = intent.getStringExtra("USER")

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        ll = LinearLayout(this)
        var llParameters: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        ll!!.setBackgroundColor(Color.argb(66,255,0,0))
        ll!!.setLayoutParams(llParameters)

        var parameters :WindowManager.LayoutParams = WindowManager.LayoutParams(400,400, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)
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

        rtspCamera = RtspServerCamera1(surfaceView, this, 1935)

        //check RTSP server status
        checkRTSPServerInit()

        val serverToggleBtn: Button = findViewById(R.id.serverToggleBtn_serverStatus)
        serverToggleBtn.setOnClickListener{

                val statusTF: TextView = findViewById(R.id.statusTF_serverStatus)
                val urlTF: EditText = findViewById(R.id.urlTF_serverStatus)

                if (rtspCamera.isStreaming) {
                    statusTF.text = Editable.Factory.getInstance().newEditable("Stream Stopped")
                    urlTF.text = Editable.Factory.getInstance().newEditable("")

                    rtspCamera.stopStream()
                    rtspCamera.stopPreview()
                } else {
                    if (rtspCamera.isRecording || rtspCamera.prepareAudio(64 * 1024, 32000, true, true, true) && rtspCamera.prepareVideo()) {
                        statusTF.text = Editable.Factory.getInstance().newEditable("Stream on live")
                        rtspCamera.startStream()

                        urlTF.text = Editable.Factory.getInstance().newEditable(rtspCamera.getEndPointConnection())
                    } else {
                        Toast.makeText(
                            this, "Error preparing stream, This device cant do it",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }


        }

        val swapcamBtn: Button = findViewById(R.id.swapCamBtn_serverStatus)
        swapcamBtn.setOnClickListener{

            rtspCamera.switchCamera()
        }
    }

    private fun checkRTSPServerInit(){
        val statusTF: TextView = findViewById(R.id.statusTF_serverStatus)
        val urlTF: EditText = findViewById(R.id.urlTF_serverStatus)

        if(rtspCamera.isStreaming){
            statusTF.text = Editable.Factory.getInstance().newEditable("Stream on live")
            urlTF.text = Editable.Factory.getInstance().newEditable(rtspCamera.getEndPointConnection())
        }else{
            statusTF.text = Editable.Factory.getInstance().newEditable("Stream Stopped")
            urlTF.text = Editable.Factory.getInstance().newEditable("")
        }
    }

    private fun snackbarPopup(msg: String){
        val snackBar = Snackbar.make(
            this.findViewById(R.id.serverToggleBtn_serverStatus), msg,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackBar.show()
    }

    //preserving previous activity variable values
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            android.R.id.home -> {
                this@activityServerStatus?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
        runOnUiThread {
            Toast.makeText(this@activityServerStatus, "Auth error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthSuccessRtsp() {
        runOnUiThread {
            Toast.makeText(this@activityServerStatus, "Auth success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionFailedRtsp(reason: String) {
        runOnUiThread {
            Toast.makeText(this@activityServerStatus, "Connection failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionStartedRtsp(rtspUrl: String) {

    }

    override fun onConnectionSuccessRtsp() {
        runOnUiThread {
            Toast.makeText(this@activityServerStatus, "Connection success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisconnectRtsp() {
        runOnUiThread {
            Toast.makeText(this@activityServerStatus, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewBitrateRtsp(bitrate: Long) {

    }


}