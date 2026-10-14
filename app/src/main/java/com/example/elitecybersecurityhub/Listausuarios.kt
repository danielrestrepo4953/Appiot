package com.example.elitecybersecurityhub

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import java.text.Normalizer

class Listausuarios : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsuarioAdapter
    private lateinit var searchView: SearchView
    private lateinit var requestQueue: RequestQueue

    private val listaUsuarios = mutableListOf<Usuario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listausuarios)

        recyclerView = findViewById(R.id.rvUsuarios) // Asegúrate de que en tu XML este ID sea rvUsuarios
        searchView = findViewById(R.id.searchViewUsuarios)
        requestQueue = Volley.newRequestQueue(this)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = UsuarioAdapter(listaUsuarios) { usuario ->
            val intent = Intent(this, Usuariocrud::class.java).apply {
                putExtra("Id", usuario.id)
                putExtra("Nombre", usuario.nombre)
                putExtra("Apellido", usuario.apellido)
                putExtra("Email", usuario.email)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        configurarBuscador()
    }

    override fun onResume() {
        super.onResume()
        // 1. Cargamos la lista local de SQLite inmediatamente
        cargarUsuariosLocales()

        // 2. Si hay internet, intentamos sincronizar los pendientes
        if (hayInternet()) {
            sincronizarPendientesConEC2()
            descargarUsuariosDesdeEC2()
        }
    }

    private fun hayInternet(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun cargarUsuariosLocales() {
        listaUsuarios.clear()
        val helper = ConexionDbHelper(this)
        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM USUARIOS", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("NOMBRE"))
                val apellido = cursor.getString(cursor.getColumnIndexOrThrow("APELLIDO"))
                val email = cursor.getString(cursor.getColumnIndexOrThrow("EMAIL"))
                val sincronizado = cursor.getInt(cursor.getColumnIndexOrThrow("SINCRONIZADO"))

                listaUsuarios.add(Usuario(id, nombre, apellido, email, sincronizado))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        adapter.actualizarLista(listaUsuarios)
    }

    private fun sincronizarPendientesConEC2() {
        val helper = ConexionDbHelper(this)
        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM USUARIOS WHERE SINCRONIZADO = 0", null)

        if (cursor.moveToFirst()) {
            do {
                val nom = cursor.getString(cursor.getColumnIndexOrThrow("NOMBRE"))
                val ape = cursor.getString(cursor.getColumnIndexOrThrow("APELLIDO"))
                val mai = cursor.getString(cursor.getColumnIndexOrThrow("EMAIL"))
                val cla = cursor.getString(cursor.getColumnIndexOrThrow("CLAVE"))

                enviarAEc2(nom, ape, mai, cla)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
    }

    private fun descargarUsuariosDesdeEC2() {
        val url = "http://100.26.70.56/obtener_usuarios.php"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val estado = response.optBoolean("estado", false)
                if (estado) {
                    val jsonArray = response.optJSONArray("usuarios")
                    if (jsonArray != null) {
                        val helper = ConexionDbHelper(this)
                        val db = helper.writableDatabase

                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            val nombre = obj.optString("nombre", "")
                            val apellido = obj.optString("apellido", "")
                            val email = obj.optString("email", "")
                            val clave = obj.optString("clave", "")

                            // Preparamos los datos con SINCRONIZADO = 1 (ya que vienen del servidor)
                            val values = android.content.ContentValues().apply {
                                put("NOMBRE", nombre)
                                put("APELLIDO", apellido)
                                put("EMAIL", email)
                                put("CLAVE", clave)
                                put("SINCRONIZADO", 1)
                            }

                            // Insertamos o reemplazamos en SQLite local evitando duplicados gracias al UNIQUE
                            db.insertWithOnConflict(
                                "USUARIOS",
                                null,
                                values,
                                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                            )
                        }
                        db.close()

                        // Recargamos la lista en pantalla para reflejar los nuevos usuarios descargados
                        cargarUsuariosLocales()
                    }
                }
            },
            { err ->
                // Error de red silencioso al descargar (se reintentará al reabrir)
            }
        )
        requestQueue.add(request)
    }

    private fun enviarAEc2(nom: String, ape: String, mai: String, cla: String) {
        val urlVerificar = "http://100.26.70.56/verificar_correo.php?email=$mai"

        val reqVerificar = JsonObjectRequest(Request.Method.GET, urlVerificar, null,
            { response ->
                val estadoVerif = response.optBoolean("estado", true)
                if (!estadoVerif) {
                    // Conflicto: El correo ya existe en EC2
                    marcarComoSincronizado(mai) // Lo marcamos como procesado para no reintentar eternamente
                    mostrarAlertaError("Conflicto de sincronización", "El correo $mai guardado offline ya existe en el servidor y no pudo duplicarse.")
                    cargarUsuariosLocales()
                } else {
                    // Disponible: Registramos en EC2
                    val urlRegistrar = "http://100.26.70.56/registrar_usuario.php?email=$mai&nombre=$nom&apellido=$ape&password=$cla"
                    val reqRegistrar = JsonObjectRequest(Request.Method.GET, urlRegistrar, null,
                        { resReg ->
                            val estadoReg = resReg.optBoolean("estado", false)
                            if (estadoReg) {
                                marcarComoSincronizado(mai)
                                cargarUsuariosLocales()
                            } else {
                                val mensaje = resReg.optString("mensaje", "Error desconocido en servidor")
                                mostrarAlertaError("Error de Sincronización", "No se pudo subir el correo $mai: $mensaje")
                            }
                        },
                        { err -> /* Error de red silencioso o reintentable en siguiente inicio */ }
                    )
                    requestQueue.add(reqRegistrar)
                }
            },
            { err -> /* Error de red */ }
        )
        requestQueue.add(reqVerificar)
    }

    private fun marcarComoSincronizado(email: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        db.execSQL("UPDATE USUARIOS SET SINCRONIZADO = 1 WHERE EMAIL = '$email'")
        db.close()
    }

    private fun mostrarAlertaError(titulo: String, mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("Entendido")
            .show()
    }

    private fun configurarBuscador() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarUsuarios(newText ?: "")
                return true
            }
        })
    }

    private fun filtrarUsuarios(textoBusqueda: String) {
        val filtro = normalizarTexto(textoBusqueda)
        val filtrados = listaUsuarios.filter {
            normalizarTexto("${it.nombre} ${it.apellido} ${it.email}").contains(filtro)
        }
        adapter.actualizarLista(filtrados)
    }

    private fun normalizarTexto(texto: String): String {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }
}