package com.nobodyatall.dogsocialmedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class activityViewFriendProfileCall : AppCompatActivity() {
    private var rtspIP: String = ""
    private var rtspPORT: String = ""
    private var friendNAME: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_friend_profile_call)

        var actionBar = getSupportActionBar()
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        //get from prev interface
        val LOGIN_USER: String? = intent.getStringExtra("USER")
        val VIEW_USER: String? = intent.getStringExtra("VIEW_USER")

        //get user details
        getProfileDetail(VIEW_USER)

        val callBtn: Button = findViewById(R.id.callBtn_viewFriendProfCall)
        callBtn.setOnClickListener{
            //redirect to call interface
            Toast.makeText(this, "[*] calling Friend", Toast.LENGTH_LONG).show()

            //craft RTSP url
            val rtspURL = "rtsp://%s:%s/".format(rtspIP, rtspPORT)

            val intent = Intent(this, activityCallInterface::class.java)
            intent.putExtra("USER", LOGIN_USER)
            intent.putExtra("FRIEND_NAME", friendNAME)
            intent.putExtra("RTSP_URL", rtspURL)
            startActivity(intent)
        }
    }

    private fun getProfileDetail(viewUser: String?){
        val params = JSONObject()
        params.put("username", viewUser)

        runBlocking {
            val route = "http://%s:%s/getProfileDet".format(getString(R.string.api_ip_address), getString(R.string.api_port_address))
            val (request, response, result) = Fuel.post(route)
                .header("Content-Type" to "application/json")
                .body(params.toString())
                .response()

            if (response.statusCode in 200..299) {
                //status code: 200-299 OK
                val msg: String = response.data.decodeToString()

                if("[!] ERROR in /getProfileDet: " in msg){
                    snackbarPopup(msg)
                }else if("Username not found!" in msg) {
                    snackbarPopup(msg)
                }else{
                    val retJSON = JSONObject(msg)

                    //set values to class var
                    rtspIP = "" + retJSON["ip"]
                    rtspPORT = "" + retJSON["port"]
                    friendNAME = "" + retJSON["username"]

                    //setting values into interface
                    val usernameTF: TextView = findViewById(R.id.usernameLabel_viewFriendProfCall)
                    usernameTF.text = "" + retJSON["username"]

                    val emailTF: EditText = findViewById(R.id.emailTF_viewFriendProfCall)
                    emailTF.text = Editable.Factory.getInstance().newEditable("" + retJSON["email"])

                    val ipTF: EditText = findViewById(R.id.ipTF__viewFriendProfCall)
                    ipTF.text = Editable.Factory.getInstance().newEditable("" + retJSON["ip"])

                    val portTF: EditText = findViewById(R.id.portTF_viewFriendProfCall)
                    portTF.text = Editable.Factory.getInstance().newEditable("" + retJSON["port"])
                }
            }else{
                snackbarPopup("[!] Something went wrong")
            }
        }
    }

    private fun snackbarPopup(msg: String){
        val snackBar = Snackbar.make(
            this.findViewById(R.id.callBtn_viewFriendProfCall), msg,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackBar.show()
    }

    //preserving previous activity variable values
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            android.R.id.home -> {
                this@activityViewFriendProfileCall?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}