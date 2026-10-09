package com.example.controlpagos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlpagos.data.AppDatabase
import com.example.controlpagos.data.Ciclo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.net.Uri
import com.example.controlpagos.data.Respaldo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class CiclosViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val dao = db.cicloDao()

    val ciclos: StateFlow<List<Ciclo>> = dao.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregar(nombre: String) {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) return
        viewModelScope.launch { dao.insertar(Ciclo(nombre = limpio)) }
    }

    fun renombrar(ciclo: Ciclo, nuevoNombre: String) {
        val limpio = nuevoNombre.trim()
        if (limpio.isEmpty()) return
        viewModelScope.launch { dao.actualizar(ciclo.copy(nombre = limpio)) }
    }

    fun eliminar(ciclo: Ciclo) {
        viewModelScope.launch { dao.eliminar(ciclo) }
    }

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    fun exportar(uri: Uri) {
        viewModelScope.launch {
            _mensaje.value = try {
                val json = Respaldo.generarJson(db)
                withContext(Dispatchers.IO) {
                    val salida = getApplication<Application>().contentResolver.openOutputStream(uri)
                        ?: error("No se pudo abrir el archivo")
                    salida.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                }
                "Respaldo guardado"
            } catch (e: Exception) {
                "No se pudo exportar: ${e.message}"
            }
        }
    }

    fun importar(uri: Uri) {
        viewModelScope.launch {
            _mensaje.value = try {
                val texto = withContext(Dispatchers.IO) {
                    val entrada = getApplication<Application>().contentResolver.openInputStream(uri)
                        ?: error("No se pudo abrir el archivo")
                    entrada.use { it.readBytes().toString(Charsets.UTF_8) }
                }
                Respaldo.restaurarDesdeJson(db, texto)
                "Respaldo restaurado"
            } catch (e: Exception) {
                "No se pudo importar: ${e.message}"
            }
        }
    }
}