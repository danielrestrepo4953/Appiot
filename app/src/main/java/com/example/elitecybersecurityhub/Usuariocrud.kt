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

private lateinit var editarid: EditText
private lateinit var editarNombre: EditText
private lateinit var editarApellido: EditText
private lateinit var editarCorreo: EditText

private lateinit var modificar: Button

private lateinit var eliminar: Button

class Usuariocrud : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_usuariocrud)

        val nombre = intent.getStringExtra("nombreUsuario")
        val apellido = intent.getStringExtra("apellidoUsuario")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editarid = findViewById(R.id.idtext)
        editarNombre = findViewById(R.id.modnombre)
        editarApellido = findViewById(R.id.modapellido)
        editarCorreo = findViewById(R.id.modemail)
        modificar = findViewById(R.id.modbtn)
        eliminar = findViewById(R.id.eliminarbtn)

        val idusu = intent.getIntExtra("Id", 0)
        val nombreusu = intent.getStringExtra("Nombre") ?: ""
        val apellidousu = intent.getStringExtra("Apellido") ?: ""
        val emailusu = intent.getStringExtra("Email") ?: ""

        editarid.setText(idusu.toString())
        editarNombre.setText((nombreusu))
        editarApellido.setText(apellidousu)
        editarCorreo.setText((emailusu))

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
                    val intent = Intent(this, Listausuarios::class.java)
                    startActivity(intent)
                }
            }, 3000) // Espera 3 segundos simulando un proceso
        }

        fun mostrarExito() {
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("¡Éxito!")
                .setContentText("La operación fue realizada correctamente.")
                .setConfirmText("Aceptar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    val intent = Intent(this, Listausuarios::class.java)
                    startActivity(intent)
                }
                .show()
        }

        fun mostrarConfirmacion() {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Estás seguro?")
                .setContentText("Esta acción eliminará los datos.")
                .setConfirmText("Sí")
                .setCancelText("No")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    mostrarExito() // Aquí podrías ejecutar la acción real


                }
                .setCancelClickListener { dialog ->
                    dialog.dismissWithAnimation()
                }
                .show()
        }

        fun validarEmail(email: String): Boolean {
            val regex = Regex(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            )
            return regex.matches(email)
        }

        fun validarNombreApellido(texto: String): Boolean {

            val regex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")

            return texto.matches(regex)
        }

        fun modificarDatos(id: Int, nombre: String,
                              apellido: String, email: String) {
            val helper = ConexionDbHelper(this)
            val db = helper.writableDatabase
            val sql = "UPDATE USUARIOS SET NOMBRE='$nombre',APELLIDO='$apellido' ,EMAIL='$email' WHERE ID=$id"
            db.execSQL(sql)
            db.close()
        }

        fun eliminarDatos(id: Int) {
            val helper = ConexionDbHelper(this)
            val db = helper.writableDatabase
            val sql = "DELETE FROM USUARIOS WHERE ID=$id"
            db.execSQL(sql)
            db.close()
        }


        modificar.setOnClickListener {
            var esValido = true
            if(editarNombre.text.toString().isEmpty()){
                esValido = false
                editarNombre.setError("El nombre no puede estar vacío.")
            }

            else if (!validarNombreApellido(editarNombre.text.toString().trim())) {
                esValido = false
                editarNombre.setError("El nombre solo puede contener letras y espacios")
            }

            if (editarApellido.text.toString().isEmpty()){
                esValido = false
                editarApellido.setError("El apellido no puede estár vacío.")
            }

            else if (!validarNombreApellido(editarApellido.text.toString().trim())) {
                esValido = false
                editarApellido.setError("El apellido solo puede contener letras y espacios")
            }

            if (editarCorreo.text.toString().isEmpty()) {
                esValido = false
                editarCorreo.setError("El correo no puede estár vacío.")
            }

            else if (!validarEmail(editarCorreo.text.toString().trim())) {
                esValido = false
                editarCorreo.setError("El formato del correo no es valido.")
            }

            if (!esValido) {
            mostrarError()
            }

            else {
                modificarDatos(idusu, editarNombre.text.toString().trim(), editarApellido.text.toString().trim(), editarCorreo.text.toString().trim())
                procesoConCargandoYExito()

            }
        }

        eliminar.setOnClickListener {
            mostrarConfirmacion()
            eliminarDatos(idusu)
            mostrarExito()
        }

    }
}