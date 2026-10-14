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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class Usuariocrud : AppCompatActivity() {
    private lateinit var editarid: EditText
    private lateinit var editarNombre: EditText
    private lateinit var editarApellido: EditText
    private lateinit var editarCorreo: EditText
    private lateinit var modificar: Button
    private lateinit var eliminar: Button
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_usuariocrud)

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
        requestQueue = Volley.newRequestQueue(this)

        val idusu = intent.getIntExtra("Id", 0)
        val nombreusu = intent.getStringExtra("Nombre") ?: ""
        val apellidousu = intent.getStringExtra("Apellido") ?: ""
        val emailusu = intent.getStringExtra("Email") ?: ""

        editarid.setText(idusu.toString())
        editarid.isEnabled = false // El ID no debe editarse
        editarNombre.setText(nombreusu)
        editarApellido.setText(apellidousu)
        editarCorreo.setText(emailusu)

        modificar.setOnClickListener {
            val nuevoNombre = editarNombre.text.toString().trim()
            val nuevoApellido = editarApellido.text.toString().trim()
            val nuevoCorreo = editarCorreo.text.toString().trim()

            if (!validarCampos(nuevoNombre, nuevoApellido, nuevoCorreo)) return@setOnClickListener

            // 1. Validar en SQLite local que el correo no pertenezca a OTRO usuario
            if (correoExisteEnOtroLocal(idusu, nuevoCorreo)) {
                mostrarAlertaError("Correo Duplicado", "Este correo ya está registrado por otro usuario en este dispositivo.")
                return@setOnClickListener
            }

            // 2. Si hay internet, validamos también en el servidor EC2
            if (hayInternet()) {
                val urlVerificar = "http://100.26.70.56/verificar_correo.php?email=$nuevoCorreo"
                val reqVerificar = JsonObjectRequest(Request.Method.GET, urlVerificar, null,
                    { response ->
                        val estadoVerif = response.optBoolean("estado", true)
                        // Si estado es false (ya existe), verificamos si es de otro usuario en la nube
                        if (!estadoVerif && nuevoCorreo != emailusu) {
                            mostrarAlertaError("Correo en Uso", "El correo $nuevoCorreo ya pertenece a otro usuario en la nube.")
                        } else {
                            // Guardar localmente como pendiente de actualizar (SINCRONIZADO = 0)
                            actualizarLocalmente(idusu, nuevoNombre, nuevoApellido, nuevoCorreo,emailusu, sincronizado = 2)
                            sincronizarModificacionEnServidor(idusu, emailusu, nuevoNombre, nuevoApellido, nuevoCorreo)
                        }
                    },
                    {
                        // Si falla la red, guardamos offline de todas formas
                        actualizarLocalmente(idusu, nuevoNombre, nuevoApellido, nuevoCorreo,emailusu, sincronizado = 2)
                        mostrarExito("Guardado offline", "Sin conexión. Los cambios se actualizarán al recuperar internet.")
                    }
                )
                requestQueue.add(reqVerificar)
            } else {
                // Sin internet: Guardar localmente con SINCRONIZADO = 0
                actualizarLocalmente(idusu, nuevoNombre, nuevoApellido, nuevoCorreo,emailusu, sincronizado = 2)
                mostrarExito("Guardado Local", "Cambios guardados localmente. Se sincronizarán al reconectar.")
            }
        }

        eliminar.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Estás seguro?")
                .setContentText("Esta acción eliminará el usuario.")
                .setConfirmText("Sí, eliminar")
                .setCancelText("Cancelar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()

                    // Marcar como pendiente de eliminar en la nube (SINCRONIZADO = -1)
                    marcarPendienteEliminar(idusu, emailusu)
                }
                .show()
        }
    }

    private fun hayInternet(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun correoExisteEnOtroLocal(idActual: Int, email: String): Boolean {
        val helper = ConexionDbHelper(this)
        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT ID FROM USUARIOS WHERE EMAIL = '$email' AND ID != $idActual", null)
        val existe = cursor.moveToFirst()
        cursor.close()
        db.close()
        return existe
    }

    private fun actualizarLocalmente(id: Int, nombre: String, apellido: String, emailNuevo: String, emailOriginal: String, sincronizado: Int) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        val values = ContentValues().apply {
            put("NOMBRE", nombre)
            put("APELLIDO", apellido)
            put("EMAIL", emailNuevo)
            put("EMAIL_ORIGINAL", emailOriginal) // Guardamos el correo original para rastrearlo en la nube
            put("SINCRONIZADO", sincronizado)
        }
        db.update("USUARIOS", values, "ID = ?", arrayOf(id.toString()))
        db.close()
    }

    private fun marcarPendienteEliminar(id: Int, email: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase

        if (hayInternet()) {
            // Si hay internet, podemos eliminar directamente en EC2 y luego en SQLite
            val urlEliminar = "http://100.26.70.56/eliminar_usuario.php?email=$email"
            val reqEliminar = JsonObjectRequest(Request.Method.GET, urlEliminar, null,
                { _ ->
                    db.delete("USUARIOS", "ID = ?", arrayOf(id.toString()))
                    db.close()
                    mostrarExito("Eliminado", "Usuario eliminado correctamente.")
                },
                {
                    // Si falla la red, lo marcamos con SINCRONIZADO = -1 para borrarlo después
                    db.execSQL("UPDATE USUARIOS SET SINCRONIZADO = -1 WHERE ID = $id")
                    db.close()
                    mostrarExito("Guardado offline", "Sin conexión. Se eliminará de la nube al reconectar.")
                }
            )
            requestQueue.add(reqEliminar)
        } else {
            // Sin internet: Marcamos con SINCRONIZADO = -1 (Pendiente de eliminar)
            db.execSQL("UPDATE USUARIOS SET SINCRONIZADO = -1 WHERE ID = $id")
            db.close()
            mostrarExito("Guardado offline", "Marcado para eliminación. Se procesará al reconectar.")
        }
    }
    private fun sincronizarModificacionEnServidor(id: Int, emailOriginal: String, nombre: String, apellido: String, emailNuevo: String) {
        val urlModificar = "http://100.26.70.56/modificar_usuario.php?email_antiguo=$emailOriginal&nombre=$nombre&apellido=$apellido&nuevo_email=$emailNuevo"

        val request = JsonObjectRequest(Request.Method.GET, urlModificar, null,
            { response ->
                val estado = response.optBoolean("estado", false)
                if (estado) {
                    actualizarLocalmente(id, nombre, apellido, emailNuevo, emailOriginal, sincronizado = 1)
                    mostrarExito("¡Éxito!", "Usuario modificado y sincronizado correctamente.")
                } else {
                    mostrarAlertaError("Error", "No se pudo actualizar en el servidor.")
                }
            },
            {
                mostrarExito("Guardado", "Modificado localmente. Se sincronizará al haber red.")
            }
        )
        requestQueue.add(request)
    }


    private fun validarCampos(nombre: String, apellido: String, email: String): Boolean {
        val regexEmail = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        val regexNombre = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")

        if (nombre.isEmpty() || !nombre.matches(regexNombre)) {
            editarNombre.setError("Nombre inválido (solo letras)")
            return false
        }
        if (apellido.isEmpty() || !apellido.matches(regexNombre)) {
            editarApellido.setError("Apellido inválido (solo letras)")
            return false
        }
        if (email.isEmpty() || !regexEmail.matches(email)) {
            editarCorreo.setError("Correo electrónico inválido")
            return false
        }
        return true
    }

    private fun mostrarAlertaError(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Cerrar")
            .show()
    }

    private fun mostrarExito(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                startActivity(Intent(this, Listausuarios::class.java))
            }
            .show()
    }
}