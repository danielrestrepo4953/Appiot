package com.example.elitecybersecurityhub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.Normalizer

class Listausuarios : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsuarioAdapter
    private lateinit var searchView: SearchView

    private val usuarios = mutableListOf(
        Usuario("Daniel", "Restrepo"),
        Usuario("Juan", "Cortes"),
        Usuario("Benjamin", "Avalos"),
        Usuario("Ana", "Barraza")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listausuarios)
        recyclerView = findViewById(R.id.rvUsuarios)
        searchView = findViewById(R.id.searchViewUsuarios)
        adapter = UsuarioAdapter(usuarios) {usuario ->

            val intent = Intent(this, Usuariocrud::class.java)

            intent.putExtra(
                "nombreUsuario",
                usuario.nombre
            )

            intent.putExtra(
                "apellidoUsuario",
                usuario.apellido
            )

            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        configurarBuscador()



    }

    private fun configurarBuscador() {

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {

                    filtrarUsuarios(newText ?: "")

                    return true
                }
            }
        )
    }

    private fun filtrarUsuarios(textoBusqueda: String) {

        val filtro = normalizarTexto(textoBusqueda)

        val usuariosFiltrados = usuarios.filter { usuario ->

            val nombreCompleto = normalizarTexto(
                "${usuario.nombre} ${usuario.apellido}"
            )

            nombreCompleto.contains(filtro)
        }

        adapter.actualizarLista(usuariosFiltrados)
    }

    private fun normalizarTexto(texto: String): String {

        return Normalizer
            .normalize(texto, Normalizer.Form.NFD)
            .replace(
                "\\p{InCombiningDiacriticalMarks}+".toRegex(),
                ""
            )
            .lowercase()
    }
}