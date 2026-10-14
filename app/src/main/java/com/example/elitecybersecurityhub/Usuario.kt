package com.example.elitecybersecurityhub

data class Usuario(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val email: String,
    val sincronizado: Int // 0 = Pendiente (Rojo), 1 = Sincronizado (Normal)
)