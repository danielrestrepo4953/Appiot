package com.example.elitecybersecurityhub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlin.jvm.java

private lateinit var CRUDUserbtn: Button
private lateinit var Sensorbtn: Button

private lateinit var desarrolladorbtn: Button

private lateinit var salir: Button


class Menuprincipal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menuprincipal)


        CRUDUserbtn = findViewById(R.id.CRUDbtn)
        Sensorbtn = findViewById(R.id.sensordatabtn)
        desarrolladorbtn = findViewById(R.id.button6)
        salir = findViewById(R.id.button7)

        fun mostrarCargando()
        {
            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.titleText = "Cargando..."
            pDialog.setCancelable(false)
            pDialog.show()
            Handler(Looper.getMainLooper()).postDelayed({
                pDialog.dismissWithAnimation()
                val intent = Intent(this, Gestionusuarios::class.java)
                startActivity(intent)
            }, 3000) // 3000 milisegundos = 3 segundos
        }

        fun mostrarCargandoALogin()
        {
            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.titleText = "Cerrando sesión..."
            pDialog.setCancelable(false)
            pDialog.show()
            Handler(Looper.getMainLooper()).postDelayed({
                pDialog.dismissWithAnimation()
                val intent = Intent(this, MainActivity2::class.java)
                startActivity(intent)
            }, 3000) // 3000 milisegundos = 3 segundos
        }

        fun mostrarCargandoSensor()
        {
            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.titleText = "Cargando..."
            pDialog.setCancelable(false)
            pDialog.show()
            Handler(Looper.getMainLooper()).postDelayed({
                pDialog.dismissWithAnimation()
                val intent = Intent(this, Sensores::class.java)
                startActivity(intent)
            }, 3000) // 3000 milisegundos = 3 segundos
        }

        fun mostrarConfirmacion() {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Estás seguro?")
                .setContentText("Se va a cerrar la sesión.")
                .setConfirmText("Sí")
                .setCancelText("No")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                     mostrarCargandoALogin()// Aquí podrías ejecutar la acción real
                }
                .setCancelClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        CRUDUserbtn.setOnClickListener {
            mostrarCargando()

        }

        Sensorbtn.setOnClickListener {
            mostrarCargandoSensor()
        }

        desarrolladorbtn.setOnClickListener {
            val intent = Intent(this, Desarrollador::class.java)
            startActivity(intent)
        }

        salir.setOnClickListener {
            mostrarConfirmacion()
        }
    }


}