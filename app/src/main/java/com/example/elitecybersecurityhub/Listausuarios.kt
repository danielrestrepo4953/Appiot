package com.example.elitecybersecurityhub

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.Normalizer
import kotlin.text.split

private lateinit var listado: ListView
private lateinit var usuariolista: ArrayList<String>

class Listausuarios : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listausuarios)


        listado=findViewById(R.id.lista)
        listado.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view,
                                              position, id ->
                val item = usuariolista[position]
                val datos = item.split(" ")
                if (datos.size >= 3) {
                    val idusu = datos[0].toIntOrNull() ?: 0
                    val nombre = datos[1]
                    val apellido = datos[2]
                    val email = datos[3]
                    val intent = Intent(
                        this@Listausuarios,
                        Usuariocrud::class.java
                    ).apply {
                        putExtra("Id", idusu)
                        putExtra("Nombre", nombre)
                        putExtra("Apellido", apellido)
                        putExtra("Email", email)
                    }
                    startActivity(intent)
                }
            }


    }

    private fun listaUsuario(): ArrayList<String> {
        val datos = ArrayList<String>()
        val helper = ConexionDbHelper(this)
        val db = helper.readableDatabase
        val sql = "SELECT * FROM USUARIOS"
        val c = db.rawQuery(sql, null)
        if (c.moveToFirst()) {
            do {
                val linea = "${c.getInt(0)} ${c.getString(1)} ${c.getString(2)} ${c.getString(3)}"
                datos.add(linea)
            } while (c.moveToNext())
        }
        c.close()
        db.close()
        return datos
    }

    private fun CargarLista() {
        usuariolista = listaUsuario()
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            usuariolista
        )
        listado.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        CargarLista()
    }



}