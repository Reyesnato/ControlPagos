package com.example.controlpagos.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controlpagos.data.Grupo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GruposScreen(
    onVolver: () -> Unit,
    onGrupoClick: (Long) -> Unit,
    vm: GruposViewModel = viewModel()
) {
    val grupos by vm.grupos.collectAsStateWithLifecycle()
    val nombreCiclo by vm.nombreCiclo.collectAsStateWithLifecycle()

    var mostrarAgregar by remember { mutableStateOf(false) }
    var grupoAEditar by remember { mutableStateOf<Grupo?>(null) }
    var grupoAEliminar by remember { mutableStateOf<Grupo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grupos · $nombreCiclo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarAgregar = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar grupo")
            }
        }
    ) { padding ->
        if (grupos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Aún no hay grupos. Toca + para agregar uno (ej. Avanzadas).")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(grupos, key = { it.id }) { grupo ->
                    val colorFondo = grupo.color?.let { Color(it) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGrupoClick(grupo.id) },
                        colors = if (colorFondo != null){
                            CardDefaults.cardColors(
                                containerColor = colorFondo,
                                contentColor = Color.Black
                            )
                        } else {
                            CardDefaults.cardColors()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                start = 16.dp,
                                top = 8.dp,
                                bottom = 8.dp,
                                end = 4.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = grupo.nombre,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { grupoAEditar = grupo }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { grupoAEliminar = grupo }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarAgregar) {
        DialogoNombre(
            titulo = "Nuevo grupo",
            etiqueta = "Nombre (ej.Avanzadas)",
            textoInicial = "",
            onConfirmar = { vm.agregar(it); mostrarAgregar = false },
            onCancelar = { mostrarAgregar = false }
        )
    }

    grupoAEditar?.let { grupo ->
        DialogoEditarGrupo(
            grupo = grupo,
            onGuardar = { nombre, color -> vm.editar(grupo, nombre, color); grupoAEditar = null },
            onCancelar = { grupoAEditar = null }
        )
    }
    grupoAEliminar?.let { grupo ->
        AlertDialog(
            onDismissRequest = { grupoAEliminar = null },
            title = { Text("Eliminar grupo") },
            text = {
                Text("Se borrarán \"${grupo.nombre}\" y todos sus alumnos, conceptos y abonos. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = { vm.eliminar(grupo); grupoAEliminar = null }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { grupoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}
private val PaletaGrupos = listOf(
    Color(0xFFEF9A9A), // rojo
    Color(0xFFFFCC80), // naranja
    Color(0xFFFFF59D), // amarillo
    Color(0xFFA5D6A7), // verde
    Color(0xFF80DEEA), // turquesa
    Color(0xFF90CAF9), // azul
    Color(0xFFCE93D8), // morado
    Color(0xFFF48FB1)  // rosa
)

@Composable
private fun DialogoEditarGrupo(
    grupo: Grupo,
    onGuardar: (String, Int?) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(grupo.nombre) }
    var color by remember { mutableStateOf(grupo.color) }

    // null = "sin color", seguido de los colores de la paleta
    val opciones: List<Color?> = listOf(null) + PaletaGrupos

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar grupo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Text("Color de fondo")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    opciones.chunked(5).forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            fila.forEach { c ->
                                OpcionColor(
                                    color = c,
                                    seleccionado = color == c?.toArgb(),
                                    onClick = { color = c?.toArgb() }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onGuardar(nombre, color) },
                enabled = nombre.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun OpcionColor(
    color: Color?,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color ?: Color.Transparent)
            .border(
                width = if (seleccionado) 3.dp else 1.dp,
                color = if (seleccionado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (color == null) {
            Icon(Icons.Filled.Close, contentDescription = "Sin color")
        } else if (seleccionado) {
            Icon(Icons.Filled.Check, contentDescription = "Seleccionado", tint = Color.Black)
        }
    }
}


