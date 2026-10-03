package com.example.controlpagos.ui

fun formatoMonto(centavos: Long): String =
    if (centavos % 100 == 0L) "\$${centavos / 100}"
    else "\$${centavos / 100}.${"%02d".format(centavos % 100)}"

fun montoACentavos(texto: String): Long? {
    val numero = texto.trim().replace(",",".").toBigDecimalOrNull() ?: return null
    if (numero.signum() <= 0) return null
    return numero.movePointRight(2).toLong()
}