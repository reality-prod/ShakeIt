package org.shakeit.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.shakeit.app.MainActivity
import org.shakeit.app.actions.ActionExecutor
import org.shakeit.app.data.ShakePreferences
import org.shakeit.app.data.ShakeStrength
import org.shakeit.app.sensor.ShakeDetector

class ShakeForegroundService : Service() {
    companion object { const val OPEN_WALLET = "open_wallet"; private const val CHANNEL = "shakeit_listener"; private const val LISTENER_NOTIFICATION = 40 }
    private lateinit var sensorManager: SensorManager; private lateinit var detector: ShakeDetector; private lateinit var preferences: ShakePreferences; private lateinit var executor: ActionExecutor; private val recent = ArrayDeque<Pair<Long, ShakeStrength>>()
    override fun onCreate() { super.onCreate(); preferences = ShakePreferences(this); executor = ActionExecutor(this); createChannel(); startForeground(LISTENER_NOTIFICATION, listenerNotification()); sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager; detector = ShakeDetector({ preferences.calibration }, ::handleShake); sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { sensorManager.registerListener(detector, it, SensorManager.SENSOR_DELAY_GAME) } }
    private fun handleShake(strength: ShakeStrength) { val now = android.os.SystemClock.elapsedRealtime(); recent.addLast(now to strength); while (recent.isNotEmpty() && now - recent.first().first > 1800L) recent.removeFirst(); preferences.shortcuts().filter { it.enabled }.forEach { shortcut -> if (recent.count { it.second == shortcut.strength } == shortcut.shakes) { recent.clear(); when (val result = executor.execute(shortcut.action)) { is ActionExecutor.Result.Success -> notifyMessage(result.message); is ActionExecutor.Result.Failed -> notifyMessage(result.message); ActionExecutor.Result.NeedsDndAccess -> notifySettings("Allow Do not disturb access", Intent("android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS")); ActionExecutor.Result.NeedsWalletUnlock -> notifyWallet() } } } }
    private fun listenerNotification() = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(android.R.drawable.ic_menu_compass).setContentTitle("ShakeIt! is listening").setContentText("Motion shortcuts are active").setOngoing(true).setCategory(NotificationCompat.CATEGORY_SERVICE).build()
    private fun notifyMessage(message: String) { getSystemService(NotificationManager::class.java).notify(41, NotificationCompat.Builder(this, CHANNEL).setSmallIcon(android.R.drawable.ic_menu_compass).setContentTitle("ShakeIt!").setContentText(message).setAutoCancel(true).build()) }
    private fun notifyWallet() { val intent = Intent(this, MainActivity::class.java).putExtra(OPEN_WALLET, true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP); notifySettings("Tap to unlock your wallet", intent) }
    private fun notifySettings(title: String, intent: Intent) { val pending = PendingIntent.getActivity(this, title.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); getSystemService(NotificationManager::class.java).notify(41, NotificationCompat.Builder(this, CHANNEL).setSmallIcon(android.R.drawable.ic_menu_compass).setContentTitle(title).setContentText("Tap to continue").setContentIntent(pending).setAutoCancel(true).build()) }
    private fun createChannel() { getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL, "Shake detection", NotificationManager.IMPORTANCE_LOW)) }
    override fun onDestroy() { sensorManager.unregisterListener(detector); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
