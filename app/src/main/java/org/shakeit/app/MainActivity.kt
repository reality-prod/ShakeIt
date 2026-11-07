package org.shakeit.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat.getMainExecutor
import org.shakeit.app.data.ShakePreferences
import org.shakeit.app.service.ShakeForegroundService
import org.shakeit.app.ui.ShakeItApp
import org.shakeit.app.ui.ShakeItTheme

class MainActivity : FragmentActivity() {
    private lateinit var preferences: ShakePreferences
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) startListening() }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); preferences = ShakePreferences(this); setContent { ShakeItTheme { ShakeItApp(preferences, intent?.getBooleanExtra(ShakeForegroundService.OPEN_WALLET, false) == true, ::setListening, ::authenticateWallet) } } }
    private fun setListening(enabled: Boolean) {
        preferences.listeningEnabled = enabled
        if (!enabled) { stopService(Intent(this, ShakeForegroundService::class.java)); return }
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) else startListening()
    }
    private fun startListening() { ContextCompat.startForegroundService(this, Intent(this, ShakeForegroundService::class.java)) }
    private fun authenticateWallet(onSuccess: () -> Unit) {
        val allowed = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (BiometricManager.from(this).canAuthenticate(allowed) != BiometricManager.BIOMETRIC_SUCCESS) return
        val prompt = BiometricPrompt(this, getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() { override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onSuccess() } })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Unlock ShakeIt! wallet").setSubtitle("Confirm to show your default card").setAllowedAuthenticators(allowed).build())
    }
}
