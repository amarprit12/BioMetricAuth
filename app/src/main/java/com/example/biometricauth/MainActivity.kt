package com.example.biometricauth

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private var mCancellationSignal: CancellationSignal? = null
    private val mAuthenticationCallback: BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser("Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    notifyUser(getString(R.string.auth_success))
                    startActivity(Intent(this@MainActivity, SensitiveDataActivity::class.java))
                }
            }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkBiometricSupport()
        val button = findViewById<Button>(R.id.btn_authenticate)
        button.setOnClickListener {
            val biometricPrompt: BiometricPrompt = BiometricPrompt.Builder(this)
                    .setTitle(getString(R.string.auth_title))
                    .setSubtitle(getString(R.string.auth_subTitle))
                    .setDescription(getString(R.string.auth_description))
                    .setNegativeButton(getString(R.string.cancel_button), this.mainExecutor, { dialog, which ->
                    }).build()
            biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, mAuthenticationCallback)
        }
    }

    private fun checkBiometricSupport(): Boolean {
        val keyguardManager: KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isKeyguardSecure) {
            notifyUser(getString(R.string.fingerprint_issue))
            return false
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            notifyUser(getString(R.string.fingerprint_issue))
            return false
        }
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getCancellationSignal(): CancellationSignal {
        mCancellationSignal = CancellationSignal()
        mCancellationSignal?.setOnCancelListener {
            notifyUser(getString(R.string.authentication_cancelled))
        }
        return mCancellationSignal as CancellationSignal
    }
}