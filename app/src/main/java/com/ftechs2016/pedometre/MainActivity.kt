package com.ftechs2016.pedometre

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.interstitial.InterstitialAd
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var steps: TextView
    private var distance: TextView? = null
    private var calories: TextView? = null
    var time: TextView? = null
    private lateinit var play: CardView
    private lateinit var stop: CardView
    private lateinit var reset: CardView
    private lateinit var reports: ImageView
    private lateinit var animationView2: LottieAnimationView
    private lateinit var animationView: LottieAnimationView

    private var seconds = 0
    private var totalTime: String? = "00:00"
    private var reportViewModel: ReportViewModel? = null
    private var nextDay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkPermission()) requestPermission()

        // Views
        play = findViewById(R.id.play)
        stop = findViewById(R.id.stop)
        reset = findViewById(R.id.reset)
        steps = findViewById(R.id.steps)
        distance = findViewById(R.id.distance)
        calories = findViewById(R.id.calories)
        time = findViewById(R.id.time)
        reports = findViewById(R.id.reports)
        animationView = findViewById(R.id.animationView)
        animationView2 = findViewById(R.id.animationView2)

        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]

        loadData()

        val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
        val run = sharedPreferences.getBoolean("running", false)

        if (run) {
            play.visibility = View.GONE
            stop.visibility = View.VISIBLE
            animationView.playAnimation()
            animationView2.playAnimation()
        } else {
            play.visibility = View.VISIBLE
            stop.visibility = View.GONE
            animationView.pauseAnimation()
            animationView2.pauseAnimation()
        }

        play.setOnClickListener {
            play.visibility = View.GONE
            stop.visibility = View.VISIBLE
            animationView.playAnimation()
            animationView2.playAnimation()

            sharedPreferences.edit().putBoolean("running", true).apply()

            val serviceIntent = Intent(this, BackGroundService::class.java)

            if (!nextDay) {
                serviceIntent.putExtra("sensorValue", 0)
                serviceIntent.putExtra("current", steps.text.toString())
                serviceIntent.putExtra("sec", seconds)
                serviceIntent.putExtra("time", totalTime)
            } else {
                val v = sharedPreferences.getFloat("sensorValue", 0f)
                serviceIntent.putExtra("sensorValue", v)
                serviceIntent.putExtra("sec", seconds)
                serviceIntent.putExtra("time", "00:00")
            }

            startService(serviceIntent)
        }

        stop.setOnClickListener {
            sharedPreferences.edit().putBoolean("running", false).apply()
            stopService(Intent(this, BackGroundService::class.java))
            play.visibility = View.VISIBLE
            stop.visibility = View.GONE
            animationView.pauseAnimation()
            animationView2.pauseAnimation()
            loadData()
        }

        reset.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putFloat("key1", 0f)
            editor.putFloat("distance", 0f)
            editor.putFloat("calories", 0f)
            editor.putInt("time", 0)
            editor.putString("totalTime", "00:00")
            editor.putFloat("sensorValue", 0f)
            editor.putBoolean("running", false)
            editor.apply()

            // Stop the background service
            stopService(Intent(this, BackGroundService::class.java))


            // Clear UI
            steps.text = "0"
            distance?.text = "0"
            calories?.text = "0"
            time?.text = "00:00"

            // Reset UI state
            stop.visibility = View.GONE
            play.visibility = View.VISIBLE

            // Stop animations cleanly
            animationView.cancelAnimation()
            animationView2.cancelAnimation()

            Toast.makeText(this, "All stats have been reset!", Toast.LENGTH_SHORT).show()
        }


        reports.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        Thread {
            while (true) {
                runOnUiThread { loadData() }
                Thread.sleep(2000)
            }
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        val calendar = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val today = formatter.format(calendar)

        val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
        val savedDate = sharedPreferences.getString("date", null)

        if (savedDate == today) {
            nextDay = false

            val s = sharedPreferences.getFloat("key1", 0f).toInt()
            val dis = sharedPreferences.getFloat("distance", 0f)
            val calKcal = sharedPreferences.getFloat("calories", 0f)
            seconds = sharedPreferences.getInt("time", 0)

            val minutes = seconds / 60
            val secondsOnly = seconds % 60
            totalTime = String.format("%02d:%02d", minutes, secondsOnly)

            val meters = dis.toInt()
            val cal = calKcal.toInt()

            steps.text = s.toString()
            distance?.text = meters.toString()
            calories?.text = cal.toString()
            time?.text = totalTime
        } else {
            nextDay = true

            val s = sharedPreferences.getFloat("key1", 0f).toInt()
            val dis = sharedPreferences.getFloat("distance", 0f)
            val calKcal = sharedPreferences.getFloat("calories", 0f)
            val t = sharedPreferences.getString("totalTime", null)
            seconds = 0

            val newReport = ReportModel(savedDate, s.toString(), dis.toString(), calKcal.toString(), t, null)
            reportViewModel?.insert(newReport)

            steps.text = "0"
            distance?.text = "0"
            calories?.text = "0"
            time?.text = "00:00"

            sharedPreferences.edit().putString("date", today).apply()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val stepText: TextView = findViewById(R.id.stepText)
        val kmText: TextView = findViewById(R.id.kmText)
        val KcalText: TextView = findViewById(R.id.KcalText)
        val timeText: TextView = findViewById(R.id.timeText)
        val appName: TextView = findViewById(R.id.appName)

        appName.setText(R.string.app_name)
        timeText.setText(R.string.Time)
        KcalText.setText(R.string.Cal) // updated from Kcal to Cal
        kmText.setText(R.string.meters) // updated from km to meters
        stepText.setText(R.string.steps)

        super.onConfigurationChanged(newConfig)
    }

    private fun checkPermission(): Boolean {
        val permission1 = ContextCompat.checkSelfPermission(this, permission.ACTIVITY_RECOGNITION)
        val permission2 = ContextCompat.checkSelfPermission(this, permission.FOREGROUND_SERVICE)
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission.ACTIVITY_RECOGNITION, permission.FOREGROUND_SERVICE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                val granted = grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
    }
}
