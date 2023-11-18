package com.example.firebaseauthentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class EmailPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var register: Button
    private lateinit var cancel: Button
    private lateinit var emailstr : String
    private lateinit var passwordstr: String
    private lateinit var logout: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_password)

        // Initialize Firebase Auth
        auth = Firebase.auth

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        register = findViewById(R.id.register)
        cancel = findViewById(R.id.cancel)
        logout = findViewById(R.id.logout)

        register.setOnClickListener {
            emailstr = email.getText().toString().trim()
            passwordstr = password.getText().toString().trim()
            if(emailstr.equals("")){
                Toast.makeText(this, "enter the email", Toast.LENGTH_SHORT).show()
            }else if(passwordstr.equals("")){
                Toast.makeText(this, "enter the password", Toast.LENGTH_SHORT).show()
            }else{
                checkAleadyRegisterUser(emailstr,passwordstr)
            }
        }

        cancel.setOnClickListener {
            email.setText("")
            password.setText("")
        }

        logout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "logOut successfully", Toast.LENGTH_SHORT).show()
            email.setText("")
            password.setText("")
            emailstr = ""
            passwordstr = ""
        }

    }

    private fun checkAleadyRegisterUser(emailstr: String, passwordstr: String) {

        auth.signInWithEmailAndPassword(emailstr, passwordstr)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    user?.let {
                        // Name, email address, and profile photo Url
                        val namemodel = it.displayName
                        val emailmodel = it.email
                        val photoUrlmodel = it.photoUrl

                        // Check if user's email is verified
                        val emailVerifiedmodel = it.isEmailVerified

                        // The user's ID, unique to the Firebase project. Do NOT use this value to
                        // authenticate with your backend server, if you have one. Use
                        // FirebaseUser.getIdToken() instead.
                        val uidmodel = it.uid

                        email.setText(emailmodel)

                        Toast.makeText(this, "This email Already User :"+emailmodel, Toast.LENGTH_SHORT).show()


                    }

//                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "signInWithEmail:failure", task.exception)
//                    Toast.makeText(
//                        baseContext,
//                        "Authentication failed. Please register as new user",
//                        Toast.LENGTH_SHORT,
//                    ).show()
//                    updateUI(null)

                    newUserRegister(emailstr,passwordstr);
                }
            }

    }

    private fun newUserRegister(newemail: String, newpassword: String) {

        auth.createUserWithEmailAndPassword(newemail, newpassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                        Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
//                        updateUI(user)
                    Toast.makeText(this, "New User Added Successfully", Toast.LENGTH_SHORT).show()

                    email.setText("")
                    password.setText("")
                    emailstr = ""
                    passwordstr = ""

                } else {
                    // If sign in fails, display a message to the user.
//                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed. while register new user",
                        Toast.LENGTH_SHORT,
                    ).show()
//                        updateUI(null)
                }
            }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
//            reload()


            currentUser?.let {
                // Name, email address, and profile photo Url
                val namemodel = it.displayName
                val emailmodel = it.email
                val photoUrlmodel = it.photoUrl

                // Check if user's email is verified
                val emailVerifiedmodel = it.isEmailVerified

                // The user's ID, unique to the Firebase project. Do NOT use this value to
                // authenticate with your backend server, if you have one. Use
                // FirebaseUser.getIdToken() instead.
                val uidmodel = it.uid

                email.setText(emailmodel)

                Toast.makeText(this, "LoggedIn Current User :"+emailmodel, Toast.LENGTH_SHORT).show()


            }



        }else{
            Toast.makeText(this, "No CurrentUser Please register new user", Toast.LENGTH_SHORT).show()

        }
    }


}