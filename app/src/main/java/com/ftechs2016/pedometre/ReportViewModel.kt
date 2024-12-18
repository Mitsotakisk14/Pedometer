package com.ftechs2016.pedometre

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SQLiteRepository
    val reportModel: LiveData<List<ReportModel>>

    init {
        repository = SQLiteRepository(application)
        reportModel = repository.reportModel
    }

    fun insert(reportModel: ReportModel?) {
        repository.insert(reportModel!!)
    }
}
