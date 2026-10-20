package de.schritte.app.ui

import java.util.Locale

fun formatDiff(diff: Int, percent: Int?): String {
    if (diff == 0) return "unverändert"
    val steps = String.format(Locale.GERMANY, "%+,d", diff)
    return if (percent == null) steps else "$steps (${String.format(Locale.GERMANY, "%+d %%", percent)})"
}
