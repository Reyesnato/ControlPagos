package com.example.controlpagos.data

import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject

object Respaldo {

    private const val VERSION = 1

    suspend fun generarJson(db: AppDatabase): String {
        val dao = db.respaldoDao()
        val raiz = JSONObject()
        raiz.put("app", "ControlPagos")
        raiz.put("version", VERSION)
        raiz.put("fecha", System.currentTimeMillis())

        raiz.put("ciclos", JSONArray().also { arr ->
            dao.ciclos().forEach {
                arr.put(JSONObject().put("id", it.id).put("nombre", it.nombre))
            }
        })
        raiz.put("grupos", JSONArray().also { arr ->
            dao.grupos().forEach {
                arr.put(
                    JSONObject().put("id", it.id).put("cicloId", it.cicloId)
                        .put("nombre", it.nombre).put("color", it.color)
                )
            }
        })
        raiz.put("alumnos", JSONArray().also { arr ->
            dao.alumnos().forEach {
                arr.put(
                    JSONObject().put("id", it.id).put("grupoId", it.grupoId)
                        .put("nombre", it.nombre)
                )
            }
        })
        raiz.put("conceptos", JSONArray().also { arr ->
            dao.conceptos().forEach {
                arr.put(
                    JSONObject().put("id", it.id).put("grupoId", it.grupoId)
                        .put("nombre", it.nombre).put("montoCentavos", it.montoCentavos)
                )
            }
        })
        raiz.put("abonos", JSONArray().also { arr ->
            dao.abonos().forEach {
                arr.put(
                    JSONObject().put("id", it.id).put("alumnoId", it.alumnoId)
                        .put("conceptoId", it.conceptoId)
                        .put("cantidadCentavos", it.cantidadCentavos).put("fecha", it.fecha)
                )
            }
        })
        return raiz.toString(2)
    }

    suspend fun restaurarDesdeJson(db: AppDatabase, texto: String) {
        val raiz = JSONObject(texto)
        require(raiz.optString("app") == "ControlPagos") {
            "El archivo no es un respaldo de ControlPagos"
        }
        require(raiz.optInt("version", 0) in 1..VERSION) {
            "Versión de respaldo no compatible"
        }

        val ciclos = raiz.getJSONArray("ciclos").mapear {
            Ciclo(it.getLong("id"), it.getString("nombre"))
        }
        val grupos = raiz.getJSONArray("grupos").mapear {
            Grupo(
                it.getLong("id"), it.getLong("cicloId"), it.getString("nombre"),
                if (it.isNull("color")) null else it.getInt("color")
            )
        }
        val alumnos = raiz.getJSONArray("alumnos").mapear {
            Alumno(it.getLong("id"), it.getLong("grupoId"), it.getString("nombre"))
        }
        val conceptos = raiz.getJSONArray("conceptos").mapear {
            Concepto(
                it.getLong("id"), it.getLong("grupoId"),
                it.getString("nombre"), it.getLong("montoCentavos")
            )
        }
        val abonos = raiz.getJSONArray("abonos").mapear {
            Abono(
                it.getLong("id"), it.getLong("alumnoId"), it.getLong("conceptoId"),
                it.getLong("cantidadCentavos"), it.getLong("fecha")
            )
        }

        val dao = db.respaldoDao()
        db.withTransaction {
            dao.borrarTodo()
            dao.insertarCiclos(ciclos)
            dao.insertarGrupos(grupos)
            dao.insertarAlumnos(alumnos)
            dao.insertarConceptos(conceptos)
            dao.insertarAbonos(abonos)
        }
    }

    private fun <T> JSONArray.mapear(f: (JSONObject) -> T): List<T> =
        List(length()) { f(getJSONObject(it)) }
}