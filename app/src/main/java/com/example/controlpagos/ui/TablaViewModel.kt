package com.example.controlpagos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.controlpagos.data.Alumno
import com.example.controlpagos.data.AppDatabase
import com.example.controlpagos.data.Concepto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//Fila del alumno y abono por concepto
data class FilaPago(
    val alumno: Alumno,
    val abonado: Map<Long, Long>
)

data class TablaUiState(
    val conceptos: List<Concepto> = emptyList(),
    val filas: List<FilaPago> = emptyList()
)
class TablaViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val grupoId: Long = checkNotNull(savedStateHandle["grupoId"])

    private val db = AppDatabase.obtener(app)

    val nombreGrupo: StateFlow<String> = db.grupoDao().observarId(grupoId)
        .map { it?.nombre ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val tabla: StateFlow<TablaUiState> = combine(
        db.alumnoDao().observarPorGrupo(grupoId),
        db.conceptoDao().observarPorGrupo(grupoId),
        db.abonoDao().observarPorGrupo(grupoId)
    ) { alumnos, conceptos, abonos ->
        //Suma de los abonos
        val sumas: Map<Long, Map<Long, Long>> = abonos
            .groupBy { it.alumnoId }
            .mapValues { (_, delAlumno) ->
                delAlumno
                    .groupBy { it.conceptoId }
                    .mapValues { (_, lista) -> lista.sumOf { it.cantidadCentavos } }
            }
        TablaUiState(
            conceptos = conceptos,
            filas = alumnos.map
            { FilaPago(it, sumas[it.id] ?: emptyMap()) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TablaUiState())

    fun agregarAlumno(nombre: String){
        val limpio = nombre.trim()
        if (limpio.isEmpty()) return
        viewModelScope.launch {
            db.alumnoDao().insertar(Alumno(grupoId = grupoId, nombre = limpio))
        }
    }
    fun agregarConcepto(nombre: String, montoCentavos: Long) {
        val limpio = nombre.trim()
        if (limpio.isEmpty() || montoCentavos <= 0) return
        viewModelScope.launch {
            db.conceptoDao().insertar(
                Concepto(grupoId = grupoId, nombre = limpio, montoCentavos = montoCentavos)
            )
        }
    }
}