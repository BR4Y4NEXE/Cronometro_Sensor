package com.example.cronometro_sensor

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.max
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var tiltFactor: Float = 1.0f

    private lateinit var timerTextView: TextView
    private lateinit var startButton: Button

    private var running = false
    private var accumulatedTime: Long = 0L
    private var lastUpdateTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.timer)
        startButton = findViewById(R.id.btn_start)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        startButton.setOnClickListener {
            if (!running) {
                startTimer()
            } else {
                pauseTimer()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val z = event.values[2]
            tiltFactor = 1.0f + ((9.8f - abs(z)) / 9.8f) * 1.5f
            tiltFactor = tiltFactor.coerceIn(0.5f, 3.0f)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startTimer() {
        running = true
        lastUpdateTime = System.currentTimeMillis()
        startButton.text = "Pausar"

        timerTextView.post(object : Runnable {
            override fun run() {
                if (running) {
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = currentTime - lastUpdateTime
                    lastUpdateTime = currentTime

                    accumulatedTime += (deltaTime * tiltFactor).toLong()

                    val seconds = (accumulatedTime / 1000).toInt()
                    val minutes = seconds / 60
                    val hours = minutes / 60
                    val displaySeconds = seconds % 60
                    val displayMinutes = minutes % 60

                    timerTextView.text = String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d",
                        hours,
                        displayMinutes,
                        displaySeconds
                    )

                    timerTextView.postDelayed(this, 50)
                }
            }
        })
    }

    private fun pauseTimer() {
        running = false
        startButton.text = "Reanudar"
    }
}
