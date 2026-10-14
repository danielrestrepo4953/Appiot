package com.example.elitecybersecurityhub

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuarioAdapter(
    private var listaUsuarios: List<Usuario>,
    private val onUsuarioClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreCompleto: TextView = view.findViewById(R.id.tvNombreCompleto)
        val tvCorreoItem: TextView = view.findViewById(R.id.tvCorreoItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_itemusuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = listaUsuarios[position]

        holder.tvNombreCompleto.text = "${usuario.nombre} ${usuario.apellido}"
        holder.tvCorreoItem.text = usuario.email

        // Si NO está sincronizado (0), pintamos el texto en ROJO
        // En onBindViewHolder de UsuarioAdapter.kt:
        if (usuario.sincronizado == 0) {
            holder.tvNombreCompleto.setTextColor(Color.parseColor("#C8002E"))
            holder.tvCorreoItem.text = "${usuario.email} (Pendiente de sincronizar)"
            holder.tvCorreoItem.setTextColor(Color.parseColor("#C8002E"))
        } else if (usuario.sincronizado == 2) {
            holder.tvNombreCompleto.setTextColor(Color.parseColor("#C8002E"))
            holder.tvCorreoItem.text = "${usuario.email} (Pendiente de actualizar)"
            holder.tvCorreoItem.setTextColor(Color.parseColor("#C8002E"))
        } else if (usuario.sincronizado == -1) {
            holder.tvNombreCompleto.setTextColor(Color.parseColor("#C8002E"))
            holder.tvCorreoItem.text = "${usuario.email} (Pendiente de eliminar)"
            holder.tvCorreoItem.setTextColor(Color.parseColor("#C8002E"))
        } else {
            holder.tvNombreCompleto.setTextColor(Color.parseColor("#000000"))
            holder.tvCorreoItem.setTextColor(Color.parseColor("#666666"))
        }

        holder.itemView.setOnClickListener {
            onUsuarioClick(usuario)
        }
    }

    override fun getItemCount(): Int = listaUsuarios.size

    fun actualizarLista(nuevaLista: List<Usuario>) {
        listaUsuarios = nuevaLista
        notifyDataSetChanged()
    }
}