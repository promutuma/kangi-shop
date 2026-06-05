package ke.eelaminnovations.kangaishop.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Matches spec:
// Cards: 12dp | Input fields: 8dp | FAB: 16dp | Sheets/Dialogs: 28dp | Buttons: pill
val KangaiShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),       // input fields, chips
    medium = RoundedCornerShape(12.dp),     // cards
    large = RoundedCornerShape(16.dp),      // FAB, large containers
    extraLarge = RoundedCornerShape(28.dp)  // bottom sheets, dialogs
)
