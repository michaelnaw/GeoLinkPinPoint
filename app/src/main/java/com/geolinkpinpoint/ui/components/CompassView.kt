package com.geolinkpinpoint.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.geolinkpinpoint.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassView(
    azimuth: Float,
    modifier: Modifier = Modifier,
    bearingToTarget: Float? = null
) {
    var previousTarget by remember { mutableFloatStateOf(0f) }
    val wrappedAzimuth = remember(azimuth) {
        val delta = ((azimuth - previousTarget + 540) % 360) - 180
        previousTarget += delta
        previousTarget
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = wrappedAzimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "compass_rotation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    val compassDescription = if (bearingToTarget != null) {
        stringResource(R.string.compass_heading_bearing_description, azimuth, bearingToTarget)
    } else {
        stringResource(R.string.compass_heading_description, azimuth)
    }

    Canvas(
        modifier = modifier
            .size(240.dp)
            .semantics { contentDescription = compassDescription }
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2 * 0.85f

        // Background circle
        drawCircle(
            color = surfaceVariantColor,
            radius = radius,
            center = Offset(centerX, centerY)
        )

        // Rotate canvas for compass orientation
        rotate(-animatedAzimuth, pivot = Offset(centerX, centerY)) {
            // Tick marks
            for (i in 0 until 360 step 10) {
                val isCardinal = i % 90 == 0
                val isMajor = i % 30 == 0
                val tickLength = when {
                    isCardinal -> 20f
                    isMajor -> 14f
                    else -> 8f
                }
                val tickWidth = if (isCardinal) 3f else 1.5f
                val angle = Math.toRadians(i.toDouble())
                val startR = radius - tickLength
                drawLine(
                    color = onSurfaceColor,
                    start = Offset(
                        centerX + (startR * sin(angle)).toFloat(),
                        centerY - (startR * cos(angle)).toFloat()
                    ),
                    end = Offset(
                        centerX + (radius * sin(angle)).toFloat(),
                        centerY - (radius * cos(angle)).toFloat()
                    ),
                    strokeWidth = tickWidth
                )
            }

            // Cardinal labels
            val cardinals = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
            for ((label, degrees) in cardinals) {
                val angle = Math.toRadians(degrees.toDouble())
                val labelRadius = radius - 35f
                val textX = centerX + (labelRadius * sin(angle)).toFloat()
                val textY = centerY - (labelRadius * cos(angle)).toFloat()
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize = 36f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        color = if (label == "N") {
                            android.graphics.Color.rgb(
                                (errorColor.red * 255).toInt(),
                                (errorColor.green * 255).toInt(),
                                (errorColor.blue * 255).toInt()
                            )
                        } else {
                            android.graphics.Color.rgb(
                                (onSurfaceColor.red * 255).toInt(),
                                (onSurfaceColor.green * 255).toInt(),
                                (onSurfaceColor.blue * 255).toInt()
                            )
                        }
                    }
                    drawText(label, textX, textY + 12f, paint)
                }
            }

            // Bearing to target arrow
            if (bearingToTarget != null) {
                val bearingAngle = Math.toRadians(bearingToTarget.toDouble())
                val arrowR = radius * 0.6f
                val arrowTipX = centerX + (arrowR * sin(bearingAngle)).toFloat()
                val arrowTipY = centerY - (arrowR * cos(bearingAngle)).toFloat()
                drawCircle(
                    color = primaryColor,
                    radius = 8f,
                    center = Offset(arrowTipX, arrowTipY)
                )
            }
        }

        // Fixed needle (always points up = North relative to device)
        drawNeedle(centerX, centerY, radius * 0.55f, errorColor, onSurfaceColor)
    }
}

private fun DrawScope.drawNeedle(
    cx: Float,
    cy: Float,
    length: Float,
    northColor: Color,
    southColor: Color
) {
    val halfWidth = 8f

    // North half (red)
    val northPath = Path().apply {
        moveTo(cx, cy - length)
        lineTo(cx - halfWidth, cy)
        lineTo(cx + halfWidth, cy)
        close()
    }
    drawPath(northPath, northColor)

    // South half
    val southPath = Path().apply {
        moveTo(cx, cy + length * 0.4f)
        lineTo(cx - halfWidth, cy)
        lineTo(cx + halfWidth, cy)
        close()
    }
    drawPath(southPath, southColor.copy(alpha = 0.4f))

    // Center dot
    drawCircle(color = southColor, radius = 5f, center = Offset(cx, cy))
}
