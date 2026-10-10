package com.example.controlpagos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Ciclo::class, Grupo::class, Alumno::class, Concepto::class, Abono::class],
    version = 2,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun cicloDao(): CicloDao
    abstract fun grupoDao(): GrupoDao
    abstract fun alumnoDao(): AlumnoDao
    abstract fun conceptoDao(): ConceptoDao
    abstract fun abonoDao(): AbonoDao
    abstract fun respaldoDao(): RespaldoDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE grupos ADD COLUMN color INTEGER")
            }
        }

        fun obtener(context: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "control_pagos.db"
                )
                    .addMigrations(MIGRACION_1_2)
                    .build().also { INSTANCIA = it}
            }
    }
}