package com.example.controlpagos.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controlpagos.data.Abono
import com.example.controlpagos.data.Alumno
import com.example.controlpagos.data.Concepto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AnchoNombre = 140.dp
private val AnchoCelda = 100.dp
private val AltoEncabezado = 64.dp
private val AltoFila = 52.dp
private val VerdePagado = Color(0xFF32AD1F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablaScreen(
    onVolver: () -> Unit, vm: TablaViewModel = viewModel()
) {
    val tabla by vm.tabla.collectAsStateWithLifecycle()
    val nombreGrupo by vm.nombreGrupo.collectAsStateWithLifecycle()

    var mostrarAlumno by remember { mutableStateOf(false) }
    var mostrarConcepto by remember { mutableStateOf(false) }
    var celdaSeleccionada by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(nombreGrupo) }, navigationIcon = {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            })
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { mostrarAlumno = true }) { Text("+ Alumno") }
                OutlinedButton(onClick = { mostrarConcepto = true }) { Text("+ Concepto") }
            }

            if (tabla.filas.isEmpty() && tabla.conceptos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Agrega alumnos y conceptos para empezar.")
                }
            } else {
                TablaPagos(
                    tabla = tabla,
                    onCeldaClick = { alumnoId, conceptoId ->
                        celdaSeleccionada = alumnoId to conceptoId
                    }
                )
            }
        }
    }

    if (mostrarAlumno) {
        DialogoNombre(
            titulo = "Nuevo alumno",
            etiqueta = "Nombre del alumno",
            textoInicial = "",
            onConfirmar = { vm.agregarAlumno(it); mostrarAlumno = false },
            onCancelar = { mostrarAlumno = false })
    }

    if (mostrarConcepto) {
        DialogoConcepto(onConfirmar = { nombre, centavos ->
            vm.agregarConcepto(nombre, centavos)
            mostrarConcepto = false
        }, onCancelar = { mostrarConcepto = false })
    }
    celdaSeleccionada?.let { (alumnoId, conceptoId) ->
        val fila = tabla.filas.find { it.alumno.id == alumnoId }
        val concepto = tabla.conceptos.find { it.id == conceptoId }
        if (fila != null && concepto != null) {
            val historial by remember(alumnoId, conceptoId) {
                vm.abonosDeCelda(alumnoId, conceptoId)
            }.collectAsStateWithLifecycle(initialValue = emptyList())

            DialogoAbono(
                alumno = fila.alumno,
                concepto = concepto,
                abonado = fila.abonado[conceptoId] ?: 0L,
                historial = historial,
                onAbonar = { vm.agregarAbono(alumnoId, conceptoId, it) },
                onEliminarAbono = { vm.eliminarAbono(it) },
                onCerrar = { celdaSeleccionada = null }
            )
        }
    }
}

@Composable
private fun TablaPagos(tabla: TablaUiState, onCeldaClick: (Long, Long) -> Unit) {
    val scrollHorizontal = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        Column {
            Celda(
                Modifier
                    .width(AnchoNombre)
                    .height(AltoEncabezado)
            ) {
                Text("Nombre", fontWeight = FontWeight.Bold)
            }
            tabla.filas.forEach { fila ->
                Celda(
                    modifier = Modifier
                        .width(AnchoNombre)
                        .height(AltoFila),
                    alineacion = Alignment.CenterStart
                ) {
                    Text(fila.alumno.nombre, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        //Columas de conceptos
        Column(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollHorizontal)
        ) {
            Row {
                tabla.conceptos.forEach { concepto ->
                    Celda(
                        Modifier
                            .width(AnchoCelda)
                            .height(AltoEncabezado)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(formatoMonto(concepto.montoCentavos), fontWeight = FontWeight.Bold)
                            Text(
                                text = concepto.nombre,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            tabla.filas.forEach { fila ->
                Row {
                    tabla.conceptos.forEach { concepto ->
                        val abonado = fila.abonado[concepto.id] ?: 0L
                        val completo = abonado >= concepto.montoCentavos
                        Celda(
                            modifier = Modifier
                                .width(AnchoCelda)
                                .height(AltoFila),
                            fondo = if (completo) VerdePagado else Color.Transparent,
                            onClick = { onCeldaClick(fila.alumno.id, concepto.id) }
                        ) {
                            if (abonado > 0) {
                                Text(
                                    text = formatoMonto(abonado),
                                    color = if (completo) Color.White else Color.Unspecified
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Celda(
    modifier: Modifier,
    fondo: Color = Color.Transparent,
    alineacion: Alignment = Alignment.Center,
    onClick: (() -> Unit)? = null,
    contenido: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(fondo)
            .border(0.5.dp, MaterialTheme.colorScheme.outline)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alineacion
    ) {
        contenido()
    }
}

@Composable
private fun DialogoConcepto(
    onConfirmar: (String, Long) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    val centavos = montoACentavos(precio)

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nuevo concepto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Concepto (ej. Camisas)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio (ej. 300)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { centavos?.let { onConfirmar(nombre, it) } },
                enabled = nombre.isNotBlank() && centavos != null
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun DialogoAbono(
    alumno: Alumno,
    concepto: Concepto,
    abonado: Long,
    historial: List<Abono>,
    onAbonar: (Long) -> Unit,
    onEliminarAbono: (Abono) -> Unit,
    onCerrar: () -> Unit
) {
    var cantidad by remember { mutableStateOf("") }
    val centavos = montoACentavos(cantidad)
    val falta = (concepto.montoCentavos - abonado).coerceAtLeast(0L)
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(alumno.nombre) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${concepto.nombre} · ${formatoMonto(concepto.montoCentavos)}",
                    fontWeight = FontWeight.Bold
                )
                Text("Abonado: ${formatoMonto(abonado)}")
                Text(if (falta == 0L) "Pagado completo" else "Falta: ${formatoMonto(falta)}")

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad a abonar") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                if (falta > 0L) {
                    TextButton(onClick = { cantidad = centavosATexto(falta) }) {
                        Text("Completar (${formatoMonto(falta)})")
                    }
                }

                if (historial.isNotEmpty()) {
                    Text("Historial", fontWeight = FontWeight.Bold)
                    historial.forEach { abono ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatoFecha.format(Date(abono.fecha)),
                                modifier = Modifier.weight(1f)
                            )
                            Text(formatoMonto(abono.cantidadCentavos))
                            IconButton(onClick = { onEliminarAbono(abono) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar abono")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { centavos?.let { onAbonar(it); cantidad = "" } },
                enabled = centavos != null
            ){Text("Abonar")}
                        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar") }
        }
    )
}