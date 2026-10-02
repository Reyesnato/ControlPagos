package com.example.controlpagos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.controlpagos.data.AppDatabase
import com.example.controlpagos.data.Grupo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GruposViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    // aquí el id va a llegar como argumento de navegación
    private val cicloId: Long = checkNotNull(savedStateHandle["cicloId"])

    private val db = AppDatabase.obtener(app)
    private val grupoDao = db.grupoDao()

    val nombreCiclo: StateFlow<String> = db.cicloDao().observarId(cicloId)
        .map { it?.nombre ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val grupos: StateFlow<List<Grupo>> = grupoDao.observarPorCiclo(cicloId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregar(nombre: String) {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) return
        viewModelScope.launch { grupoDao.insertar(Grupo(cicloId = cicloId, nombre = limpio)) }
    }

    fun renombrar(grupo: Grupo, nuevoNombre: String) {
        val limpio = nuevoNombre.trim()
        if (limpio.isEmpty()) return
        viewModelScope.launch { grupoDao.actualizar(grupo.copy(nombre = limpio)) }
    }

    fun eliminar(grupo: Grupo) {
        viewModelScope.launch { grupoDao.eliminar(grupo) }
    }
}
