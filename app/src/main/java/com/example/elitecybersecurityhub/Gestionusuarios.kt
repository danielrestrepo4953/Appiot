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

private lateinit var registrarbtn: Button
private lateinit var listarbtn: Button

class Gestionusuarios : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestionusuarios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registrarbtn = findViewById(R.id.ingresarusuarios)
        listarbtn = findViewById(R.id.button4)



        fun mostrarCargando()
        {
            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.titleText = "Cargando..."
            pDialog.setCancelable(false)
            pDialog.show()
            Handler(Looper.getMainLooper()).postDelayed({
                pDialog.dismissWithAnimation()
                val intent = Intent(this, Registrarusuario::class.java)
                startActivity(intent)
            }, 3000) // 3000 milisegundos = 3 segundos
        }

        fun mostrarCargandoListar()
        {
            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.titleText = "Cargando..."
            pDialog.setCancelable(false)
            pDialog.show()
            Handler(Looper.getMainLooper()).postDelayed({
                pDialog.dismissWithAnimation()
                val intent = Intent(this, Registrarusuario::class.java)
                startActivity(intent)
            }, 3000) // 3000 milisegundos = 3 segundos
        }

        registrarbtn.setOnClickListener {
            val intent = Intent(this, Registrarusuario::class.java).apply {
                putExtra("origen", "gestion")
            }
            startActivity(intent)
        }

        listarbtn.setOnClickListener {
            val intent = Intent(this, Listausuarios::class.java)
            startActivity(intent)
        }
    }
}