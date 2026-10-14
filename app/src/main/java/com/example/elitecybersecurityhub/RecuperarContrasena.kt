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
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest


private lateinit var codetext: TextView
private lateinit var btnver: Button
private lateinit var vercorreo: EditText

private lateinit var codver: EditText

private lateinit var volver: ImageView

private lateinit var requestQueue: RequestQueue


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
        requestQueue = Volley.newRequestQueue(this)

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
            val email = vercorreo.text.toString().trim()

            // 1. Validar que el campo no esté vacío y tenga formato correcto
            if (email.isEmpty()) {
                correoVacioError()
                vercorreo.setError("El correo no puede estar vacío")
                return@setOnClickListener
            }
            if (!validarEmail(email)) {
                correoFormatoError()
                vercorreo.setError("El formato del correo no es válido")
                return@setOnClickListener
            }

            // 2. URL de tu backend en la instancia EC2 (reemplaza con tu IP o dominio y nombre de tu script)
            val url = "http://100.26.70.56/enviar.php?email=$email"

            // 3. Crear la petición StringRequest (GET)
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    // Respuesta exitosa del servidor EC2
                    if (response.contains("Correo enviado correctamente")) {
                        // Si el correo se envió, iniciamos el temporizador de cuenta regresiva
                        iniciarTemporizador(codetext)

                        // Mostramos alerta de éxito
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("¡Éxito!")
                            .setContentText("Código enviado a $email")
                            .setConfirmText("Aceptar")
                            .show()
                    } else {
                        // Si el PHP devolvió otro mensaje indicando fallo
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Respuesta del servidor: $response")
                            .setConfirmText("Cerrar")
                            .show()
                    }
                },
                { error ->
                    // Error de red, servidor apagado, IP incorrecta, etc.
                    error.printStackTrace()
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error de Conexión")
                        .setContentText("No se pudo conectar con el servidor: ${error.message}")
                        .setConfirmText("Cerrar")
                        .show()
                }
            )

            // 4. Agregar la petición a la cola de Volley
            requestQueue.add(stringRequest)
        }

        btnver.setOnClickListener {
            val email = vercorreo.text.toString().trim()
            val codigoIngresado = codver.text.toString().trim()

            var esValido = true

            // Validaciones locales
            if (email.isEmpty()) {
                esValido = false
                vercorreo.setError("El correo no puede estar vacío.")
            } else if (!validarEmail(email)) {
                esValido = false
                vercorreo.setError("El formato del correo no es válido.")
            }

            if (codigoIngresado.isEmpty()) {
                esValido = false
                codver.setError("Ingrese el código de verificación.")
            } else if (codigoIngresado.length != 6) { // O 6 según lo que pidas
                esValido = false
                codver.setError("El código debe tener exactamente seis digitos.")
            }

            if (!esValido) {
                if (email.isEmpty()) correoVacioError()
                else if (!validarEmail(email)) correoFormatoError()
                else if (codigoIngresado.isEmpty()) codigoVacioError()
                else if (codigoIngresado.length != 6) codigoLargoError()
                return@setOnClickListener
            }

            // --- PETICIÓN VOLLEY PARA VERIFICAR EL CÓDIGO EN EL EC2 ---
            val url = "http://100.26.70.56/verificar_codigo.php?usu=$email&cod=$codigoIngresado"

            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    // Respuesta del servidor PHP en EC2
                    if (response.contains("Codigo correcto") || response.contains("estado\":\"1")) {
                        // Código válido: avanzamos a la pantalla de crear nueva contraseña
                        val intent = Intent(this, Crearcontrasena::class.java).apply {
                            putExtra("email_usuario", email) // Pasamos el correo para saber a quién actualizarle la contraseña
                        }
                        startActivity(intent)
                    } else {
                        // Código incorrecto o no coincide en la base de datos
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Código Incorrecto")
                            .setContentText("El código ingresado no es válido o ya expiró.")
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
}}