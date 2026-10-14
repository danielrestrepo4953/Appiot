package com.example.elitecybersecurityhub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

private lateinit var crearcontrasena: EditText
private lateinit var repetircontrasena: EditText
private lateinit var crearbtn: Button

private lateinit var requestQueue: RequestQueue


class Crearcontrasena : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crearcontrasena)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        crearcontrasena = findViewById(R.id.crearcon)
        repetircontrasena = findViewById(R.id.repetircon)
        crearbtn = findViewById(R.id.crear)
        requestQueue = Volley.newRequestQueue(this)
        val usuarioemail = intent.getStringExtra("email_usuario") ?: ""





        fun validarContrasena(password: String): Boolean {
            val regex = Regex(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_\\-])[A-Za-z\\d@$!%*?&.#_\\-]{8,}$"
            )
            return regex.matches(password)
        }

        fun compararValores(valor1: String, valor2: String): Boolean {
            return valor1 == valor2
        }

        fun contrasenaVacioError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("La contraseña no puede estár vacía.")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun contrasenaDebilError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("La contraseña debe tener una mayuscula, una minuscula, un numero, un caracter especial, y una longitud minima de ocho caracteres. ")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun confirmarContrasenaError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("Debe confirmar la contraseña. ")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun contrasenaNoCoincideError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("Las contraseñas no coinciden. ")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun procesoConCargandoYExito() {
            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.titleText = "Cargando..."
            pDialog.setCancelable(false)
            pDialog.show()

            // Simular una operación (por ejemplo, guardar datos)
            Handler(Looper.getMainLooper()).postDelayed({
                pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                pDialog.titleText = "¡Éxito!"
                pDialog.contentText = "La operación se completó correctamente."
                pDialog.confirmText = "Aceptar"
                pDialog.setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    val intent = Intent(this, MainActivity2::class.java)
                    startActivity(intent)
                }
            }, 3000) // Espera 3 segundos simulando un proceso
        }

        crearbtn.setOnClickListener {
            var correcto = true
            if (crearcontrasena.text.toString().isEmpty()) {
                correcto = false
                crearcontrasena.setError("La contraseña no puede estár vacía.")
            }

            else if (!validarContrasena(crearcontrasena.text.toString().trim())) {
                correcto = false
                crearcontrasena.setError("La contraseña debe tener una mayuscula, una minuscula, un numero, un caracter especial, y una longitud de 8 caracteres.")
            }

            if (repetircontrasena.text.toString().isEmpty()) {
                correcto = false
                repetircontrasena.setError("Debe confirmar la contraseña.")
            }

            else if (!compararValores(crearcontrasena.text.toString().trim(),
                    repetircontrasena.text.toString().trim())) {
                correcto = false
                repetircontrasena.setError("Las contraseñas no coinciden. ")
            }

            if (!correcto) {
                if (crearcontrasena.text.toString().isEmpty()) {
                    contrasenaVacioError()
                }

                else if (!validarContrasena(crearcontrasena.text.toString().trim())) {
                    contrasenaDebilError() }

                else if (repetircontrasena.text.toString().isEmpty()) {
                    confirmarContrasenaError()
                }

                else if (!compararValores(crearcontrasena.text.toString().trim(),
                        repetircontrasena.text.toString().trim())) {
                    contrasenaNoCoincideError()
                }


            }

            else {

                val nuevaClave = crearcontrasena.text.toString().trim()

                // URL de tu backend en EC2
                val url = "http://100.26.70.56/actualizar_password.php?email=$usuarioemail&password=$nuevaClave"

                val stringRequest = StringRequest(
                    Request.Method.GET, url,
                    { response ->
                        if (response.contains("Contraseña actualizada correctamente")) {
                            // Si el servidor confirma el cambio, mostramos el diálogo de éxito
                            procesoConCargandoYExito()
                        } else {
                            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error")
                                .setContentText("Respuesta del servidor: $response")
                                .setConfirmText("Cerrar")
                                .show()
                        }
                    },
                    { error ->
                        error.printStackTrace()
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error de Conexión")
                            .setContentText("No se pudo conectar con el servidor: ${error.message}")
                            .setConfirmText("Cerrar")
                            .show()
                    }
                )

                requestQueue.add(stringRequest)
            }
        }

            }
        }



