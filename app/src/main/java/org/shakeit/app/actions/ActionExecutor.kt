package org.shakeit.app.actions

import android.app.NotificationManager
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import org.shakeit.app.data.ShortcutAction

class ActionExecutor(private val context: Context) {
    fun execute(action: ShortcutAction): Result = when (action) { ShortcutAction.FLASHLIGHT -> toggleFlashlight(); ShortcutAction.DO_NOT_DISTURB -> toggleDnd(); ShortcutAction.SOUND -> toggleSound(); ShortcutAction.WALLET -> Result.NeedsWalletUnlock }
    private fun toggleFlashlight(): Result = runCatching {
        val manager = context.getSystemService(CameraManager::class.java); val camera = manager.cameraIdList.firstOrNull { id -> manager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true } ?: return Result.Failed("This device has no torch")
        val store = context.getSharedPreferences("shakeit_runtime", Context.MODE_PRIVATE); val current = store.getBoolean("torch", false); manager.setTorchMode(camera, !current); store.edit().putBoolean("torch", !current).apply(); Result.Success(if (!current) "Flashlight on" else "Flashlight off")
    }.getOrElse { Result.Failed("Flashlight unavailable") }
    private fun toggleDnd(): Result { val manager = context.getSystemService(NotificationManager::class.java); if (!manager.isNotificationPolicyAccessGranted) return Result.NeedsDndAccess; val store = context.getSharedPreferences("shakeit_runtime", Context.MODE_PRIVATE); val current = store.getBoolean("dnd", false); manager.setInterruptionFilter(if (current) NotificationManager.INTERRUPTION_FILTER_ALL else NotificationManager.INTERRUPTION_FILTER_PRIORITY); store.edit().putBoolean("dnd", !current).apply(); return Result.Success(if (current) "Do not disturb off" else "Do not disturb on") }
    private fun toggleSound(): Result { val audio = context.getSystemService(AudioManager::class.java); val silent = audio.ringerMode == AudioManager.RINGER_MODE_SILENT; audio.ringerMode = if (silent) AudioManager.RINGER_MODE_NORMAL else AudioManager.RINGER_MODE_VIBRATE; return Result.Success(if (silent) "Sound on" else "Vibrate mode") }
    sealed interface Result { data class Success(val message: String) : Result; data class Failed(val message: String) : Result; data object NeedsWalletUnlock : Result; data object NeedsDndAccess : Result }
}
