package com.geolinkpinpoint.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.geolinkpinpoint.R
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.geolinkpinpoint.ui.MainViewModel
import com.geolinkpinpoint.ui.screens.GpsCompassScreen
import com.geolinkpinpoint.ui.screens.HistoryScreen
import com.geolinkpinpoint.ui.screens.MeasureScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable object MeasureRoute
@Serializable object GpsCompassRoute
@Serializable object HistoryRoute

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val measureState by viewModel.measureState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val exportScope = rememberCoroutineScope()

    val bottomNavItems = listOf(
        BottomNavItem(stringResource(R.string.tab_measure), Icons.Default.Straighten, MeasureRoute),
        BottomNavItem(stringResource(R.string.tab_gps_compass), Icons.Default.Explore, GpsCompassRoute),
        BottomNavItem(stringResource(R.string.tab_history), Icons.Default.History, HistoryRoute)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    if (currentDestination?.hasRoute(HistoryRoute::class) == true) {
                        IconButton(onClick = {
                            exportScope.launch {
                                val uri = viewModel.exportMeasurementsCsv()
                                if (uri != null) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_measurements)))
                                } else {
                                    snackbarHostState.showSnackbar(context.getString(R.string.export_failed))
                                }
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_export_csv))
                        }
                    }
                    if (measureState.pointA != null || measureState.pointB != null) {
                        IconButton(onClick = { viewModel.clearPoints() }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear_points))
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hasRoute(item.route::class) == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MeasureRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<MeasureRoute> {
                MeasureScreen(viewModel = viewModel, snackbarHostState = snackbarHostState)
            }
            composable<GpsCompassRoute> {
                GpsCompassScreen(viewModel = viewModel, snackbarHostState = snackbarHostState)
            }
            composable<HistoryRoute> {
                HistoryScreen(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    onLoadMeasurement = {
                        navController.navigate(MeasureRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
