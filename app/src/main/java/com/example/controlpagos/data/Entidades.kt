package com.example.controlpagos.data
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ciclos")
data class Ciclo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre:String
)

@Entity(
    tableName = "grupos",
    foreignKeys = [ForeignKey(
        entity = Ciclo::class,
        parentColumns = ["id"],
        childColumns = ["cicloId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("cicloId")]
)
data class Grupo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cicloId: Long,
    val nombre: String
)

@Entity(
    "alumnos",
    foreignKeys = [ForeignKey(
        entity = Grupo::class,
        parentColumns = ["id"],
        childColumns = ["grupoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("grupoId")]
)
data class Alumno(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val grupoId: Long,
    val nombre: String
)

@Entity(
    tableName = "conceptos",
    foreignKeys = [ForeignKey(
        entity = Grupo::class,
        parentColumns = ["id"],
        childColumns = ["grupoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("grupoId")]
)
data class Concepto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val grupoId: Long,
    val nombre: String,
    val montoCentavos: Long
)

@Entity(
    tableName = "abonos",
    foreignKeys = [
        ForeignKey(
            entity = Alumno::class,
            parentColumns = ["id"],
            childColumns = ["alumnoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Concepto::class,
            parentColumns = ["id"],
            childColumns = ["conceptoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("alumnoId"), Index("conceptoId")]
)
data class Abono(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alumnoId: Long,
    val conceptoId: Long,
    val cantidadCentavos: Long,
    val fecha: Long = System.currentTimeMillis()
)