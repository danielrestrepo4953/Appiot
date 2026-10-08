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

private lateinit var crearcontrasena: EditText
private lateinit var repetircontrasena: EditText
private lateinit var crearbtn: Button


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
                procesoConCargandoYExito()
            }
        }
    }


}