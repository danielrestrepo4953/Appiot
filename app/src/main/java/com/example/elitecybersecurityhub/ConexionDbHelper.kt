package com.example.elitecybersecurityhub

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ConexionDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    val sql = "CREATE TABLE USUARIOS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NOMBRE TEXT, APELLIDO TEXT, EMAIL TEXT, CLAVE TEXT, SINCRONIZADO INTEGER DEFAULT 0)";
    override fun onCreate(db: SQLiteDatabase) {
        // Aquí va tu SQL para crear tablas
        db.execSQL(sql)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Aquí puedes actualizar tu esquema
        db.execSQL("DROP TABLE USUARIOS")
        onCreate(db)
    }
    companion object {
        private const val DATABASE_NAME = "CRUD"
        private const val DATABASE_VERSION = 3
    }
}