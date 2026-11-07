package org.shakeit.app.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt
import org.shakeit.app.data.Calibration
import org.shakeit.app.data.ShakeStrength

class ShakeDetector(private val calibration: () -> Calibration, private val onShake: (ShakeStrength) -> Unit) : SensorEventListener {
    private var lastTrigger = 0L; private var gravity = FloatArray(3)
    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.8f
        for (axis in 0..2) gravity[axis] = alpha * gravity[axis] + (1 - alpha) * event.values[axis]
        val x = event.values[0] - gravity[0]; val y = event.values[1] - gravity[1]; val z = event.values[2] - gravity[2]
        val acceleration = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH; val now = SystemClock.elapsedRealtime()
        if (now - lastTrigger < 300L) return
        val levels = calibration(); val strength = when { acceleration >= levels.hard -> ShakeStrength.HARD; acceleration >= levels.medium -> ShakeStrength.MEDIUM; acceleration >= levels.light -> ShakeStrength.LIGHT; else -> null } ?: return
        lastTrigger = now; onShake(strength)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
