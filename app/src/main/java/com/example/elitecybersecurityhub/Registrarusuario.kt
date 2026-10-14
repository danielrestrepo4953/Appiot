package com.example.elitecybersecurityhub

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

private lateinit var nombrereg: EditText
private lateinit var apellidoreg: EditText
private lateinit var correoreg: EditText
private lateinit var contrasenareg: EditText
private lateinit var confirmarcontrareg: EditText
private lateinit var botonregistro: Button
private lateinit var requestQueue: RequestQueue

class Registrarusuario : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrarusuario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nombrereg = findViewById(R.id.nombres2)
        apellidoreg = findViewById(R.id.apellidos2)
        correoreg = findViewById(R.id.correoregistrar2)
        contrasenareg = findViewById(R.id.contrasenaregistrar2)
        confirmarcontrareg = findViewById(R.id.confirmarcontrasena2)
        botonregistro = findViewById(R.id.registrobtn)
        requestQueue = Volley.newRequestQueue(this)

        // Botón Registrar
        botonregistro.setOnClickListener {
            // ... (Tus validaciones actuales de campos vacíos y contraseñas seguras) ...

            val nom = nombrereg.text.toString().trim()
            val ape = apellidoreg.text.toString().trim()
            val mai = correoreg.text.toString().trim()
            val cla = contrasenareg.text.toString().trim()

            // 1. Guardar SIEMPRE en SQLite local primero (con SINCRONIZADO = 0)
            guardarLocalmente(nom, ape, mai, cla, sincronizado = 0)

            // 2. Verificar si hay internet para subir a EC2
            if (hayInternet()) {
                sincronizarUsuarioEnEC2(nom, ape, mai, cla) { exito, mensaje ->
                    if (exito) {
                        // Actualizar estado local a SINCRONIZADO = 1
                        marcarComoSincronizado(mai)
                        mostrarExitoYSalir()
                    } else {
                        // El correo ya existía en EC2 o falló
                        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Aviso")
                            .setContentText(mensaje)
                            .setConfirmText("Aceptar")
                            .show()
                    }
                }
            } else {
                // Sin internet: Notificar al usuario que se guardó localmente
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Guardado Localmente")
                    .setContentText("No hay conexión a internet. Los datos se han guardado en el dispositivo y se subirán al recuperar la conexión.")
                    .setConfirmText("Aceptar")
                    .setConfirmClickListener { dialog ->
                        dialog.dismissWithAnimation()
                        startActivity(Intent(this, Listausuarios::class.java))
                    }
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Al volver a entrar a la actividad, si hay internet, sincronizamos pendientes
        if (hayInternet()) {
            sincronizarPendientesLocales()
        }
    }

    private fun hayInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun guardarLocalmente(nom: String, ape: String, mai: String, cla: String, sincronizado: Int) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        try {
            val datos = ContentValues().apply {
                put("NOMBRE", nom)
                put("APELLIDO", ape)
                put("EMAIL", mai)
                put("CLAVE", cla)
                put("SINCRONIZADO", sincronizado)
            }
            db.insertOrThrow("USUARIOS", null, datos)
        } catch (e: Exception) {
            // Si ya existe en SQLite local por clave primaria o correo, puedes manejarlo aquí
            e.printStackTrace()
        }
    }

    private fun marcarComoSincronizado(email: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        db.execSQL("UPDATE USUARIOS SET SINCRONIZADO = 1 WHERE EMAIL = '$email'")
        db.close()
    }

    private fun sincronizarUsuarioEnEC2(nom: String, ape: String, mai: String, cla: String, callback: (Boolean, String) -> Unit) {
        // Paso A: Verificar si el correo ya existe en EC2 usando verificar_correo.php
        val urlVerificar = "http://100.26.70.56/verificar_correo.php?email=$mai"

        val reqVerificar = StringRequest(Request.Method.GET, urlVerificar,
            { response ->
                // Suponiendo que tu PHP devuelve "false" si ya existe, y "true" si está libre
                if (response.trim().lowercase() == "false") {
                    callback(false, "El correo $mai ya se encuentra registrado en el servidor.")
                } else {
                    // Paso B: Registrar en EC2 usando registrar_usuario.php
                    val urlRegistrar = "http://100.26.70.56/registrar_usuario.php?email=$mai&nombre=$nom&apellido=$ape&password=$cla"
                    val reqRegistrar = StringRequest(Request.Method.GET, urlRegistrar,
                        { resReg ->
                            if (resReg.trim().lowercase() == "true") {
                                callback(true, "Registrado con éxito")
                            } else {
                                callback(false, "Error al registrar en el servidor.")
                            }
                        },
                        { err -> callback(false, "Error de red: ${err.message}") }
                    )
                    requestQueue.add(reqRegistrar)
                }
            },
            { err -> callback(false, "Error de red al verificar correo: ${err.message}") }
        )
        requestQueue.add(reqVerificar)
    }

    private fun sincronizarPendientesLocales() {
        val helper = ConexionDbHelper(this)
        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM USUARIOS WHERE SINCRONIZADO = 0", null)

        val correosConflictivos = mutableListOf<String>()

        if (cursor.moveToFirst()) {
            do {
                val nom = cursor.getString(cursor.getColumnIndexOrThrow("NOMBRE"))
                val ape = cursor.getString(cursor.getColumnIndexOrThrow("APELLIDO"))
                val mai = cursor.getString(cursor.getColumnIndexOrThrow("EMAIL"))
                val cla = cursor.getString(cursor.getColumnIndexOrThrow("CLAVE"))

                // Verificamos en segundo plano con EC2
                sincronizarUsuarioEnEC2(nom, ape, mai, cla) { exito, mensaje ->
                    if (exito) {
                        marcarComoSincronizado(mai)
                    } else {
                        // Si el servidor dice que ya existe, anotamos el conflicto
                        if (mensaje.contains("ya se encuentra registrado")) {
                            correosConflictivos.add(mai)
                            // Marcamos como sincronizado localmente para evitar bucles infinitos de sincronización de un correo duplicado
                            marcarComoSincronizado(mai)
                        }
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        // Si hubo conflictos con correos existentes en la nube, notificamos al usuario
        if (correosConflictivos.isNotEmpty()) {
            val listaStr = correosConflictivos.joinToString(", ")
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Conflictos de Sincronización")
                .setContentText("Intentaste registrar uno o más correos offline que ya existían en el servidor y no pudieron subirse:\n$listaStr")
                .setConfirmText("Entendido")
                .show()
        }
    }

    private fun mostrarExitoYSalir() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Sincronizando..."
        pDialog.setCancelable(false)
        pDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
            pDialog.titleText = "¡Éxito!"
            pDialog.contentText = "Usuario registrado y sincronizado en la nube."
            pDialog.confirmText = "Aceptar"
            pDialog.setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                startActivity(Intent(this, Listausuarios::class.java))
            }
        }, 2000)
    }
}