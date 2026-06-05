package ke.eelaminnovations.kangaishop.ui.navigation

sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object Login : Screen("login")
    object Home : Screen("home")
    object Milk : Screen("milk")
    object People : Screen("people")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object PersonLedger : Screen("person_ledger/{personId}") {
        fun createRoute(personId: String) = "person_ledger/$personId"
    }
    object SmsLog : Screen("sms_log")
    object BackupSettings : Screen("backup_settings")
    object AppUsers : Screen("app_users")
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Milk,
    Screen.People,
    Screen.Reports
)
