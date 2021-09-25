package com.nobodyatall.dogsocialmedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button

class activityMainMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        var actionBar = getSupportActionBar()
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        //get value from prev interface
        val loginUser: String? = intent.getStringExtra("USER")

        //Log.i("[*] Username Login: ", loginUser!!)

        val profileBtn: Button = findViewById(R.id.profileBtn_mainMenu)
        profileBtn.setOnClickListener{
            val intent = Intent(this, activityProfile::class.java)
            intent.putExtra("USER", loginUser)
            startActivity(intent)
        }

        val callBtn: Button = findViewById(R.id.callBtn_mainMenu)
        callBtn.setOnClickListener{
            val intent = Intent(this, activityFriendList::class.java)
            intent.putExtra("USER", loginUser)
            startActivity(intent)
        }

        val addFriendBtn: Button = findViewById(R.id.addFriendBtn_mainMenu)
        addFriendBtn.setOnClickListener{
            val intent = Intent(this, activityAddFriend::class.java)
            intent.putExtra("USER", loginUser)
            startActivity(intent)
        }

        val startServerBtn: Button = findViewById(R.id.startServer_mainMenu)
        startServerBtn.setOnClickListener{
            val intent = Intent(this, activityServerStatus::class.java)
            intent.putExtra("USER", loginUser)
            startActivity(intent)
        }

        val reportBtn: Button = findViewById(R.id.report_mainMenu)
        reportBtn.setOnClickListener{
            val intent = Intent(this, activityListReports::class.java)
            intent.putExtra("USER", loginUser)
            startActivity(intent)
        }

    }
}