package com.nobodyatall.dogsocialmedia

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.json.JSONObject

class activitySignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        var actionBar = getSupportActionBar()
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        val signupBtn : Button = findViewById(R.id.signupBtn_signup)
        signupBtn.setOnClickListener {
            val usernameTF: EditText = findViewById(R.id.usernameTF_signup)
            val passwordPF: EditText = findViewById(R.id.password_signup)
            val emailTF: EditText = findViewById(R.id.emailTF_signup)
            val ipTF: EditText = findViewById(R.id.ipTF_signup)
            val portTF: EditText = findViewById(R.id.portTF_signup)

            val params = JSONObject()
            params.put("username", usernameTF.text)
            params.put("password", passwordPF.text)
            params.put("email", emailTF.text)
            params.put("ip", ipTF.text)
            params.put("port", portTF.text)

            Log.i("param", params.toString())

            runBlocking {
                val signupRoute = "http://%s:%s/signup".format(getString(R.string.api_ip_address), getString(R.string.api_port_address))
                val (request, response, result) = Fuel.post(signupRoute)
                    .header("Content-Type" to "application/json")
                    .body(params.toString())
                    .response()

                if(response.statusCode in 200..299){
                    //status code: 200-299 OK
                    val msg: String = response.data.decodeToString()
                    if(msg.equals("Signup Successful!")) {
                        //successful register
                        Log.i("Res Content: ", response.data.decodeToString())
                        val snackBar = Snackbar.make(
                            it, "[*] Register Successful",
                            Snackbar.LENGTH_LONG
                        ).setAction("Action", null)
                        snackBar.show()

                        GlobalScope.launch(context = Dispatchers.Main) {
                            delay(1500)
                            //redirect back to login
                            val intent = Intent(this@activitySignUp, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }else if("[!] ERROR in /signup: Duplicate entry" in msg && "for key 'Profiles.PRIMARY'" in msg){
                        //username exists/reused
                         Log.i("Res Content: ", response.data.decodeToString())
                         val snackBar = Snackbar.make(
                             it, "[!] Username Used, Please Try Another Username",
                             Snackbar.LENGTH_LONG
                         ).setAction("Action", null)
                         snackBar.show()
                    }else{
                        //RestAPI server problem
                        Log.i("Res Content: ", response.data.decodeToString())
                        val snackBar = Snackbar.make(
                            it, response.data.decodeToString(),
                            Snackbar.LENGTH_LONG
                        ).setAction("Action", null)
                        snackBar.show()
                    }

                }else{
                    //status code: outside 200-299 Not OK
                    Log.i("Res Content: ", response.data.decodeToString())
                    val snackBar = Snackbar.make(
                        it, "[!] Something went wrong",
                        Snackbar.LENGTH_LONG
                    ).setAction("Action", null)
                    snackBar.show()

                    val snackBar1 = Snackbar.make(
                        it,  response.data.decodeToString(),
                        Snackbar.LENGTH_LONG
                    ).setAction("Action", null)
                    snackBar1.show()
                }

            }
        }
    }
}