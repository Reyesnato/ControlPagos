package com.example.controlpagos.ui

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfTabla {

    // A4 horizontal
    private const val ANCHO_PAGINA = 842
    private const val ALTO_PAGINA = 595
    private const val MARGEN = 30f
    private const val ALTO_TITULO = 50f
    private const val ALTO_ENCABEZADO = 40f
    private const val ALTO_FILA = 24f
    private const val ANCHO_CONCEPTO = 90f

    fun generar(titulo: String, tabla: TablaUiState, salida: OutputStream) {
        val normal = Paint().apply {
            color = Color.BLACK; textSize = 10f; isAntiAlias = true
        }
        val negrita = Paint(normal).apply { typeface = Typeface.DEFAULT_BOLD }
        val blanco = Paint(normal).apply { color = Color.WHITE }
        val tituloPaint = Paint(negrita).apply { textSize = 16f }
        val gris = Paint(normal).apply { color = Color.DKGRAY; textSize = 8f }
        val borde = Paint().apply {
            color = Color.GRAY; strokeWidth = 0.5f; style = Paint.Style.STROKE
        }
        val relleno = Paint().apply {
            color = Color.rgb(0x32, 0xAD, 0x1F); style = Paint.Style.FILL
        }

        val masLargo = tabla.filas.maxOfOrNull { normal.measureText(it.alumno.nombre) } ?: 0f
        val anchoNombre = (masLargo + 16f).coerceIn(100f, 260f)

        val anchoUtil = ANCHO_PAGINA - 2 * MARGEN
        val conceptosPorPagina = ((anchoUtil - anchoNombre) / ANCHO_CONCEPTO).toInt().coerceAtLeast(1)
        val filasPorPagina =
            ((ALTO_PAGINA - 2 * MARGEN - ALTO_TITULO - ALTO_ENCABEZADO) / ALTO_FILA).toInt()
                .coerceAtLeast(1)

        val bloquesConceptos = tabla.conceptos.chunked(conceptosPorPagina).ifEmpty { listOf(emptyList()) }
        val bloquesFilas = tabla.filas.chunked(filasPorPagina).ifEmpty { listOf(emptyList()) }
        val totalPaginas = bloquesConceptos.size * bloquesFilas.size
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val doc = PdfDocument()
        try {
            var numero = 0
            for (conceptos in bloquesConceptos) {
                for (filas in bloquesFilas) {
                    numero++
                    val info = PdfDocument.PageInfo.Builder(ANCHO_PAGINA, ALTO_PAGINA, numero).create()
                    val pagina = doc.startPage(info)
                    val c = pagina.canvas

                    // Título y pie de página
                    c.drawText(titulo, MARGEN, MARGEN + 16f, tituloPaint)
                    c.drawText(
                        "Generado el $fecha · Página $numero de $totalPaginas",
                        MARGEN, ALTO_PAGINA - MARGEN / 2f, gris
                    )

                    val yEncabezado = MARGEN + ALTO_TITULO

                    c.drawRect(MARGEN, yEncabezado, MARGEN + anchoNombre, yEncabezado + ALTO_ENCABEZADO, borde)
                    c.drawText("Nombre", MARGEN + 6f, yEncabezado + ALTO_ENCABEZADO / 2f + 4f, negrita)

                    conceptos.forEachIndexed { i, concepto ->
                        val x = MARGEN + anchoNombre + i * ANCHO_CONCEPTO
                        c.drawRect(x, yEncabezado, x + ANCHO_CONCEPTO, yEncabezado + ALTO_ENCABEZADO, borde)
                        centrado(c, formatoMonto(concepto.montoCentavos), negrita, x, ANCHO_CONCEPTO, yEncabezado + 16f)
                        centrado(
                            c, ajustar(concepto.nombre, normal, ANCHO_CONCEPTO - 8f), normal,
                            x, ANCHO_CONCEPTO, yEncabezado + 31f
                        )
                    }

                    // Filas
                    filas.forEachIndexed { r, fila ->
                        val y = yEncabezado + ALTO_ENCABEZADO + r * ALTO_FILA
                        val yTexto = y + ALTO_FILA / 2f + 4f

                        c.drawRect(MARGEN, y, MARGEN + anchoNombre, y + ALTO_FILA, borde)
                        c.drawText(
                            ajustar(fila.alumno.nombre, normal, anchoNombre - 12f),
                            MARGEN + 6f, yTexto, normal
                        )

                        conceptos.forEachIndexed { i, concepto ->
                            val x = MARGEN + anchoNombre + i * ANCHO_CONCEPTO
                            val abonado = fila.abonado[concepto.id] ?: 0L
                            val completo = abonado >= concepto.montoCentavos

                            if (completo) c.drawRect(x, y, x + ANCHO_CONCEPTO, y + ALTO_FILA, relleno)
                            c.drawRect(x, y, x + ANCHO_CONCEPTO, y + ALTO_FILA, borde)
                            if (abonado > 0) {
                                centrado(
                                    c, formatoMonto(abonado),
                                    if (completo) blanco else normal,
                                    x, ANCHO_CONCEPTO, yTexto
                                )
                            }
                        }
                    }

                    doc.finishPage(pagina)
                }
            }
            doc.writeTo(salida)
        } finally {
            doc.close()
        }
    }

    private fun centrado(
        c: android.graphics.Canvas, texto: String, paint: Paint,
        x: Float, ancho: Float, y: Float
    ) {
        c.drawText(texto, x + (ancho - paint.measureText(texto)) / 2f, y, paint)
    }

    private fun ajustar(texto: String, paint: Paint, ancho: Float): String {
        if (paint.measureText(texto) <= ancho) return texto
        val letras = paint.breakText(texto, true, ancho - paint.measureText("…"), null)
        return texto.take(letras).trimEnd() + "…"
    }
}