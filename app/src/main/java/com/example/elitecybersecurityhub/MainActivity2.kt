package com.example.elitecybersecurityhub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.security.Principal
import kotlin.jvm.java

private lateinit var correologin: EditText
private lateinit var contralogin: EditText
private lateinit var loginingresarbtn: Button
private lateinit var registrologin: TextView
private lateinit var recuperar: TextView

private lateinit var dato: RequestQueue

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        correologin = findViewById(R.id.user_identificador)
        contralogin = findViewById(R.id.passlogin)
        loginingresarbtn = findViewById(R.id.loginbtn)
        registrologin = findViewById(R.id.registrarmebtn)
        recuperar = findViewById(R.id.recuperarbtn)
        dato = Volley.newRequestQueue(this)

        fun validarEmail(email: String): Boolean {
            val regex = Regex(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            )
            return regex.matches(email)
        }

        fun validarContrasena(password: String): Boolean {
            val regex = Regex(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_\\-])[A-Za-z\\d@$!%*?&.#_\\-]{8,}$"
            )
            return regex.matches(password)
        }

        fun mostrarError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("Error en los datos ingresados.")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun mostrarCargando()
        {
            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.titleText = "Cargando..."
            pDialog.setCancelable(false)
            pDialog.show()
            Handler(Looper.getMainLooper()).postDelayed({
                pDialog.dismissWithAnimation()
                val intent = Intent(this, Menuprincipal::class.java)
                startActivity(intent)

            }, 3000) // 3000 milisegundos = 3 segundos
        }

        fun contrasenavaciaError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("La contraseña no puede estar vacia. ")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun contrasenaInvalidaError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("La contraseña debe al menos tener una mayuscula, una minuscula, un numero, un caracter especial, y una longitud minima de 8 caracteres.  ")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun correovacioError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("El correo no puede estar vacio. ")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun correoinvalidoError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("El correo no cumple con el formato. ")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }
        fun mostrarErrorEstado() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("La cuenta ha sido deshabilitada.")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun consultarEstado(usu: String) {
            val url = "http://100.26.70.56/verificarestado.php?usu=$usu"
            val request = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    try {
                        val estado = response.getString("estado")
                        if (estado == "0" || estado.lowercase() == "false" || estado.toIntOrNull() == 0) {
                            mostrarErrorEstado()
                        } else {

                            mostrarCargando()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                { error ->
                    error.printStackTrace()
                }
            )
            dato.add(request)
        }

        fun consultarDatos(usu: String, pass: String) {
            val url = "http://100.26.70.56/apiconsultausu.php?usu=$usu&pass=$pass"
            val request = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    try {
                        val estado = response.getString("estado")
                        if (estado == "0" || estado.lowercase() == "false" || estado.toIntOrNull() == 0) {
                            mostrarError()
                        } else {

                            consultarEstado(usu)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                { error ->
                    error.printStackTrace()
                }
            )
            dato.add(request)
        }



        loginingresarbtn.setOnClickListener {
            var correcto = true
        if(correologin.text.toString().trim().isNotEmpty() && contralogin.text.toString().trim().isNotEmpty() &&
            validarEmail(correologin.text.toString().trim()) && validarContrasena(contralogin.text.toString().trim()) ){
            consultarDatos(correologin.text.toString().trim(), contralogin.text.toString().trim())

        }

        if (correologin.text.toString().isEmpty()) {
            correcto = false
            correologin.setError("El correo no debe estar vacio. ")
        }
        else if (!validarEmail(correologin.text.toString().trim())) {
            correcto = false
            correologin.setError("El correo no cumple con el formato correcto.")

        }

         if (contralogin.text.toString().isEmpty()) {
             correcto = false
             contralogin.setError("La contraseña no puede estar vacia. ")
         }

         else if (!validarContrasena(contralogin.text.toString().trim())) {
                correcto = false
             contralogin.setError("La contraseña debe al menos tener una mayuscula, una minuscula, un numero, un caracter especial, y una longitud minima de 8 caracteres.  ")
            }

            if (!correcto) {
                if (correologin.text.toString().isEmpty()) {
                    correovacioError()
                }
                else if (!validarEmail(correologin.text.toString().trim())) {
                correoinvalidoError()}

                else if (contralogin.text.toString().isEmpty()) {
                    contrasenavaciaError()
                }

                else if (!validarContrasena(contralogin.text.toString().trim())) {
                    contrasenaInvalidaError()
                }


                }

            }


        registrologin.setOnClickListener {

            val intent = Intent(this, Registrarusuario::class.java)
            startActivity(intent)
        }


        recuperar.setOnClickListener {
            val intent = Intent(this, RecuperarContrasena::class.java)
            startActivity(intent)
        }


    }

}