package com.example.firebaseauthentication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var emailpassword: Button
    private lateinit var phonenumber: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailpassword = findViewById(R.id.emailpassword)
        phonenumber = findViewById(R.id.phonenumber)

        emailpassword.setOnClickListener {
            val intent = Intent(this@MainActivity, EmailPasswordActivity::class.java)
            startActivity(intent)
        }

        phonenumber.setOnClickListener {
//            val intent = Intent(this@MainActivity, PhoneNumberActivity::class.java)
//            startActivity(intent)
            val intent = Intent(this@MainActivity, ChatGPTPhoneNumberActivity::class.java)
            startActivity(intent)
        }



    }




}