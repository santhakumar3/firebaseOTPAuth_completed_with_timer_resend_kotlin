package com.example.firebaseauthentication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class PhoneNumberActivity : AppCompatActivity() {
    
    private lateinit var phonenumber : EditText
    private lateinit var otpnumber : EditText
    private lateinit var sendotp : Button
    private lateinit var phonenumberstr : String
    private lateinit var otpstr: String

    private lateinit var auth: FirebaseAuth
    private lateinit var otpcodesentverificationId: String
    private lateinit var confirmotp: Button

    //phoneotp Auto retrival from received message using firebase broadcast receiver
    private val REQ_USER_CONSENT =200
    var smsBroadcastReceiver : SmsBroadcastReceiver? = null

    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsBroadcastReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQ_USER_CONSENT){
            if(resultCode == RESULT_OK && data != null) {
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                getOtpFromMessage(message)
            }
        }

    }

    private fun getOtpFromMessage(message: String?) {
        val otpPatter = Pattern.compile("(!^)\\d{6}")
        val matcher = otpPatter.matcher(message)
        if(matcher.find()){

            otpnumber!!.setText(matcher.group(0))

        }

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_number)

        // Initialize Firebase Auth
        auth = Firebase.auth
        
        phonenumber = findViewById(R.id.phonenumber)

        sendotp = findViewById(R.id.sendotp)
        confirmotp = findViewById(R.id.confirmotp)

        //phoneotp Auto retrival from received message using firebase broadcast receiver
        otpnumber = findViewById(R.id.otp)
//        startSmartUserConsent()

        

        
        sendotp.setOnClickListener {

            phonenumberstr = phonenumber.getText().toString().trim()
            if(phonenumberstr.equals("")){
                Toast.makeText(this, "Enter Phone Number", Toast.LENGTH_SHORT).show()
            }else  if(phonenumberstr.length< 10){
                Toast.makeText(this, "Enter Valid Phone Number", Toast.LENGTH_SHORT).show()
            }else{
                var newphone = "+91" + phonenumberstr
                Log.d("phoneno: ","phoneno :"+newphone)
                sendOtp(newphone);
            }
        }
        
        confirmotp.setOnClickListener { 
            otpstr = otpnumber.getText().toString().trim()
            if(otpstr.equals("")){
                Toast.makeText(this, "Enter the OTP Number", Toast.LENGTH_SHORT).show()
            }

            else{
                manualOtpVerification(otpstr)
            }
        }

    }

    private fun startSmartUserConsent() {
        val client = SmsRetriever.getClient(this)
        client.startSmsUserConsent(null)
    }

    private fun registerBroadcastReceiver() {
       smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener = object : SmsBroadcastReceiver.SmsBroadcastReceiverListener{
            override fun onSuccess(intent: Intent?) {
                if (intent != null) {
                    startActivityForResult(intent,REQ_USER_CONSENT)
                }
            }

            override fun onFailure() {
                TODO("Not yet implemented")
            }

        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver,intentFilter)


    }


    private fun manualOtpVerification(otpstr: String) {


        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(otpcodesentverificationId!!, otpstr)
        // [END verify_with_code]

        signInWithPhoneAuthCredential(credential,"fromManualOTPVerification")

    }

    private fun sendOtp(phonenumberstr: String) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phonenumberstr) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onCodeSent(
                    verificationId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken,
                ) {
                    // Save the verification id somewhere
                    otpcodesentverificationId = verificationId

                    Toast.makeText(applicationContext, "code sent ", Toast.LENGTH_SHORT).show()
                    otpnumber.isVisible = true
                    phonenumber.isEnabled = false
                    sendotp.isVisible = false
                    confirmotp.isVisible = true

//                    startSmartUserConsent(phonenumberstr)

                    // The corresponding whitelisted code above should be used to complete sign-in.
//                    this@PhoneNumberActivity.enableUserManuallyInputCode()
                }

                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    // Sign in with the credential
                    Toast.makeText(applicationContext, "VerificationCompleted ", Toast.LENGTH_SHORT).show()

                    signInWithPhoneAuthCredential(phoneAuthCredential,"fromOnVerificationCompleted")
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(applicationContext, "VerificationFailed ", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeAutoRetrievalTimeOut(p0: String) {
                    super.onCodeAutoRetrievalTimeOut(p0)

                    Toast.makeText(applicationContext, "AutoRetrieval code: "+p0, Toast.LENGTH_SHORT).show()

                    val credential = PhoneAuthProvider.getCredential(otpcodesentverificationId!!, p0)
                    signInWithPhoneAuthCredential(credential,"fromOnCodeAutoRetrievavlTimeOut")
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential,msg: String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user

                    Toast.makeText(this, "OTPManualEntryCompleted :"+msg, Toast.LENGTH_SHORT).show()

                } else {
                    // Sign in failed, display a message and update the UI
//                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "OTPManualEntryFailed-1", Toast.LENGTH_SHORT).show()

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "OTPManualEntryFailed-2", Toast.LENGTH_SHORT).show()

                    }
                    // Update UI
                }
            }


    }
}