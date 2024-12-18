package com.ftechs2016.pedometre

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.MobileAds

class ReportActivity : AppCompatActivity() {
    lateinit var rv: RecyclerView
    var list: ArrayList<ReportModel>? = null
    var adapter: AdapterClass? = null
    lateinit var hide: TextView
    private var reportViewModel: ReportViewModel? = null
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        MobileAds.initialize(this) { initializationStatus ->
            Log.e(
                "initializationStatus",
                "initializationStatus$initializationStatus"
            )
        }
        hide = findViewById(R.id.hideLayout)
        rv = findViewById(R.id.recyclerView)

        //list = new ArrayList<>();
        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]
        reportViewModel!!.reportModel.observe(this) { reportModels: List<ReportModel> ->
            if (reportModels.isEmpty()) {
                hide.visibility = View.VISIBLE
                rv.visibility = View.GONE
            } else {
                hide.visibility = View.GONE
                rv.visibility = View.VISIBLE
                adapter = AdapterClass(reportModels, this)
                rv.layoutManager = LinearLayoutManager(this)
                rv.adapter = adapter
            }
        }


//        Cursor cursor = db.ReadAll();
//        while (cursor.moveToNext()){
//            if (cursor.getCount()>0){
//                ReportModel model = new ReportModel(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6));
////                if (model.getDate()!="15-Jan-2023"){
//                String date = model.getDate();
//                String steps = model.getSteps();
//                if  (!Objects.equals(steps,"0.0")){
//                    if (!Objects.equals(date, "15-Jan-2023")) {
//                        list.add(model);
//                    }
//                }
////                }
//            }
//            rv.setAdapter(adapter);
//            adapter.notifyDataSetChanged();
//
//            if (list.size()<0){
//                hide.setVisibility(View.GONE);
//                rv.setVisibility(View.VISIBLE);
//            }
//
//        }
        findViewById<View>(R.id.back).setOnClickListener { finish() }
    }
}