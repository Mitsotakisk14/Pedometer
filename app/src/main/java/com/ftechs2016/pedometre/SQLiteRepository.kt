package com.ftechs2016.pedometre

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SQLiteRepository(context: Context?) {
    private val dbHelper: SQLITEDATABASE
    private val reportModelLiveData = MutableLiveData<List<ReportModel>>()

    init {
        dbHelper = SQLITEDATABASE(context)
        loadMyEntities()
    }

    val reportModel: LiveData<List<ReportModel>>
        get() = reportModelLiveData

    fun insert(reportModel: ReportModel) {
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put("date", reportModel.date)
        values.put("steps", reportModel.steps)
        values.put("distance", reportModel.distance)
        values.put("calories", reportModel.calories)
        values.put("time", reportModel.time)
        values.put("extra", reportModel.extra)
        db.insert(SQLITEDATABASE.lists, null, values)
        db.close()
        // Reload data after insertion
        loadMyEntities()
    }

    private fun loadMyEntities() {
        val db = dbHelper.readableDatabase
        val reportModels: MutableList<ReportModel> = ArrayList()

        // Assuming your ReportModel has a constructor that takes values for date, steps, distance, etc.
        val columns = arrayOf("date", "steps", "distance", "calories", "time", "extra")
        val cursor = db.query(SQLITEDATABASE.lists, columns, null, null, null, null, null)
        for (columnName in cursor.columnNames) {
            Log.d("ColumnName", columnName)
        }
        while (cursor.moveToNext()) {
            @SuppressLint("Range") val date = cursor.getString(cursor.getColumnIndex("date"))
            @SuppressLint("Range") val steps = cursor.getString(cursor.getColumnIndex("steps"))
            @SuppressLint("Range") val distance =
                cursor.getString(cursor.getColumnIndex("distance"))
            @SuppressLint("Range") val calories =
                cursor.getString(cursor.getColumnIndex("calories"))
            @SuppressLint("Range") val time = cursor.getString(cursor.getColumnIndex("time"))
            @SuppressLint("Range") val extra = cursor.getString(cursor.getColumnIndex("extra"))
            val reportModel = ReportModel(date, steps, distance, calories, time, extra)
            reportModels.add(reportModel)
        }
        cursor.close()
        db.close()
        reportModelLiveData.postValue(reportModels)
    }
}
