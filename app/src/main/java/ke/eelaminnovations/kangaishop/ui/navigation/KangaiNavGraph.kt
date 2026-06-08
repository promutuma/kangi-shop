package ke.eelaminnovations.kangaishop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ke.eelaminnovations.kangaishop.ui.home.HomeRoute
import ke.eelaminnovations.kangaishop.ui.login.LoginScreen
import ke.eelaminnovations.kangaishop.ui.milk.MilkRoute
import ke.eelaminnovations.kangaishop.ui.people.PeopleRoute
import ke.eelaminnovations.kangaishop.ui.people.PersonLedgerScreen
import ke.eelaminnovations.kangaishop.ui.reports.ReportsRoute
import ke.eelaminnovations.kangaishop.ui.settings.SettingsScreen
import ke.eelaminnovations.kangaishop.ui.settings.SmsLogScreen
import ke.eelaminnovations.kangaishop.ui.setup.SetupScreen

import androidx.compose.ui.Modifier

@Composable
fun KangaiNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(Screen.Setup.route) {
            SetupScreen(onSetupComplete = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Setup.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Login.route) {
            ke.eelaminnovations.kangaishop.ui.auth.AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNeedsSetup = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }


        composable(Screen.Home.route) {
            HomeRoute(onNavigate = { route -> navController.navigate(route) })
        }

        composable(Screen.Milk.route) {
            MilkRoute(onNavigateToLedger = { personId ->
                navController.navigate(Screen.PersonLedger.createRoute(personId))
            })
        }

        composable(Screen.People.route) {
            PeopleRoute(
                onNavigateToLedger = { personId ->
                    navController.navigate(Screen.PersonLedger.createRoute(personId))
                }
            )
        }

        composable(
            route = Screen.PersonLedger.route,
            arguments = listOf(navArgument("personId") { type = NavType.StringType })
        ) { backStack ->
            val personId = backStack.arguments?.getString("personId") ?: return@composable
            PersonLedgerScreen(
                personId = personId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            ReportsRoute(onNavigateToLedger = { personId ->
                navController.navigate(Screen.PersonLedger.createRoute(personId))
            })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSmsLog = { navController.navigate(Screen.SmsLog.route) },
                onNavigateToConflicts = { navController.navigate(Screen.ConflictResolution.route) }
            )
        }

        composable(Screen.ConflictResolution.route) {
            ke.eelaminnovations.kangaishop.ui.settings.ConflictResolutionScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SmsLog.route) {
            SmsLogScreen(onBack = { navController.popBackStack() })
        }
    }
}

