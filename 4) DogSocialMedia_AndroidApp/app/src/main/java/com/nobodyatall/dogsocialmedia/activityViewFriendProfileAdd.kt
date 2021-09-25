package com.nobodyatall.dogsocialmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class activityViewFriendProfileAdd : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_friend_profile_add)

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

        val addFriendBtn: Button = findViewById(R.id.addBtn_viewFriendProfAdd)
        addFriendBtn.setOnClickListener{
            addFriend(LOGIN_USER!!, VIEW_USER!!)
        }

    }

    private fun addFriend(loginUser: String, addUser: String){
        val params = JSONObject()
        params.put("username", loginUser)
        params.put("addUser", addUser)

        runBlocking {
            val route = "http://%s:%s/addFriend".format(getString(R.string.api_ip_address), getString(R.string.api_port_address))
            val (request, response, result) = Fuel.post(route)
                .header("Content-Type" to "application/json")
                .body(params.toString())
                .response()

            if(response.statusCode in 200..299){
                val msg: String = response.data.decodeToString()

                if("[!] ERROR in /: " in msg) {
                    snackbarPopup(msg)
                }else if("[!] Friend Added Before!" in msg) {
                    snackbarPopup(msg)
                }else{
                    snackbarPopup(msg)
                }
            }else{
                snackbarPopup("[!] Something went wrong")
            }
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

                    //setting values into interface
                    val usernameTF: TextView = findViewById(R.id.usernameLabel_viewFriendProfAdd)
                    usernameTF.text = "" + retJSON["username"]

                    val emailTF: EditText = findViewById(R.id.emailTF_viewFriendProfAdd)
                    emailTF.text = Editable.Factory.getInstance().newEditable("" + retJSON["email"])

                    val ipTF: EditText = findViewById(R.id.ipTF__viewFriendProfAdd)
                    ipTF.text = Editable.Factory.getInstance().newEditable("" + retJSON["ip"])

                    val portTF: EditText = findViewById(R.id.portTF_viewFriendProfAdd)
                    portTF.text = Editable.Factory.getInstance().newEditable("" + retJSON["port"])
                }
            }else{
                snackbarPopup("[!] Something went wrong")
            }
        }
    }

    private fun snackbarPopup(msg: String){
        val snackBar = Snackbar.make(
            this.findViewById(R.id.ipTF__viewFriendProfAdd), msg,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackBar.show()
    }

    //preserving previous activity variable values
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            android.R.id.home -> {
                this@activityViewFriendProfileAdd?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}