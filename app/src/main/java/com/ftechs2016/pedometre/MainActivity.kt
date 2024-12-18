package com.ftechs2016.pedometre

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
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
    var load = false
    private var seconds = 0
    private var totalTime: String? = "00:00"
    private lateinit var reports: ImageView
    private lateinit var animationView2: LottieAnimationView
    private lateinit var animationView: LottieAnimationView

    //private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    //ImageView language;
    var context: Context? = null
    var nextDay = false
    private val mInterstitialAdPlay: InterstitialAd? = null
    private val mInterstitialAdStop: InterstitialAd? = null
    private var reportViewModel: ReportViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!checkPermission()) {
            requestPermission()
        }
        play = findViewById(R.id.play)
        steps = findViewById(R.id.steps)
        stop = findViewById(R.id.stop)
        reports = findViewById(R.id.reports)
        calories = findViewById(R.id.calories)
        distance = findViewById(R.id.distance)
        time = findViewById(R.id.time)
        animationView2 = findViewById(R.id.animationView2)
        animationView = findViewById(R.id.animationView)
        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]
        loadData()
        val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
        val run = sharedPreferences.getBoolean("running", false)
        if (run) {
            play.visibility = View.GONE
            stop.visibility = View.VISIBLE
            animationView2.playAnimation()
            animationView.playAnimation()
        } else {
            play.visibility = View.VISIBLE
            stop.visibility = View.GONE
            animationView2.pauseAnimation()
            animationView.pauseAnimation()
        }
        play.setOnClickListener(View.OnClickListener {
            play.visibility = View.GONE
            stop.visibility = View.VISIBLE
            animationView2.playAnimation()
            animationView.playAnimation()
            val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("running", true).apply()
            if (!nextDay) {
                val serviceIntent = Intent(this@MainActivity, BackGroundService::class.java)
                serviceIntent.putExtra("sensorValue", 0)
                serviceIntent.putExtra("current", steps.text.toString())
                serviceIntent.putExtra("sec", seconds)
                serviceIntent.putExtra("time", totalTime)
                startService(serviceIntent)
            } else {
                val v = sharedPreferences.getFloat("sensorValue", 0f)
                val serviceIntent = Intent(this@MainActivity, BackGroundService::class.java)
                serviceIntent.putExtra("sensorValue", v)
                serviceIntent.putExtra("sec", seconds)
                serviceIntent.putExtra("time", "00:00")
                startService(serviceIntent)
            }
        })
        reports.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@MainActivity, ReportActivity::class.java)
            )
        })
        stop.setOnClickListener(View.OnClickListener {
            val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("running", false).apply()
            play.visibility = View.VISIBLE
            stop.visibility = View.GONE
            animationView2.pauseAnimation()
            animationView.pauseAnimation()
            loadData()
            stopService(Intent(this@MainActivity, BackGroundService::class.java))
        })
        Thread {
            while (true) {
                runOnUiThread { loadData() }
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    @SuppressLint("SetTextI18n")
    fun loadData() {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val formattedDate = df.format(c)
        val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
        val date = sharedPreferences.getString("date", null)
        if (date == formattedDate) {
            nextDay = false
            val s = sharedPreferences.getFloat("key1", 0f)
            val dis = sharedPreferences.getFloat("distance", 0f)
            val cal = sharedPreferences.getFloat("calories", 0f)
            seconds = sharedPreferences.getInt("time", 0)
            totalTime = sharedPreferences.getString("totalTime", null)
            steps.text = s.toString() + ""
            distance!!.text = dis.toString() + ""
            calories!!.text = cal.toString() + ""
            time!!.text = totalTime
        } else {
            nextDay = true
            val s = sharedPreferences.getFloat("key1", 0f)
            val dis = sharedPreferences.getFloat("distance", 0f)
            val cal = sharedPreferences.getFloat("calories", 0f)
            val t = sharedPreferences.getString("totalTime", null)
            seconds = 0
            val newReport = ReportModel(date, s.toString(), dis.toString(), cal.toString(), t, null)
            reportViewModel!!.insert(newReport)
            steps.text = 0.toString() + ""
            distance!!.text = 0.toString() + ""
            calories!!.text = 0.toString() + ""
            time!!.text = "0h 50m"
            val sharedPreferences2 = getSharedPreferences("steps", MODE_PRIVATE)
            sharedPreferences2.edit().putString("date", formattedDate).apply()
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
        KcalText.setText(R.string.Kcal)
        kmText.setText(R.string.km)
        stepText.setText(R.string.steps)
        super.onConfigurationChanged(newConfig)
    }

    private fun checkPermission(): Boolean {
        val permission1 =
            ContextCompat.checkSelfPermission(applicationContext, permission.ACTIVITY_RECOGNITION)
        val permission2 =
            ContextCompat.checkSelfPermission(applicationContext, permission.FOREGROUND_SERVICE)
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission.ACTIVITY_RECOGNITION, permission.FOREGROUND_SERVICE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                val writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show()
                } else {
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