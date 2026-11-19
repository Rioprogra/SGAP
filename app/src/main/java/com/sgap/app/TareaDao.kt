package com.sgap.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TareaDao {

    @Query("SELECT * FROM tareas_guardadas WHERE idUsuario = :idUsuario")
    fun tareasDe(idUsuario: String): List<TareaGuardada>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardar(tareas: List<TareaGuardada>)

    @Query("DELETE FROM tareas_guardadas WHERE idUsuario = :idUsuario")
    fun borrarDe(idUsuario: String)
}
