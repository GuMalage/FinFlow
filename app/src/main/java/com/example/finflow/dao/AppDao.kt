package com.example.finflow.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.finflow.model.Registro
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(registro: Registro)

    @Query("SELECT * FROM registros ORDER BY data DESC")
    fun listarTodas(): Flow<List<Registro>>

}