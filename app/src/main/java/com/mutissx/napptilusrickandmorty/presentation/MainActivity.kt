package com.mutissx.napptilusrickandmorty.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mutissx.napptilusrickandmorty.presentation.components.BottomBar
import com.mutissx.napptilusrickandmorty.presentation.navigation.AppNavHost
import com.mutissx.napptilusrickandmorty.presentation.navigation.bottomBarRoutes
import com.mutissx.napptilusrickandmorty.core.ui.theme.NapptilusRickAndMortyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            NapptilusRickAndMortyTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (currentRoute in bottomBarRoutes) {
                            BottomBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(navController = navController, innerPadding = innerPadding)
                }
            }
        }
    }
}
