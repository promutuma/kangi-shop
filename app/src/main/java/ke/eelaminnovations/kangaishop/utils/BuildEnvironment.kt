package ke.eelaminnovations.kangaishop.utils

import ke.eelaminnovations.kangaishop.BuildConfig

object BuildEnvironment {
    val isDevelopment: Boolean = BuildConfig.DEBUG_MODE
    val isProduction: Boolean = !BuildConfig.DEBUG_MODE
    val firebaseProject: String = BuildConfig.FIREBASE_PROJECT

    fun logEnvironment() {
        android.util.Log.d(
            "BuildEnvironment",
            "Environment: ${if (isDevelopment) "DEVELOPMENT" else "PRODUCTION"}, Firebase: $firebaseProject"
        )
    }
}
