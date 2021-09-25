package com.nobodyatall.dogsocialmedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

class activityFriendList : AppCompatActivity(), View.OnClickListener {
    private var usernameArr = ArrayList<String>()
    private var LOGIN_USER: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)

        var actionBar = getSupportActionBar()
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        //get from prev interface
        LOGIN_USER = intent.getStringExtra("USER")

        //get list of friends for the current logon user
        getFriendList(LOGIN_USER!!)

    }

    private fun getFriendList(loginUser: String){
        runBlocking {
            val route = ("http://%s:%s/friendList?username=" + loginUser).format(getString(R.string.api_ip_address), getString(R.string.api_port_address))
            val (request, response, result) = Fuel.get(route)
                .response()

            if (response.statusCode in 200..299) {
                //status code: 200-299 OK
                val msg: String = response.data.decodeToString()

                Log.i("debug", msg)
                if("[!] ERROR in" in msg){
                    snackbarPopup(msg)
                }else if ("[*] You've No Friends. Boo...." in msg){
                    //no query
                    snackbarPopup(msg)
                }else{
                    //query success
                    val retJson: JSONObject = JSONObject(msg)
                    val query = retJson.getJSONArray("friends")

                    //fillup table
                    fillTableData(query)
                    //snackbarPopup(msg)
                }
            }else{
                snackbarPopup("[!] Something went wrong")
            }
        }
    }

    private fun fillTableData(query: JSONArray){
        val rslTable: TableLayout = findViewById(R.id.friendListTable_friendList)

        //clear table & arrayList
        rslTable.removeAllViews()
        usernameArr.clear()

        //fill up table with usernames
        for (k in 0 until query.length()) {
            //jsonArr each iter to jsonObj
            val iterJson: JSONObject = JSONObject(query[k].toString())

            //add friend's username into arrayList
            usernameArr.add("" + iterJson["friend_username"])

            //filling up tablerows
            val tr = TableRow(this)
            tr.layout(0, 0, 0, 0)
            val loc = TextView(this)
            loc.setText("" + iterJson["friend_username"])
            loc.setPadding(30, 15, 30, 15)
            tr.setPadding(0, 30, 0, 0)
            tr.addView(loc)
            tr.id = k // here you can set unique id to TableRow for
            // identification
            tr.setOnClickListener(this@activityFriendList) // set TableRow onClickListner
            rslTable.addView(
                tr, TableLayout.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    //table onClick get the clicked id
    //when user clicked, invoke this function
    override fun onClick(v: View?) {
        val clicked_id = v!!.id // here you get id for clicked TableRow

        // now you can get value like this
        val userClicked: String = usernameArr[clicked_id]

        //snackbarPopup("[*] User Clicked: " + userClicked)
        val intent = Intent(this, activityViewFriendProfileCall::class.java)
        intent.putExtra("USER", LOGIN_USER)
        intent.putExtra("VIEW_USER", userClicked)
        startActivity(intent)
    }

    private fun snackbarPopup(msg: String){
        val snackBar = Snackbar.make(
            this.findViewById(R.id.friendListTable_friendList), msg,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackBar.show()
    }

    //preserving previous activity variable values
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            android.R.id.home -> {
                this@activityFriendList?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}