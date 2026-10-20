package de.schritte.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import de.schritte.app.SchritteApp
import de.schritte.app.ui.MainActivity
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class StepsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as SchritteApp
        val content = widgetContent(app.steps.load(), app.settings.settings.first(), LocalDate.now(app.clock))
        provideContent { GlanceTheme { Content(content) } }
    }

    @Composable
    private fun Content(c: WidgetContent) {
        val context = LocalContext.current
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(24.dp)
                .padding(12.dp)
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                c.steps,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                "Schritte heute",
                style = TextStyle(color = GlanceTheme.colors.primary, fontSize = 13.sp),
            )
            Text(
                "${c.distance} · ${c.kcal}",
                modifier = GlanceModifier.padding(top = 6.dp),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
            )
        }
    }
}

class StepsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StepsWidget()
}
