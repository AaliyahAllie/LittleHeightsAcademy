package com.example.littleheightsacademy

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "SchoolDB", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "firstName TEXT," +
                    "lastName TEXT," +
                    "address TEXT," +
                    "zip TEXT," +
                    "email TEXT," +
                    "dob TEXT," +
                    "activities TEXT," +
                    "status TEXT DEFAULT 'PENDING')"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS students")
        onCreate(db)
    }

    fun insertStudent(
        firstName: String,
        lastName: String,
        address: String,
        zip: String,
        email: String,
        dob: String,
        activities: String
    ) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("firstName", firstName)
        values.put("lastName", lastName)
        values.put("address", address)
        values.put("zip", zip)
        values.put("email", email)
        values.put("dob", dob)
        values.put("activities", activities)
        values.put("status", "PENDING")
        db.insert("students", null, values)
        db.close()
    }

    fun getStudentsByParentEmail(email: String): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM students WHERE email=?", arrayOf(email))
    }
}
