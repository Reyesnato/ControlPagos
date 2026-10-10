package com.example.controlpagos.ui

import java.io.OutputStream
import java.math.BigDecimal
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelTabla {

    // Índices de estilo (definidos en ESTILOS)
    private const val NEGRITA = 1
    private const val VERDE = 2
    private const val MONTO = 3

    fun generar(tabla: TablaUiState, salida: OutputStream) {
        val zip = ZipOutputStream(salida)
        agregar(zip, "[Content_Types].xml", CONTENT_TYPES)
        agregar(zip, "_rels/.rels", RELS)
        agregar(zip, "xl/workbook.xml", WORKBOOK)
        agregar(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS)
        agregar(zip, "xl/styles.xml", ESTILOS)
        agregar(zip, "xl/worksheets/sheet1.xml", hoja(tabla))
        zip.finish()
        zip.flush()
    }

    private fun agregar(zip: ZipOutputStream, nombre: String, contenido: String) {
        zip.putNextEntry(ZipEntry(nombre))
        zip.write(contenido.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun hoja(tabla: TablaUiState): String {
        val sb = StringBuilder()
        val totalColumnas = tabla.conceptos.size + 3 // nombre + conceptos + total + falta

        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sb.append(
            """<sheetViews><sheetView workbookViewId="0">""" +
                    """<pane xSplit="1" ySplit="1" topLeftCell="B2" activePane="bottomRight" state="frozen"/>""" +
                    """</sheetView></sheetViews>"""
        )
        sb.append(
            """<cols><col min="1" max="1" width="32" customWidth="1"/>""" +
                    """<col min="2" max="$totalColumnas" width="16" customWidth="1"/></cols>"""
        )
        sb.append("<sheetData>")

        // Fila 1: encabezados
        sb.append("""<row r="1">""")
        var col = 0
        sb.append(celdaTexto(col++, 1, "Nombre", NEGRITA))
        tabla.conceptos.forEach { concepto ->
            val titulo = "${concepto.nombre} (${formatoMonto(concepto.montoCentavos)})"
            sb.append(celdaTexto(col++, 1, titulo, NEGRITA))
        }
        sb.append(celdaTexto(col++, 1, "Total abonado", NEGRITA))
        sb.append(celdaTexto(col, 1, "Falta por pagar", NEGRITA))
        sb.append("</row>")

        // Una fila por alumno
        tabla.filas.forEachIndexed { indice, fila ->
            val numeroFila = indice + 2
            sb.append("""<row r="$numeroFila">""")
            var c = 0
            sb.append(celdaTexto(c++, numeroFila, fila.alumno.nombre, 0))

            var totalAbonado = 0L
            var falta = 0L
            tabla.conceptos.forEach { concepto ->
                val abonado = fila.abonado[concepto.id] ?: 0L
                totalAbonado += abonado
                falta += (concepto.montoCentavos - abonado).coerceAtLeast(0L)

                if (abonado > 0) {
                    val completo = abonado >= concepto.montoCentavos
                    sb.append(celdaMonto(c, numeroFila, abonado, if (completo) VERDE else MONTO))
                }
                c++
            }
            sb.append(celdaMonto(c++, numeroFila, totalAbonado, MONTO))
            sb.append(celdaMonto(c, numeroFila, falta, MONTO))
            sb.append("</row>")
        }

        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun celdaTexto(col: Int, fila: Int, texto: String, estilo: Int): String =
        """<c r="${letra(col)}$fila" t="inlineStr" s="$estilo"><is><t>${escapar(texto)}</t></is></c>"""

    private fun celdaMonto(col: Int, fila: Int, centavos: Long, estilo: Int): String =
        """<c r="${letra(col)}$fila" s="$estilo"><v>${BigDecimal.valueOf(centavos, 2).toPlainString()}</v></c>"""

    // 0 -> A, 1 -> B, ... 25 -> Z, 26 -> AA
    private fun letra(indice: Int): String {
        var n = indice
        val sb = StringBuilder()
        do {
            sb.insert(0, 'A' + n % 26)
            n = n / 26 - 1
        } while (n >= 0)
        return sb.toString()
    }

    private fun escapar(texto: String): String = texto
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    // ---- Partes fijas del archivo .xlsx ----

    private val CONTENT_TYPES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

    private val RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private val WORKBOOK = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Pagos" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

    private val WORKBOOK_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    // Estilos: 0 normal, 1 negrita, 2 verde con número, 3 número
    private val ESTILOS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<fonts count="2">
<font><sz val="11"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><name val="Calibri"/></font>
</fonts>
<fills count="3">
<fill><patternFill patternType="none"/></fill>
<fill><patternFill patternType="gray125"/></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF32AD1F"/><bgColor indexed="64"/></patternFill></fill>
</fills>
<borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="4">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="4" fontId="0" fillId="2" borderId="0" xfId="0" applyNumberFormat="1" applyFill="1"/>
<xf numFmtId="4" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
</cellXfs>
<cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
</styleSheet>"""
}