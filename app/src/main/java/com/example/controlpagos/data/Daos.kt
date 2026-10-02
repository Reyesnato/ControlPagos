package com.example.controlpagos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CicloDao {
    @Query("SELECT * FROM ciclos ORDER BY id DESC")
    fun observarTodos(): Flow<List<Ciclo>>

    @Query("SELECT * FROM ciclos WHERE id = :id")
    fun observarId(id: Long): Flow<Ciclo?>

    @Insert
    suspend fun insertar(ciclo: Ciclo): Long

    @Update
    suspend fun actualizar(ciclo: Ciclo)

    @Delete
    suspend fun eliminar(ciclo: Ciclo)
}

@Dao
interface GrupoDao {
    @Query("SELECT * FROM grupos WHERE cicloId = :cicloId ORDER BY id")
    fun observarPorCiclo(cicloId: Long): Flow<List<Grupo>>

    @Insert
    suspend fun insertar(grupo: Grupo): Long

    @Update
    suspend fun actualizar(grupo: Grupo)

    @Delete
    suspend fun eliminar(grupo: Grupo)
}

@Dao
interface AlumnoDao {
    @Query("SELECT * FROM alumnos WHERE grupoId = :grupoId ORDER BY nombre")
    fun observarPorGrupo(grupoId: Long): Flow<List<Alumno>>

    @Insert
    suspend fun insertar(alumno: Alumno): Long

    @Update
    suspend fun actualizar(alumno: Alumno)

    @Delete
    suspend fun eliminar(alumno: Alumno)
}

@Dao
interface ConceptoDao {
    @Query("SELECT * FROM conceptos WHERE grupoId = :grupoId ORDER BY id")
    fun observarPorGrupo(grupoId: Long): Flow<List<Concepto>>

    @Insert
    suspend fun insertar(concepto: Concepto): Long

    @Update
    suspend fun actualizar(concepto: Concepto)

    @Delete
    suspend fun eliminar(concepto: Concepto)
}

@Dao
interface AbonoDao {
    //abonos de un grupo para armar la tabla completa
    @Query(
        """
            SELECT abonos.* FROM abonos
            INNER JOIN alumnos ON abonos.alumnoId = alumnos.id
            WHERE alumnos.grupoId = :grupoId
            """
    )
    fun observarPorGrupo(grupoId: Long): Flow<List<Abono>>

    //historial de una celda

    @Query(
        """
        SELECT * FROM abonos
        WHERE alumnoId = :alumnoId AND conceptoId = :conceptoId
        ORDER BY fecha DESC
        """
    )
    fun observarPorCelda(alumnoId: Long, conceptoId: Long): Flow<List<Abono>>

    @Insert
    suspend fun insertar(abono: Abono): Long

    @Delete
    suspend fun eliminar(abono: Abono)
}