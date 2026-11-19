package com.sgap.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TareaGuardada::class], version = 1, exportSchema = false)
abstract class CacheLocal : RoomDatabase() {

    abstract fun tareaDao(): TareaDao

    companion object {
        private var instancia: CacheLocal? = null

        fun obtener(contexto: Context): CacheLocal {
            if (instancia == null) {
                instancia = Room.databaseBuilder(
                    contexto.applicationContext,
                    CacheLocal::class.java,
                    "studygest_cache.db"
                ).allowMainThreadQueries().build()
            }
            return instancia!!
        }

        fun guardarTareas(contexto: Context, idUsuario: String, tareas: List<Tarea>) {
            val dao = obtener(contexto).tareaDao()
            dao.borrarDe(idUsuario)
            dao.guardar(tareas.map { TareaGuardada.desde(it, idUsuario) })
        }

        fun leerTareas(contexto: Context, idUsuario: String): List<Tarea> {
            return obtener(contexto).tareaDao().tareasDe(idUsuario).map { it.aTarea() }
        }
    }
}
