package com.ftechs2016.pedometre

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ftechs2016.pedometre.MainActivity
import java.util.Locale

class OnBoarding : AppCompatActivity() {
    lateinit var language: RelativeLayout
    lateinit var gender: RelativeLayout

    //Spinner languageSelect;
    var lang = "null"
    lateinit var male: CardView
    lateinit var female: CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)
        language = findViewById(R.id.language_layout)
        gender = findViewById(R.id.genderLayout)
        //languageSelect = findViewById(R.id.languageSelect);
        male = findViewById(R.id.male)
        female = findViewById(R.id.female)


//        languageSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                lang = parent.getItemAtPosition(position).toString();
//                if (lang.equals("English (US)")){
//                    setLocale("en");
//                } else if (lang.equals("Arabic (AR)")) {
//                    setLocale("ar");
//                }
//            }@Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
        male.setOnClickListener(View.OnClickListener {
            male.setCardBackgroundColor(resources.getColor(R.color.appColor))
            female.setCardBackgroundColor(Color.WHITE)
        })
        female.setOnClickListener(View.OnClickListener {
            male.setCardBackgroundColor(Color.WHITE)
            female.setCardBackgroundColor(resources.getColor(R.color.appColor))
        })
        findViewById<View>(R.id.startNow).setOnClickListener {
            val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("logIn", true).apply()
            startActivity(Intent(this@OnBoarding, MainActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.gotoNext_GenderSelect).setOnClickListener {
            if (lang == "null") {
                val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
                sharedPreferences.edit().putString("lang", "en").apply()
                language.visibility = View.GONE
                gender.visibility = View.VISIBLE
            } else if (lang == "English (US)") {
                val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
                sharedPreferences.edit().putString("lang", "en").apply()
                language.visibility = View.GONE
                gender.visibility = View.VISIBLE
            } else if (lang == "Arabic (AR)") {
                val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
                sharedPreferences.edit().putString("lang", "ar").apply()
                language.visibility = View.GONE
                gender.visibility = View.VISIBLE
            } else {
                val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
                sharedPreferences.edit().putString("lang", "en").apply()
                language.visibility = View.GONE
                gender.visibility = View.VISIBLE
            }
        }
    }

    fun setLocale(lang: String?) {
        val myLocale = Locale(lang)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
        onConfigurationChanged(conf)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val next: TextView = findViewById(R.id.next)
        val selectGender: TextView = findViewById(R.id.title1)
        val selectAge: TextView = findViewById(R.id.title3)
        val start: TextView = findViewById(R.id.start)
        start.setText(R.string.start)
        selectAge.setText(R.string.select_age)
        selectGender.setText(R.string.select_gender)
        next.setText(R.string.next)
        super.onConfigurationChanged(newConfig)
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences = getSharedPreferences("steps", MODE_PRIVATE)
        val check = sharedPreferences.getBoolean("logIn", false)
        if (check) {
            startActivity(Intent(this@OnBoarding, MainActivity::class.java))
            finish()
        }
    }
}