package ke.eelaminnovations.kangaishop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.AppTheme
import ke.eelaminnovations.kangaishop.ui.navigation.KangaiNavGraph
import ke.eelaminnovations.kangaishop.ui.navigation.Screen
import ke.eelaminnovations.kangaishop.ui.navigation.bottomNavItems
import ke.eelaminnovations.kangaishop.ui.theme.KangaiShopTheme
import ke.eelaminnovations.kangaishop.utils.PermissionHelper
import ke.eelaminnovations.kangaishop.utils.SmsDiagnostics
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settings: AppSettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Diagnose SMS capability
        SmsDiagnostics.diagnoseSmsProblem(this)

        // Request SMS permissions early
        PermissionHelper.requestSmsPermission(this)
        PermissionHelper.requestContactsPermission(this)

        setContent {
            val theme by settings.theme.collectAsStateWithLifecycle(initialValue = AppTheme.DYNAMIC)
            val darkModeOverride by settings.darkModeOverride.collectAsStateWithLifecycle(initialValue = null)
            val systemDark = isSystemInDarkTheme()
            val darkTheme = darkModeOverride ?: systemDark

            KangaiShopTheme(theme = theme, darkTheme = darkTheme) {
                KangaiApp()
            }
        }
    }
}

@Composable
private fun KangaiApp() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screenIcon(screen), contentDescription = screenLabel(screen)) },
                            label = { Text(screenLabel(screen)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        KangaiNavGraph(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

private fun screenIcon(screen: Screen): ImageVector = when (screen) {
    Screen.Home -> Icons.Default.Home
    Screen.Milk -> Icons.Default.LocalDrink
    Screen.People -> Icons.Default.People
    Screen.Reports -> Icons.Default.BarChart
    else -> Icons.Default.Circle
}

private fun screenLabel(screen: Screen): String = when (screen) {
    Screen.Home -> "Home"
    Screen.Milk -> "Milk"
    Screen.People -> "People"
    Screen.Reports -> "Reports"
    else -> ""
}
