package ke.eelaminnovations.kangaishop.utils

import java.security.MessageDigest

fun hashPin(pin: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
    return hash.joinToString("") { "%02x".format(it) }
}
