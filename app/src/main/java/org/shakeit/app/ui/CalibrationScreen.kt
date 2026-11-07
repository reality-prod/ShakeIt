package org.shakeit.app.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt
import org.shakeit.app.data.Calibration
import org.shakeit.app.data.ShakePreferences

@Composable fun CalibrationScreen(modifier: Modifier, preferences: ShakePreferences) {
    val context = LocalContext.current; var peak by remember { mutableFloatStateOf(0f) }; var samples by remember { mutableIntStateOf(0) }; var running by remember { mutableStateOf(false) }; var saved by remember { mutableStateOf(false) }
    DisposableEffect(running) {
        if (!running) return@DisposableEffect onDispose { }
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val listener = object : SensorEventListener { override fun onSensorChanged(event: SensorEvent) { val value = sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]) / SensorManager.GRAVITY_EARTH; peak = maxOf(peak, value); samples++ }; override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit }
        manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        onDispose { manager.unregisterListener(listener) }
    }
    Column(modifier.fillMaxSize().background(Color(0xFFF7F5FB)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Calibrate", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF211D2B)); Text("Teach ShakeIt! what your light and hard shakes feel like. Calibration stays on this device.", color = Color(0xFF6F6A78))
        Card(shape = RoundedCornerShape(22.dp)) { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(if (running) "Shake naturally now" else "Ready when you are", fontWeight = FontWeight.Bold, fontSize = 19.sp); Text("Peak intensity ${"%.2f".format(peak)} g", color = Color(0xFF6F6A78), modifier = Modifier.padding(top = 8.dp)); Text("$samples sensor samples", color = Color(0xFF6F6A78), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)); Button(onClick = { running = !running }, modifier = Modifier.padding(top = 18.dp)) { Text(if (running) "Stop recording" else "Record a shake") } } }
        if (peak > 0f) Text("This peak will set your hard threshold. Medium and light thresholds are derived below it for reliable separation.", color = Color(0xFF6F6A78), fontSize = 14.sp)
        Button(enabled = peak > 0f && !running, onClick = { preferences.calibration = Calibration(peak * .42f, peak * .68f, peak.coerceAtLeast(1.4f)); saved = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Text(if (saved) "Calibration saved" else "Save calibration") }
        Text("Current thresholds: light ${"%.2f".format(preferences.calibration.light)} g · medium ${"%.2f".format(preferences.calibration.medium)} g · hard ${"%.2f".format(preferences.calibration.hard)} g", color = Color(0xFF6F6A78), fontSize = 13.sp)
    }
}
