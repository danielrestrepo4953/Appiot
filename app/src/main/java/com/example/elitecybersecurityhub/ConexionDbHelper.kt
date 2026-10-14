package com.example.elitecybersecurityhub

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ConexionDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    val sql = "CREATE TABLE USUARIOS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NOMBRE TEXT, APELLIDO TEXT, EMAIL TEXT UNIQUE, EMAIL_ORIGINAL TEXT, CLAVE TEXT, SINCRONIZADO INTEGER DEFAULT 0)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS USUARIOS")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "CRUD"
        private const val DATABASE_VERSION = 5 // Incrementamos a versión 5
    }
}