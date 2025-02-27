package com.ftechs2016.pedometre

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import java.text.DecimalFormat
import java.util.Locale

class BackGroundService : Service(), SensorEventListener {
    var running = false
    private var nowSteps = 0f
    private var run = false
    private var seconds = 0
    var time: String? = "00:00:00"
    private var sensorManager: SensorManager? = null
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var checkSteps = 0f
    var currentSteps = 0
    var notiSetps = 0
    private var distance = 0f
    private var calories = 0f
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        //        running = true;
        val stepSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(this, "No Sensor Detected on Device", Toast.LENGTH_SHORT).show()
            Log.d("here", "Program is here: no detendion")
        } else {
            sensorManager!!.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("here", "Program is here: detection")
        }
        val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
        running = sharedPreferences.getBoolean("running", false)
        checkSteps = intent.getFloatExtra("sensorValue", 0f)
        seconds = intent.getIntExtra("sec", 0)
        time = intent.getStringExtra("time")
        if (checkSteps == 0f) {
            val current = intent.getStringExtra("current")
            val c = current!!.toFloat()
            previousTotalSteps = c
            Log.e("total steps:", "Program is here: " + previousTotalSteps + "1")
        } else {
            previousTotalSteps = checkSteps
            run = false
        }
        val h = Handler()
        h.post(object : Runnable {
            override fun run() {
                running = sharedPreferences.getBoolean("running", false)
                if (running) {
                    val handler = Handler()
                    handler.post(object : Runnable {
                        override fun run() {
                            val hours = seconds / 3600
                            val minutes = seconds % 3600 / 60
                            val secs = seconds % 60
                            time = String.format(
                                Locale.getDefault(),
                                "%d:%02d:%02d",
                                hours,
                                minutes,
                                secs
                            )
                            Log.d(ContentValues.TAG, "runService:$time")
                            if (running) {
                                Log.d(ContentValues.TAG, "seconds:$seconds")
                                seconds++
                                saveTime()
                                showNotification()
                            }
                            handler.postDelayed(this, 1000)
                        }
                    })
                }
                h.postDelayed(this, 1000)
            }
        })
        showNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (running) {
            val detectedSteps = sensorEvent.values[0]

            val pref = getSharedPreferences("steps", MODE_PRIVATE)
            pref.edit().putFloat("sensorValue", detectedSteps).apply()

            if (checkSteps == 0f) {
                previousTotalSteps++
                val st = previousTotalSteps + detectedSteps
                totalSteps = st - detectedSteps
                totalSteps++
            } else {
                if (run) {
                    nowSteps++
                    val st = nowSteps + detectedSteps
                    totalSteps = st - detectedSteps
                    totalSteps++
                } else {
                    totalSteps -= previousTotalSteps
                    totalSteps++
                    nowSteps = totalSteps
                    run = true
                }
            }

            // **Fix: Adjust step count only for newly detected steps**
            val newSteps = totalSteps - previousTotalSteps
            val correctedSteps = newSteps * (18f / 3f) // Apply correction only to new steps
            totalSteps = previousTotalSteps + correctedSteps

            // Update previousTotalSteps for future adjustments
            previousTotalSteps = totalSteps

            distance = 75 * totalSteps / 10000
            calories = (0.04 * totalSteps).toFloat()

            saveData()
            showNotification()
        }
    }

    fun saveTime() {
        val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
        sharedPreferences.edit().putInt("time", seconds).apply()
        sharedPreferences.edit().putString("totalTime", time).apply()
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
        sharedPreferences.edit().putFloat("key1", totalSteps).apply()
        sharedPreferences.edit().putFloat("distance", distance).apply()
        sharedPreferences.edit().putFloat("calories", calories).apply()
        sharedPreferences.edit().putInt("time", seconds).apply()
        sharedPreferences.edit().putString("totalTime", time).apply()
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    fun showNotification() {
        val notificationLayout = RemoteViews(packageName, R.layout.noti_layout)
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_layout)
        notificationLayoutExpanded.setTextViewText(
            R.id.steps_notification,
            totalSteps.toString() + ""
        )
        notificationLayout.setTextViewText(R.id.steps_small, totalSteps.toString() + "")
        notificationLayoutExpanded.setTextViewText(
            R.id.calories_large,
            DecimalFormat("#.###").format(calories.toDouble()) + ""
        )
        notificationLayout.setTextViewText(
            R.id.calories_small,
            DecimalFormat("#.###").format(calories.toDouble()) + ""
        )
        notificationLayout.setTextViewText(
            R.id.distance_small,
            DecimalFormat("#.###").format(distance.toDouble()) + ""
        )
        notificationLayoutExpanded.setTextViewText(
            R.id.distance_large,
            DecimalFormat("#.###").format(distance.toDouble()) + ""
        )
        notificationLayoutExpanded.setTextViewText(R.id.time_large, time)
        notificationLayout.setTextViewText(R.id.time_small, time)
        val CHANNELID = "Foreground Service ID"
        var channel: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            val notification = Notification.Builder(this, CHANNELID)
                .setSmallIcon(R.drawable.ic_play)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setSmallIcon(R.drawable.ic_launcher_background)
            startForeground(1001, notification.build())
        }
    }
}

