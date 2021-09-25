package com.nobodyatall.dogsocialmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class activityEditProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        var actionBar = getSupportActionBar()
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        //get from prev interface
        val loginUser: String? = intent.getStringExtra("USER")

        val changePwBtn: Button = findViewById(R.id.chgPwBtn_editProfile)
        changePwBtn.setOnClickListener{
            val oldPwTF: EditText = findViewById(R.id.oldPwPF_editProfile)
            val newPwTF: EditText = findViewById(R.id.newPwPF_editProfile)

            val oldPw = "" + oldPwTF.text
            val newPw = "" + newPwTF.text
            changePasswd(oldPw, newPw, loginUser!!)
        }

        val updateConnBtn: Button = findViewById(R.id.updateConnBtn_editProfile)
        updateConnBtn.setOnClickListener{
            val ipTF: EditText = findViewById(R.id.ipTF_editProfile)
            val portTF: EditText = findViewById(R.id.portTF_editProfile)

            val ip = "" + ipTF.text
            val port = "" + portTF.text

            if(!ip.equals("") && !port.equals("")){
                updateConnectionVal(ip, port, loginUser!!)
            }else{
                snackbarPopup("[!] IP & Port Must Not Be Blank!")
            }
        }

    }

    private fun updateConnectionVal(ip: String, port: String, username: String){
        val params = JSONObject()
        params.put("username", username.trim())
        params.put("ip", ip.trim())
        params.put("port", port.trim())

        runBlocking {
            val updateRoute = "http://%s:%s/updateConnVal".format(getString(R.string.api_ip_address), getString(R.string.api_port_address))
            val (request, response, result) = Fuel.post(updateRoute)
                .header("Content-Type" to "application/json")
                .body(params.toString())
                .response()

            if (response.statusCode in 200..299) {
                //status code: 200-299 OK
                val msg: String = response.data.decodeToString()

                if("[!] ERROR in" in msg){
                    snackbarPopup(msg)
                }else{
                    //successful updated password
                    snackbarPopup(msg)
                }
            }else{
                snackbarPopup("[!] Something went wrong")
            }
        }
    }

    private fun changePasswd(oldPw: String, newPw: String, username: String){
        val params = JSONObject()
        params.put("username", username)
        params.put("oldpw", oldPw)
        params.put("newpw", newPw)

        runBlocking {
            val signupRoute = "http://%s:%s/changePw".format(getString(R.string.api_ip_address), getString(R.string.api_port_address))
            val (request, response, result) = Fuel.post(signupRoute)
                .header("Content-Type" to "application/json")
                .body(params.toString())
                .response()

            if (response.statusCode in 200..299) {
                //status code: 200-299 OK
                val msg: String = response.data.decodeToString()

                if("[!] ERROR in" in msg){
                    snackbarPopup(msg)
                }else if("[!] Old Credential Not Match!" in msg) {
                    snackbarPopup(msg)
                }else{
                    //successful updated password
                    snackbarPopup(msg)
                }
            }else{
                snackbarPopup("[!] Something went wrong")
            }
        }
    }

    private fun snackbarPopup(msg: String){
        val snackBar = Snackbar.make(
            this.findViewById(R.id.oldPwPF_editProfile), msg,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackBar.show()
    }

    //preserving previous activity variable values
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            android.R.id.home -> {
                this@activityEditProfile?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}