package com.example.controlpagos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.controlpagos.data.Abono
import com.example.controlpagos.data.Alumno
import com.example.controlpagos.data.AppDatabase
import com.example.controlpagos.data.Concepto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

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

    fun agregarAlumno(nombre: String) {
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

    fun abonosDeCelda(alumnoId: Long, conceptoId: Long): Flow<List<Abono>> =
        db.abonoDao().observarPorCelda(alumnoId, conceptoId)

    fun agregarAbono(alumnoId: Long, conceptoId: Long, centavos: Long) {
        if (centavos <= 0) return
        viewModelScope.launch {
            db.abonoDao().insertar(
                Abono(alumnoId = alumnoId, conceptoId = conceptoId, cantidadCentavos = centavos)
            )
        }
    }

    fun eliminarAbono(abono: Abono) {
        viewModelScope.launch { db.abonoDao().eliminar(abono) }
    }

    fun editarAlumno(alumno: Alumno, nuevoNombre: String) {
        val limpio = nuevoNombre.trim()
        if (limpio.isEmpty()) return
        viewModelScope.launch { db.alumnoDao().actualizar(alumno.copy(nombre = limpio)) }

    }

    fun eliminarAlumno(alumno: Alumno) {
        viewModelScope.launch { db.alumnoDao().eliminar(alumno) }
    }

    fun editarConcepto(concepto: Concepto, nuevoNombre: String, nuevoMontoCentavos: Long) {
        val limpio = nuevoNombre.trim()
        if (limpio.isEmpty() || nuevoMontoCentavos <= 0) return
        viewModelScope.launch {
            db.conceptoDao().actualizar(
                concepto.copy(nombre = limpio, montoCentavos = nuevoMontoCentavos)
            )
        }
    }

    fun eliminarConcepto(concepto: Concepto) {
        viewModelScope.launch { db.conceptoDao().eliminar(concepto) }

    }

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    fun exportarPdf(uri: Uri) {
        val estado = tabla.value
        val titulo = nombreGrupo.value
        viewModelScope.launch {
            _mensaje.value = try {
                withContext(Dispatchers.IO) {
                    val salida = getApplication<Application>().contentResolver.openOutputStream(uri)
                        ?: error("No se pudo abrir el archivo")
                    salida.use { PdfTabla.generar(titulo, estado, it) }
                }
                "PDF guardado"
            } catch (e: Exception) {
                "No se pudo exportar: ${e.message}"
            }
        }
    }
    fun exportarExcel(uri: Uri) {
        val estado = tabla.value
        viewModelScope.launch {
            _mensaje.value = try {
                withContext(Dispatchers.IO) {
                    val salida = getApplication<Application>().contentResolver.openOutputStream(uri)
                        ?: error("No se pudo abrir el archivo")
                    salida.use { ExcelTabla.generar(estado, it) }
                }
                "Excel guardado"
            } catch (e: Exception) {
                "No se pudo exportar: ${e.message}"
            }
        }
    }
}