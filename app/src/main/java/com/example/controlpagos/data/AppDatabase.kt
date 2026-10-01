package com.example.controlpagos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Ciclo::class, Grupo::class, Alumno::class, Concepto::class, Abono::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun cicloDao(): CicloDao
    abstract fun grupoDao(): GrupoDao
    abstract fun alumnoDao(): AlumnoDao
    abstract fun conceptoDao(): ConceptoDao
    abstract fun abonoDao(): AbonoDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "control_pagos.db"
                ).build().also { INSTANCIA = it}
            }
    }
}