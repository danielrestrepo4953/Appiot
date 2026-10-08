package com.example.elitecybersecurityhub

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
        val tvNombreCompleto: TextView =
            view.findViewById(R.id.tvNombreCompleto)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsuarioViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_itemusuario, parent, false)

        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UsuarioViewHolder,
        position: Int
    ) {

        val usuario = listaUsuarios[position]

        holder.tvNombreCompleto.text =
            "${usuario.nombre} ${usuario.apellido}"

        holder.itemView.setOnClickListener {
            onUsuarioClick(usuario)

        }
    }

    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    fun actualizarLista(nuevaLista: List<Usuario>) {
        listaUsuarios = nuevaLista
        notifyDataSetChanged()
    }
}