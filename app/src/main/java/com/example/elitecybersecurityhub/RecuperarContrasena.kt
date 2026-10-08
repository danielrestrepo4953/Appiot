package com.example.elitecybersecurityhub

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.CountDownTimer
import android.graphics.Color
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog

private lateinit var codetext: TextView
private lateinit var btnver: Button
private lateinit var vercorreo: EditText

private lateinit var codver: EditText

private lateinit var volver: ImageView


class RecuperarContrasena : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_contrasena)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var timerActivo = false

        codetext = findViewById(R.id.textView7)
        btnver = findViewById(R.id.verificarbtn)
        vercorreo = findViewById(R.id.correover)
        codver = findViewById(R.id.codigo)
        volver = findViewById(R.id.retroceder)

        volver.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }

        fun iniciarTemporizador(txtEnviarCodigo: TextView) {

            if (timerActivo) return

            timerActivo = true

            txtEnviarCodigo.isEnabled = false
            txtEnviarCodigo.setTextColor(Color.GRAY)

            object : CountDownTimer(60000, 1000) {

                override fun onTick(millisUntilFinished: Long) {

                    val segundos = millisUntilFinished / 1000

                    txtEnviarCodigo.text =
                        "Reenviar código (${segundos}s)"
                }

                override fun onFinish() {

                    txtEnviarCodigo.text = "Enviar código"
                    txtEnviarCodigo.setTextColor(
                        Color.parseColor("#2196F3")
                    )

                    txtEnviarCodigo.isEnabled = true
                    timerActivo = false
                }

            }.start()
        }

        fun validarEmail(email: String): Boolean {
            val regex = Regex(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            )
            return regex.matches(email)
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

        fun codigoVacioError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("Ingrese el código de verificación")
                .setConfirmText("Cerrar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun codigoLargoError() {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText("El código de verificación debe tener 5 digitos de longitud")
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

        codetext.setOnClickListener {
            iniciarTemporizador(codetext)
        }

        btnver.setOnClickListener {
            var esValido = true

            if (vercorreo.text.toString().isEmpty()) {
                esValido = false
                vercorreo.setError("El correo no puede estár vacío.")

        }
            else if (!validarEmail(vercorreo.text.toString().trim())) {
                esValido = false
                vercorreo.setError("El formato del correo no es valido.")
            }

            if (codver.text.isEmpty()) {
                esValido = false
                codver.setError("Ingrese el código de verificación")
            }

            else if (codver.text.toString().length != 5) {
                esValido = false
                codver.setError("El código debe tener exactamente 5 digitos de longitud.")
            }

            if (!esValido){
                if (vercorreo.text.toString().isEmpty()) {
                    correoVacioError()
                }

                else if (!validarEmail(vercorreo.text.toString().trim())) {
                    correoFormatoError()
                }

                else if (codver.text.isEmpty()) {
                    codigoVacioError()
                }

                else if (codver.text.toString().length != 5) {
                    codigoLargoError()
                }
            }

            else {
                val intent = Intent(this, Crearcontrasena::class.java)
                startActivity(intent)
            }



    }
}}