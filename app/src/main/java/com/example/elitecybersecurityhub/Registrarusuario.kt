package com.example.elitecybersecurityhub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

private lateinit var nombrereg: EditText
private lateinit var apellidoreg: EditText
private lateinit var correoreg: EditText
private lateinit var contrasenareg: EditText
private lateinit var confirmarcontrareg: EditText

private lateinit var botonregistro: Button

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

        fun compararValores(valor1: String, valor2: String): Boolean {
            return valor1 == valor2
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

        fun nombreVacioError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("El nombre no puede estar vacío")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun apellidoVacioError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("El apellido no puede estar vacío")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun correoVacioError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("El correo no puede estar vacío")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun correoFormatoError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("El correo no tiene formato correcto.")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
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

        botonregistro.setOnClickListener {
            var esValido = true
            if(nombrereg.text.toString().isEmpty()){
                esValido = false
                nombrereg.setError("El nombre no puede estar vacío.")
            }
            if (apellidoreg.text.toString().isEmpty()){
                esValido = false
                apellidoreg.setError("El apellido no puede estár vacío.")
            }

            if (correoreg.text.toString().isEmpty()) {
                esValido = false
                correoreg.setError("El correo no puede estár vacío.")
            }

            else if (!validarEmail(correoreg.text.toString().trim())) {
                esValido = false
                correoreg.setError("El formato del correo no es valido.")
            }

            if (contrasenareg.text.toString().isEmpty()) {
                esValido = false
                contrasenareg.setError("Debe ingresar una contraseña.")
            }

            else if (!validarContrasena(contrasenareg.text.toString().trim())){

                esValido = false
                contrasenareg.setError("La contraseña debe tener una mayuscula, una minuscula, un numero, un caracter especial, y al menos ocho caracteres.")
            }

            if (confirmarcontrareg.text.toString().isEmpty()){
                esValido = false
                confirmarcontrareg.setError("Debe confirmar la contraseña.")
            }

            else if (!compararValores(contrasenareg.text.toString().trim(), confirmarcontrareg.text.toString().trim())){
                esValido = false
                confirmarcontrareg.setError(("Las contraseñas no coinciden."))

            }



            if (!esValido){
                if(nombrereg.text.toString().isEmpty()){
                    nombreVacioError()
                }

                else if (apellidoreg.text.toString().isEmpty()){
                    apellidoVacioError()
                }

                else if (correoreg.text.toString().isEmpty()) {
                    correoVacioError()
                }

                else if (!validarEmail(correoreg.text.toString().trim())) {
                    correoFormatoError()
                }

                else if (contrasenareg.text.toString().isEmpty()) {
                    contrasenaVacioError()
                }

                else if (!validarContrasena(contrasenareg.text.toString().trim())){
                    contrasenaDebilError()
                }

                else if (confirmarcontrareg.text.toString().isEmpty()){
                    confirmarContrasenaError()

                }

                else if (!compararValores(contrasenareg.text.toString().trim(), confirmarcontrareg.text.toString().trim())){
                    contrasenaNoCoincideError()

                }



            }
            else{
                procesoConCargandoYExito()
            }
        }
    }


}