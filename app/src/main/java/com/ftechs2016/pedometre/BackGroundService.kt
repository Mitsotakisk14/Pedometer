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
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.ftechs2016.pedometre.api.MusicApiService
import com.ftechs2016.pedometre.api.MusicTrack
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("MusicDebug", "Service started, initializing...")
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(this, "No Sensor Detected on Device", Toast.LENGTH_SHORT).show()
            Log.d("here", "Program is here: no detection")
        } else {
            sensorManager!!.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("here", "Program is here: detection")
        }

        val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
        running = sharedPreferences.getBoolean("running", false)
        Log.d("MusicDebug", "Service started, running = $running")

        checkSteps = intent.getFloatExtra("sensorValue", 0f)
        seconds = intent.getIntExtra("sec", 0)
        time = intent.getStringExtra("time")

        if (checkSteps == 0f) {
            val current = intent.getStringExtra("current")
            val c = current!!.toFloat()
            previousTotalSteps = c
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
                                Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs
                            )
                            if (running) {
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

        if (mediaPlayer == null) {
            fetchAndPlayMusic()
        } else {
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
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

            val newSteps = totalSteps - previousTotalSteps
            val correctedSteps = newSteps * (18f / 3f)
            totalSteps = previousTotalSteps + correctedSteps
            previousTotalSteps = totalSteps

            distance = 75 * totalSteps / 10000
            calories = (0.04 * totalSteps).toFloat()

            saveData()
            showNotification()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}

    fun showNotification() {
        val notificationLayout = RemoteViews(packageName, R.layout.noti_layout)
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_layout)
        notificationLayoutExpanded.setTextViewText(R.id.steps_notification, totalSteps.toString())
        notificationLayout.setTextViewText(R.id.steps_small, totalSteps.toString())
        notificationLayoutExpanded.setTextViewText(R.id.calories_large, DecimalFormat("#.###").format(calories))
        notificationLayout.setTextViewText(R.id.calories_small, DecimalFormat("#.###").format(calories))
        notificationLayout.setTextViewText(R.id.distance_small, DecimalFormat("#.###").format(distance))
        notificationLayoutExpanded.setTextViewText(R.id.distance_large, DecimalFormat("#.###").format(distance))
        notificationLayoutExpanded.setTextViewText(R.id.time_large, time)
        notificationLayout.setTextViewText(R.id.time_small, time)

        val CHANNELID = "Foreground Service ID"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNELID, CHANNELID, NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            val notification = Notification.Builder(this, CHANNELID)
                .setSmallIcon(R.drawable.ic_play)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setSmallIcon(R.drawable.ic_launcher_background)
            startForeground(1001, notification.build())
        }
    }

    private fun saveTime() {
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

    private fun fetchAndPlayMusic() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(MusicApiService::class.java)
        val call = service.getTrack()

        Log.d("MusicDebug", "Calling mock API for music...")

        call.enqueue(object : Callback<MusicTrack> {
            override fun onResponse(call: Call<MusicTrack>, response: Response<MusicTrack>) {
                val track = response.body()
                if (track?.track_url != null) {
                    Log.d("MusicDebug", "Track received: ${track.title} by ${track.artist}")
                    playMusic(track.track_url)
                } else {
                    Log.e("MusicDebug", "Track URL is null in response.")
                }
            }

            override fun onFailure(call: Call<MusicTrack>, t: Throwable) {
                Log.e("MusicDebug", "API call failed: ${t.message}")
            }
        })
    }

    private fun playMusic(url: String) {
        Log.d("MusicDebug", "Preparing to play music from: $url")
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            isLooping = true
            prepareAsync()
            setOnPreparedListener {
                it.start()
            }
            setOnErrorListener { mp, what, extra ->
                Log.e("MediaPlayerError", "what=$what, extra=$extra")
                true
            }
        }
    }
}

