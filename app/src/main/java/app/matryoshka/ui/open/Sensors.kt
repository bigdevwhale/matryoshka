package app.matryoshka.ui.open

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import app.matryoshka.domain.Gesture
import kotlin.math.sqrt

/** The phone's own sensors for the gesture locks. Returns a function that stops listening. */
object Sensors {
    fun watch(ctx: Context, g: Gesture, onDone: () -> Unit): () -> Unit {
        val sm = ctx.getSystemService(SensorManager::class.java) ?: return {}
        var fired = false
        fun done() { if (!fired) { fired = true; onDone() } }
        val l = when (g) {
            // A palm over the screen: the proximity sensor says "near" for a second and a half.
            Gesture.COVER -> {
                val s = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY) ?: return {}
                val h = android.os.Handler(android.os.Looper.getMainLooper())
                var near = false
                val check = Runnable { if (near) done() }
                object : SensorEventListener {
                    override fun onSensorChanged(e: SensorEvent) {
                        near = e.values[0] < minOf(s.maximumRange, 3f)
                        h.removeCallbacks(check)
                        if (near) h.postDelayed(check, 1500)
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }.also { sm.registerListener(it, s, SensorManager.SENSOR_DELAY_UI) }
            }
            // Four hard jolts within two seconds.
            Gesture.SHAKE -> {
                val s = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return {}
                val hits = ArrayDeque<Long>()
                var last = 0L
                object : SensorEventListener {
                    override fun onSensorChanged(e: SensorEvent) {
                        val (x, y, z) = Triple(e.values[0], e.values[1], e.values[2])
                        val a = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
                        val now = System.currentTimeMillis()
                        if (a > 11f && now - last > 180) {
                            last = now
                            hits.addLast(now)
                            while (hits.isNotEmpty() && now - hits.first() > 2000) hits.removeFirst()
                            if (hits.size >= 4) done()
                        }
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }.also { sm.registerListener(it, s, SensorManager.SENSOR_DELAY_GAME) }
            }
            // Face down on the table for a second.
            Gesture.FLIP -> {
                val s = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return {}
                var since = 0L
                object : SensorEventListener {
                    override fun onSensorChanged(e: SensorEvent) {
                        val now = System.currentTimeMillis()
                        if (e.values[2] < -8.3f) { if (since == 0L) since = now else if (now - since > 1000) done() }
                        else since = 0L
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }.also { sm.registerListener(it, s, SensorManager.SENSOR_DELAY_UI) }
            }
        }
        return { sm.unregisterListener(l) }
    }

}
