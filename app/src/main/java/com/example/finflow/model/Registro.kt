package com.example.finflow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "registros")
data class Registro(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val valor: Double,
    val ehCredito: Boolean,
    val data: Long,
    val observacao: String = ""
)