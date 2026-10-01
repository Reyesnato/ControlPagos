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

class CiclosViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.obtener(app).cicloDao()

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
}