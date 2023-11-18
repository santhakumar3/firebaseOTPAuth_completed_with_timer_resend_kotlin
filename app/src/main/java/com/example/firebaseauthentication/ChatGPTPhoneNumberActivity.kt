package com.example.firebaseauthentication

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.Visibility
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsMessage
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class ChatGPTPhoneNumberActivity : AppCompatActivity() {

    private lateinit var phoneNumberEditText: TextInputEditText
    private lateinit var sendOtpButton: MaterialButton
    private lateinit var resendOtpButton: MaterialButton
    private lateinit var otpconfirmbutton: MaterialButton
    private lateinit var timerTextView: TextView

    private var verificationId: String? = null
    private lateinit var auth: FirebaseAuth
    private var timer: CountDownTimer? = null
    private lateinit var tokennew : PhoneAuthProvider.ForceResendingToken
    private lateinit var smsReceiver: BroadcastReceiver
    private lateinit var otpEditText: TextInputEditText

    private lateinit var otpInputLayout: TextInputLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_gptphone_number)

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        resendOtpButton = findViewById(R.id.resendOtpButton)
        otpconfirmbutton = findViewById(R.id.otpconfirmbutton)
        otpInputLayout = findViewById(R.id.otpInputLayout)
        timerTextView = findViewById(R.id.timerTextView)
        otpEditText = findViewById(R.id.otpEditText)

        auth = FirebaseAuth.getInstance()

        sendOtpButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()


            if (phoneNumber.isEmpty()) {

                // Handle empty phone number
                Toast.makeText(this, "Please enter the mobile number", Toast.LENGTH_SHORT).show()

            } else if(phoneNumber.length < 10) {
                Toast.makeText(this, "Please enter valid mobile number", Toast.LENGTH_SHORT).show()

            }else {

                if (TextUtils.isEmpty(verificationId)) {
                    startPhoneNumberVerification("+91$phoneNumber")
                } else {
                    val otp = otpEditText.text.toString().trim()
                    if (otp.length == 6) {
                        verifyPhoneNumberWithCode(otp)
                    } else {
                        // Handle invalid OTP
                        if(otp.isEmpty()){
                            Toast.makeText(this, "Please enter otp number", Toast.LENGTH_SHORT).show()
                        }else if(otp.length<6){
                            Toast.makeText(this, "Please enter valid otp number", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


            otpconfirmbutton.setOnClickListener {


                val otp = otpEditText.text.toString().trim()
                if (otp.length == 6) {
                    verifyPhoneNumberWithCode(otp)
                } else {
                    // Handle invalid OTP
                    if(otp.isEmpty()){
                        Toast.makeText(this, "Please enter otp number", Toast.LENGTH_SHORT).show()
                    }else if(otp.length<6){
                        Toast.makeText(this, "Please enter valid otp number", Toast.LENGTH_SHORT).show()
                    }
                }


            }




        }

        resendOtpButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()

            if (phoneNumber.isEmpty()) {

                // Handle empty phone number
                Toast.makeText(this, "Please enter the mobile number", Toast.LENGTH_SHORT).show()

            } else if(phoneNumber.length < 10) {
                Toast.makeText(this, "Please enter valid mobile number", Toast.LENGTH_SHORT).show()

            }else {

                resendVerificationCode("+91$phoneNumber", tokennew)
            }

        }

        // Register SMS receiver
        registerSmsReceiver()
    }

    private fun registerSmsReceiver() {
        Toast.makeText(applicationContext, "smsReceiver", Toast.LENGTH_SHORT).show()

        val intentFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras
                if (bundle != null) {
                    Toast.makeText(applicationContext, "bundle", Toast.LENGTH_SHORT).show()

                    val pdus = bundle.get("pdus") as Array<*>
                    for (i in pdus.indices) {
                        val smsMessage = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                        val messageBody = smsMessage.messageBody
                        extractOtpFromMessage(messageBody)
                    }
                }else{
                    Toast.makeText(applicationContext, "bundlenull", Toast.LENGTH_SHORT).show()
                }
            }
        }
        registerReceiver(smsReceiver, intentFilter)
    }

    private fun extractOtpFromMessage(message: String) {
        Log.d("received_sms_content: ","received_sms_content: "+message);
        // Extract the OTP from the SMS message and auto-fill the OTP EditText
        // This will depend on the format of the message received; adjust accordingly
//        val otp = message.substringAfter("Your OTP code is: ").substringBefore(".")
//        if (otp.length == 6) {
//            // Auto-fill the OTP EditText
//            // Make sure you have an EditText for OTP in your layout (e.g., otpEditText)
//            // otpEditText.setText(otp)
//            // You can also use this OTP to automatically verify the phone number
//            verifyPhoneNumberWithCode(otp)
//        }


        val otpPatter = Pattern.compile("(!^)\\d{6}")
        val matcher = otpPatter.matcher(message)
        if(matcher.find()){

            otpEditText!!.setText(matcher.group(0))
            verifyPhoneNumberWithCode(matcher.group(0))

        }

    }

    private fun verifyPhoneNumberWithCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }


    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Handle verification failure
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@ChatGPTPhoneNumberActivity.verificationId = verificationId
                    tokennew = token
//                    hidesendButton()
                      sendOtpButton.visibility = Button.GONE
                    otpEditText.isEnabled = true
                    otpEditText.visibility = EditText.VISIBLE
                    otpInputLayout.visibility = TextInputLayout.VISIBLE
                    startTimer()
//                    enableOtpEditText()

//                    enableConfirmButton()
                }
            }
        )
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Handle verification failure
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@ChatGPTPhoneNumberActivity.verificationId = verificationId
                    startTimer()
//                    enableOtpEditText()
                    resendOtpButton.visibility = MaterialButton.GONE
                    phoneNumberEditText.isEnabled = false
                    otpEditText.isEnabled = true
                    otpEditText.visibility = EditText.VISIBLE
                    otpInputLayout.visibility = TextInputLayout.VISIBLE
                    enableConfirmButton()
                }
            },
            token
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User signed in successfully
                    Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                    timer?.cancel()
                    timerTextView.visibility = TextView.GONE
                    phoneNumberEditText.setText("")
                    otpEditText.setText("")
                    otpInputLayout.visibility = TextInputLayout.GONE
                    otpconfirmbutton.visibility = MaterialButton.GONE
                    sendOtpButton.visibility = MaterialButton.VISIBLE
                    verificationId = ""

                } else {
                    // Handle sign in failure
                    Toast.makeText(this, "Login Failure", Toast.LENGTH_SHORT).show()

                }
            }
    }


    private fun startTimer() {
        timerTextView.visibility = TextView.VISIBLE
        otpEditText.isEnabled = true
        otpEditText.visibility = EditText.VISIBLE
        otpInputLayout.visibility = TextInputLayout.VISIBLE
        otpconfirmbutton.visibility = Button.VISIBLE
//        enableOtpEditText() // Enable OTP EditText when the timer finishes
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
//                val seconds = millisUntilFinished / 1000
//                timerTextView.text = "Resend OTP in $seconds seconds"
//
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                timerTextView.text = String.format("%02d:%02d", minutes, remainingSeconds)


            }

            override fun onFinish() {
                timerTextView.visibility = TextView.GONE
                otpEditText.setText("")
                otpInputLayout.visibility = TextInputLayout.GONE
                otpconfirmbutton.visibility = MaterialButton.GONE
                showResendButton()
//                disableConfirmButton()

            }
        }.start()
    }

    private fun enableOtpEditText() {
        otpEditText.isEnabled = true
    }

    private fun disableOtpEditText() {
        otpEditText.isEnabled = false
    }

    private fun enableConfirmButton() {
        otpconfirmbutton.isEnabled = true
    }

    private fun disableConfirmButton() {
        otpconfirmbutton.isEnabled = false
    }

    private fun showResendButton() {
        resendOtpButton.visibility = Button.VISIBLE
        enableOtpEditText() // Enable OTP EditText when showing the Resend button
    }

    private fun hidesendButton() {
        sendOtpButton.visibility = Button.GONE
        enableOtpEditText() // Enable OTP EditText when showing the Resend button
    }


    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
