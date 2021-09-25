package com.nobodyatall.dogsocialmedia

import android.content.Intent
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


class activityProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        var actionBar = getSupportActionBar()
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        //get from prev interface
        val loginUser: String? = intent.getStringExtra("USER")

        //get profile details & assignation
        getProfileDetail(loginUser)

        val editProfileBtn: Button = findViewById(R.id.editProfileBtn_profile)
        editProfileBtn.setOnClickListener{
            val intent = Intent(this, activityEditProfile::class.java)
            intent.putExtra("USER", loginUser)
            startActivity(intent)
        }
    }

    private fun getProfileDetail(loginUser: String?){
        val params = JSONObject()
        params.put("username", loginUser)

        runBlocking {
            val signupRoute = "http://%s:%s/getProfileDet".format(getString(R.string.api_ip_address), getString(R.string.api_port_address))
            val (request, response, result) = Fuel.post(signupRoute)
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

                    //setting values into interface
                    val usernameTF: EditText = findViewById(R.id.usernameTF_profile)
                    usernameTF.text = Editable.Factory.getInstance().newEditable("" + retJSON["username"])

                    val emailTF: EditText = findViewById(R.id.emailTF_profile)
                    emailTF.text = Editable.Factory.getInstance().newEditable("" + retJSON["email"])

                    val ipTF: EditText = findViewById(R.id.ipTF_profile)
                    ipTF.text = Editable.Factory.getInstance().newEditable("" + retJSON["ip"])

                    val portTF: EditText = findViewById(R.id.portTF_profile)
                    portTF.text = Editable.Factory.getInstance().newEditable("" + retJSON["port"])
                }
            }else{
                snackbarPopup("[!] Something went wrong")
            }
        }
    }

    private fun snackbarPopup(msg: String){
        val snackBar = Snackbar.make(
            this.findViewById(R.id.emailTF_profile), msg,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackBar.show()
    }

    //preserving previous activity variable values
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            android.R.id.home -> {
                this@activityProfile?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}