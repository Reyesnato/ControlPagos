package com.example.controlpagos.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "ciclos") {

        composable("ciclos") {
            CiclosScreen(
                onCicloClick = { cicloId -> navController.navigate("grupos/$cicloId") }
            )
        }

        composable(
            route = "grupos/{cicloId}",
            arguments = listOf(navArgument("cicloId") { type = NavType.LongType })
        ) {
            GruposScreen(
                onVolver = { navController.popBackStack() },
                onGrupoClick = { grupoId ->
                    //tabla de pagos
                }
            )
        }

    }
}