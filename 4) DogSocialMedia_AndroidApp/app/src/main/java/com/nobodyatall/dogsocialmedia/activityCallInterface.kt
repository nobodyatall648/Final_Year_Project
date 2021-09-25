package com.nobodyatall.dogsocialmedia

import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.util.*
import android.graphics.Bitmap
import android.widget.Toast

import android.graphics.Bitmap.CompressFormat
import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.text.Editable
import android.util.Log
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.BlobDataPart
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.gson.responseObject
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.Thread.sleep


class activityCallInterface : AppCompatActivity() {
    private var RTSP_URL: String? = ""
    private var myTimer = Timer()
    private var timer = Timer()
    private var mRecorder: MediaRecorder? = null
    private var mFileName: String? = null
    private var pictureFilename: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_interface)

        RTSP_URL = intent.getStringExtra("RTSP_URL")
        val FRIEND_NAME: String? = intent.getStringExtra("FRIEND_NAME")
        val LOGIN_USER: String? = intent.getStringExtra("USER")

        val friendNameTF: TextView = findViewById(R.id.usernameLabel_callInt)
        friendNameTF.text = FRIEND_NAME

        //Play RTSP Live Stream
        var videoView: VideoView = findViewById(R.id.rtspVideo_callInt)
        videoView.setVideoURI(Uri.parse(RTSP_URL))
        var mediaController: MediaController = MediaController(this)
        videoView.setMediaController(mediaController)
        videoView.requestFocus()

        videoView.setOnPreparedListener(OnPreparedListener {
            // do something when video is ready to play
            snackbarPopup("[*] Dog Friend Online, Playing Video Feed...")
            videoView.start()
        })

        videoView.setOnErrorListener { mp, what, extra -> // do something when an error is occur during the video playback
            snackbarPopup("[*] No Video Feed. Dog Friend Aren't Online.")
            true
        }

        //predict schedule iteration part
        myTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val screenshotInd: TextView = findViewById(R.id.screenshotInd_callInt)
                val audioRecInd: TextView = findViewById(R.id.audioRecInd_callInt)
                val predictAPIInd: TextView = findViewById(R.id.predictAPIInd_callInt)

                //clear indicator texts
                runOnUiThread(Runnable(){
                    screenshotInd.text = "no"
                    audioRecInd.text = "no"
                    predictAPIInd.text = "no"
                })

                //screenshot videoView part
                //img saved in =>  <ext_storage>/dogSocialMediaTmp/img.jpg
                runOnUiThread(Runnable(){
                    screenshotInd.text = "pending"
                })

                var screenshotSaved: Boolean

                getBitMapFromSurfaceView(videoView) { bitmap: Bitmap? ->
                    val state: String = Environment.getExternalStorageState()
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        val picDir =
                            File(Environment.getExternalStorageDirectory().toString() + "/dogSocialMediaTmp")
                        if (!picDir.exists()) {
                            picDir.mkdir()
                        }

                        val fileName = "img_%s_%s_%d.jpg".format(LOGIN_USER, FRIEND_NAME, (1000 until 9999).random())
                        val picFile = File(picDir.toString() + "/" + fileName)
                        pictureFilename = picDir.toString() + "/" + fileName


                        try {
                            picFile.createNewFile()
                            val picOut = FileOutputStream(picFile)
                            screenshotSaved = bitmap!!.compress(CompressFormat.JPEG, 100, picOut)

                            //update screenshot indicator
                            if(screenshotSaved){
                                runOnUiThread(Runnable(){
                                    screenshotInd.text = "done"
                                })
                            }else{
                                runOnUiThread(Runnable(){
                                    screenshotInd.text = "error"
                                })
                            }

                            picOut.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                    }
                }

                //record audio 5 seconds

                mFileName =  Environment.getExternalStorageDirectory().toString()
                mFileName = mFileName + "/dogSocialMediaTmp" + "/audioRec_%s_%d.mp3".format(FRIEND_NAME, (1000 until 9999).random());
                mRecorder = MediaRecorder()
                mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
                mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                mRecorder?.setOutputFile(mFileName)

                runOnUiThread(Runnable(){
                    audioRecInd.text = "pending"
                })

                val timerTask: TimerTask = object : TimerTask() {
                    override fun run() {
                        try {
                            mRecorder?.stop()
                            mRecorder?.reset()
                            mRecorder?.release()
                            //upload image & audio to API server
                            uploadAudio(mFileName!!, pictureFilename!!)
                        }catch(e: Exception){
                            Log.i("[stop mediaRec timer]", e.message + "")
                        }
                    }
                }

                try {
                    mRecorder?.prepare()
                } catch (e: IOException) {
                    Log.e("AudioRecording", "prepare() failed")
                }
                mRecorder?.start()
                timer.schedule(timerTask, 5000) // Will stop recording after 5 seconds.

                runOnUiThread(Runnable(){
                    audioRecInd.text = "done"
                })
            }
        }, 15000, 15000) //1st run delay 20sec, each iter 10sec delay

        val endCallBtn: Button = findViewById(R.id.endCallBtn_callInt)
        endCallBtn.setOnClickListener{
            videoView.stopPlayback()
            myTimer.cancel()
            timer.cancel()
            finish() //goes back to previous activity
        }

    }

    fun uploadAudio(audioFile:String, picFile: String){
        val predictAPIInd: TextView = findViewById(R.id.predictAPIInd_callInt)

        runOnUiThread(Runnable(){
            predictAPIInd.text = "uploading"
        })

        runBlocking {
            val route = "http://%s:%s/uploadAudio".format(getString(R.string.api_ip_address), getString(R.string.api_port_address))

            val file = FileDataPart.from(audioFile, name="file")
            val (request, response, result) = Fuel.upload(route)
                .add(file)
                .responseString()


            if (response.statusCode in 200..299) {
                //status code: 200-299 OK
                val msg: String = response.data.decodeToString()

                if("[!] ERROR in /uploadAudio: " in msg){
                    Log.i("[/uploadAudio]", msg)
                }else if("audio upload unsuccessful" in msg) {
                    Log.i("[/uploadAudio]", msg)
                }else{
                    //snackbarPopup(msg)
                    runOnUiThread(Runnable(){
                        predictAPIInd.text = "predicting"
                    })

                    uploadImg(picFile)

                }
            }else{
                Log.i("[/uploadAudio]", "something went wrong")
            }
        }
    }

    fun uploadImg(imgFile: String){
        val predictAPIInd: TextView = findViewById(R.id.predictAPIInd_callInt)

        runBlocking {
            val route = "http://%s:%s/predict".format(getString(R.string.api_ip_address), getString(R.string.api_port_address))

            val file = FileDataPart.from(imgFile, name="file")
            val (request, response, result) = Fuel.upload(route)
                .add(file)
                .responseString()

            if (response.statusCode in 200..299) {
                //status code: 200-299 OK
                val msg: String = response.data.decodeToString()

                if("[!] ERROR in /predict: " in msg){
                    Log.i("[/predict]", msg)
                }else if("image upload unsuccessful" in msg) {
                    Log.i("[/predict]", msg)
                }else{
                    runOnUiThread(Runnable(){
                        predictAPIInd.text = "done"
                    })
                    val predictLabel: TextView = findViewById(R.id.predictLabel_callInt)
                    predictLabel.text = msg
                }
            }else{
                Log.i("[/predict]", "something went wrong")
            }
        }
    }

    /**
     * Pixel copy to copy SurfaceView/VideoView into BitMap
     * Work with Surface View, Video View
     * Won't work on Normal View
     */
    fun getBitMapFromSurfaceView(videoView: SurfaceView, callback: (Bitmap?) -> Unit) {
        val bitmap: Bitmap = Bitmap.createBitmap(
            videoView.width,
            videoView.height,
            Bitmap.Config.ARGB_8888
        );
        try {
            // Create a handler thread to offload the processing of the image.
            val handlerThread = HandlerThread("PixelCopier");
            handlerThread.start();
            PixelCopy.request(
                videoView, bitmap,
                PixelCopy.OnPixelCopyFinishedListener { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    }
                    handlerThread.quitSafely();
                },
                Handler(handlerThread.looper)
            )
        } catch (e: IllegalArgumentException) {
            callback(null)
            // PixelCopy may throw IllegalArgumentException, make sure to handle it
            e.printStackTrace()
        }
    }

    private fun snackbarPopup(msg: String){
        val snackBar = Snackbar.make(
            this.findViewById(R.id.usernameLabel_callInt), msg,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackBar.show()
    }

    //preserving previous activity variable values
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            android.R.id.home -> {
                this@activityCallInterface?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}