package com.ftechs2016.pedometre

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLITEDATABASE(context: Context?) : SQLiteOpenHelper(context, dbname, null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val qry = "create table $lists (id integer primary key autoincrement, date text, steps text, distance text, calories text, time text, extra text)"
        db.execSQL(qry)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists $lists")
        onCreate(db)
    }

    fun insertData(
        date: String?,
        steps: String?,
        distance: String?,
        calories: String?,
        time: String?,
        extra: String?
    ): Boolean {
        val db = this.writableDatabase
        val c = ContentValues()
        c.put("date", date)
        c.put("steps", steps)
        c.put("distance", distance)
        c.put("calories", calories)
        c.put("time", time)
        c.put("extra", extra)
        val r = db.insert(lists, null, c)
        return r != -1L
    }

    fun ReadAll(): Cursor {
        val db = this.writableDatabase
        val qry = "select * from $lists"
        return db.rawQuery(qry, null)
    }

    companion object {
        const val dbname = "steps.db"
        const val lists = "steps"
    }
}
