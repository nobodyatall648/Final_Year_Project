package com.nobodyatall.dogsocialmedia

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.EditText
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_CODE = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ThreadPolicy permitting all
        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        //permission checking during init
        if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions()
        }

        val signupBtn: Button = findViewById(R.id.signupBtn_login)
        signupBtn.setOnClickListener{
            //Log.i("Signup Btn Clicked", "[*] SignUp button clicked!")
            val intent = Intent(this, activitySignUp::class.java)
            startActivity(intent)
        }

        val loginBtn: Button = findViewById(R.id.loginBtn_login)
        loginBtn.setOnClickListener{

            val usernameTF: EditText = findViewById(R.id.usernameTF_login)
            val passwordPF: EditText = findViewById(R.id.passwordPF_login)

            val params = JSONObject()
            params.put("username", usernameTF.text)
            params.put("password", passwordPF.text)

            runBlocking {
                val route = "http://%s:%s/login".format(getString(R.string.api_ip_address), getString(R.string.api_port_address))
                val (request, response, result) = Fuel.post(route)
                    .header("Content-Type" to "application/json")
                    .body(params.toString())
                    .response()

                if (response.statusCode in 200..299) {
                    //status code: 200-299 OK
                    val msg: String = response.data.decodeToString()

                    if(msg.equals("Valid Credential")) {
                        //successful login
                        Log.i("Res Content: ", response.data.decodeToString())
                        val snackBar = Snackbar.make(
                            it, "[*] Logging in...",
                            Snackbar.LENGTH_LONG
                        ).setAction("Action", null)
                        snackBar.show()

                        GlobalScope.launch(context = Dispatchers.Main) {
                            //delay(1500)
                            //redirect to mainMenu
                            val intent = Intent(this@MainActivity, activityMainMenu::class.java)

                            intent.putExtra("USER", "" + usernameTF.text)
                            startActivity(intent)
                        }
                    }else if(msg.equals("Invalid Credential")){
                        //invalid cred
                        Log.i("Res Content: ", response.data.decodeToString())
                        val snackBar = Snackbar.make(
                            it, "[!] Invalid Credential, try again.",
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
                    val snackBar = Snackbar.make(
                        it,  "[!] Something went wrong!",
                        Snackbar.LENGTH_LONG
                    ).setAction("Action", null)
                    snackBar.show()
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW
        )

        ActivityCompat.requestPermissions(
            this,
            permissions,
            PERMISSIONS_CODE
        )
    }
}