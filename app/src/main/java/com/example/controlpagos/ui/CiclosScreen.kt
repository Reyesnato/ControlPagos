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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controlpagos.data.Ciclo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CiclosScreen(
    onCicloClick: (Long) -> Unit,
    vm: CiclosViewModel = viewModel()
) {
    val ciclos by vm.ciclos.collectAsStateWithLifecycle()

    var mostrarAgregar by remember { mutableStateOf(false) }
    var cicloAEditar by remember { mutableStateOf<Ciclo?>(null) }
    var cicloAEliminar by remember { mutableStateOf<Ciclo?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ciclos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarAgregar = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar ciclo")
            }
        }
    ) { padding ->
        if (ciclos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Aún no hay ciclos. Toca + para agregar uno (ej. 2026 B).")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ciclos, key = { it.id }) { ciclo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCicloClick(ciclo.id) }
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
                                text = ciclo.nombre,
                                modifier = Modifier.weight(1f),
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { cicloAEditar = ciclo }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { cicloAEliminar = ciclo }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarAgregar){
        DialogoNombre(
            titulo = "Nuevo ciclo",
            etiqueta = "Nombre (ej. 2026 B)",
            textoInicial = "",
            onConfirmar = { vm.agregar(it); mostrarAgregar = false},
            onCancelar = {mostrarAgregar = false}
        )
    }

    cicloAEditar?.let { ciclo ->
        DialogoNombre(
            titulo = "Editar ciclo",
            etiqueta = "Nombre",
            textoInicial = ciclo.nombre,
            onConfirmar = { vm.renombrar(ciclo, it); cicloAEditar = null},
            onCancelar = {cicloAEditar = null}
        )
    }

    cicloAEliminar?.let { ciclo ->
        AlertDialog(
            onDismissRequest = {cicloAEliminar = null},
            title = { Text("Eliminar ciclo")},
            text = {
                Text("Se borrarán \"${ciclo.nombre}\" y todos sus grupos, alumnos, conceptos y abonos. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = { vm.eliminar(ciclo); cicloAEliminar = null }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { cicloAEliminar = null}) {Text("Cancelar")}
            }
        )
    }
}

@Composable
fun DialogoNombre(
    titulo: String,
    etiqueta: String,
    textoInicial: String,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
){
    var  texto by remember { mutableStateOf(textoInicial) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo)},
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = {Text(etiqueta)},
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmar(texto)},
                enabled = texto.isNotBlank()
            ) { Text("Guardar")}
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}