package com.ftechs2016.pedometre

class ReportModel {
    @kotlin.jvm.JvmField
    var date: String? = null
    var steps: String? = null
    var distance: String? = null
    var calories: String? = null
    var time: String? = null
    var extra: String? = null

    constructor()
    constructor(
        date: String?,
        steps: String?,
        distance: String?,
        calories: String?,
        time: String?,
        extra: String?
    ) {
        this.date = date
        this.steps = steps
        this.distance = distance
        this.calories = calories
        this.time = time
        this.extra = extra
    }
}
