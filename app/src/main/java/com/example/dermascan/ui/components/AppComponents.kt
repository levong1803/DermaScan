package com.example.dermascan.ui.components

import android.graphics.Paint
import android.widget.ImageView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.example.dermascan.model.ScanRecord
import com.example.dermascan.ui.theme.Green
import com.example.dermascan.ui.theme.Red
import com.example.dermascan.ui.theme.Teal
import com.example.dermascan.util.formatDate
import com.example.dermascan.util.scoreColor
import com.example.dermascan.util.shortDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Uniform Design System
object AppShapes {
    val Card = RoundedCornerShape(24.dp)
    val Button = RoundedCornerShape(16.dp)
    val Input = RoundedCornerShape(14.dp)
    val Small = RoundedCornerShape(12.dp)
}

@Composable
fun ScreenColumn(
    contentPadding: PaddingValues = PaddingValues(bottom = 24.dp), 
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp)
            .padding(contentPadding), 
        content = content
    )
}

@Composable
fun GradientScreen(colors: List<Color>, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    ) {
        content()
    }
}

@Composable
fun GradientHeader(title: String, subtitle: String, colors: List<Color>, action: (@Composable () -> Unit)? = null, content: (@Composable ColumnScope.() -> Unit)? = null) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(colors)).padding(22.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(subtitle, color = Color.White.copy(alpha = 0.82f), fontSize = 15.sp)
                }
                action?.invoke()
            }
            if (content != null) {
                Spacer(modifier = Modifier.height(20.dp))
                content()
            }
        }
    }
}

@Composable
fun BackHeader(title: String, subtitle: String? = null, colors: List<Color>, actions: @Composable RowScope.() -> Unit = {}, onBack: () -> Unit, backIcon: ImageVector) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(colors)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(backIcon, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    if (!subtitle.isNullOrBlank()) Text(subtitle, color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp)
                }
            }
            Row(content = actions)
        }
    }
}

@Composable
fun InfoCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        shape = AppShapes.Card, 
        modifier = modifier.fillMaxWidth(), 
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ElevatedCard(
        shape = AppShapes.Card,
        modifier = modifier.fillMaxWidth().then(clickableModifier),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(22.dp), content = content)
    }
}

@Composable
fun AppTextField(label: String, value: String, onValueChange: (String) -> Unit, leading: @Composable (() -> Unit)? = null, trailing: @Composable (() -> Unit)? = null, visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None) {
    OutlinedTextField(
        value = value, 
        onValueChange = onValueChange, 
        modifier = Modifier.fillMaxWidth(), 
        label = { Text(label) }, 
        leadingIcon = leading, 
        trailingIcon = trailing, 
        visualTransformation = visualTransformation, 
        shape = AppShapes.Input,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Teal,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun SectionTitle(title: String) {
    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, actionLabel: String, onAction: () -> Unit) {
    InfoCard {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color.LightGray.copy(alpha = 0.6f), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAction, shape = AppShapes.Button) { Text(actionLabel) }
        }
    }
}

@Composable
fun ScorePill(score: Int) {
    Box(modifier = Modifier.clip(CircleShape).background(scoreColor(score).copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text("$score%", color = scoreColor(score), fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun SeverityBadge(severity: String) {
    val background = when (severity) {
        "Low" -> Green.copy(alpha = 0.14f)
        "Moderate" -> Color(0xFFFEF3C7)
        else -> Color(0xFFFEE2E2)
    }
    val foreground = when (severity) {
        "Low" -> Green
        "Moderate" -> Color(0xFFD97706)
        else -> Red
    }
    Box(modifier = Modifier.clip(CircleShape).background(background).padding(horizontal = 12.dp, vertical = 5.dp)) {
        Text(severity, color = foreground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ScanListItem(scan: ScanRecord, fallbackIcon: ImageVector, onClick: () -> Unit) {
    ModernCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(Teal.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                if (scan.imageUri != null) UriImage(scan.imageUri, Modifier.fillMaxSize()) else Icon(fallbackIcon, contentDescription = null, tint = Teal)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(scan.type, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatDate(scan.dateMillis), color = Color.Gray, fontSize = 13.sp)
            }
            ScorePill(scan.score)
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Teal)
        Text(label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ListNavRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    ModernCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Teal.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title.uppercase(), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(top = 16.dp, start = 4.dp), letterSpacing = 1.sp)
    Spacer(modifier = Modifier.height(8.dp))
    ElevatedCard(shape = AppShapes.Card, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun SettingToggleRow(title: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Teal))
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
}

@Composable
fun SettingStaticRow(title: String, icon: ImageVector, trailing: String? = null, onClick: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }.padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (trailing != null) Text(trailing, color = Color.Gray, fontSize = 14.sp)
            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
}

@Composable
fun RowScope.HeaderStatCard(label: String, value: String, icon: ImageVector, background: Color, tint: Color) {
    Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.15f)).padding(14.dp)) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(background), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RowScope.SmallStatCard(label: String, value: String, change: String) {
    ElevatedCard(shape = AppShapes.Card, modifier = Modifier.weight(1f), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(change, color = Green, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun QuickActionCard(title: String, colors: List<Color>, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.width(156.dp).height(120.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(colors)).clickable(onClick = onClick).padding(16.dp)) {
        Column {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun ScoreChart(scans: List<ScanRecord>) {
    if (scans.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No history data", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }
    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val chartWidth = size.width - 40.dp.toPx()
            val chartHeight = size.height - 40.dp.toPx()
            val stepX = if (scans.size == 1) 0f else chartWidth / (scans.size - 1)
            
            scans.forEachIndexed { index, scan ->
                val x = 20.dp.toPx() + stepX * index
                val y = 20.dp.toPx() + chartHeight * (1f - scan.score / 100f)
                if (index < scans.lastIndex) {
                    val nextX = 20.dp.toPx() + stepX * (index + 1)
                    val nextY = 20.dp.toPx() + chartHeight * (1f - scans[index+1].score / 100f)
                    drawLine(Teal, Offset(x, y), Offset(nextX, nextY), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                }
                drawCircle(Teal, radius = 5.dp.toPx(), center = Offset(x, y))
                drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            scans.forEach { Text(shortDate(it.dateMillis), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Medium) }
        }
    }
}

@Composable
fun RadarChart(labels: List<String>, values: List<Float>, modifier: Modifier = Modifier, color: Color = Teal) {
    val textPaint = remember {
        Paint().apply {
            this.color = android.graphics.Color.GRAY
            this.textSize = 28f
            this.textAlign = Paint.Align.CENTER
            this.isAntiAlias = true
        }
    }
    
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2 * 0.7f
        val numPoints = labels.size
        val angleStep = (2 * PI / numPoints).toFloat()

        // Background polygons
        for (i in 1..4) {
            val currentRadius = radius * (i / 4f)
            val path = Path()
            for (j in 0 until numPoints) {
                val angle = j * angleStep - PI.toFloat() / 2
                val x = centerX + currentRadius * cos(angle)
                val y = centerY + currentRadius * sin(angle)
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, Color.LightGray.copy(alpha = 0.3f), style = Stroke(width = 1.dp.toPx()))
        }

        // Value polygon
        val valuePath = Path()
        for (j in 0 until numPoints) {
            val angle = j * angleStep - PI.toFloat() / 2
            val vRadius = radius * (values[j] / 100f)
            val x = centerX + vRadius * cos(angle)
            val y = centerY + vRadius * sin(angle)
            if (j == 0) valuePath.moveTo(x, y) else valuePath.lineTo(x, y)
        }
        valuePath.close()
        drawPath(valuePath, color.copy(alpha = 0.25f), style = Fill)
        drawPath(valuePath, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

        // Draw Labels
        for (j in 0 until numPoints) {
            val angle = j * angleStep - PI.toFloat() / 2
            val labelRadius = radius + 25.dp.toPx()
            val x = centerX + labelRadius * cos(angle)
            val y = centerY + labelRadius * sin(angle)
            drawContext.canvas.nativeCanvas.drawText(labels[j], x, y + 10f, textPaint)
        }
    }
}

@Composable
fun AIScanOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(2500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "yOffset"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineY = size.height * yOffset
            drawLine(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Teal, Color.Transparent)), start = Offset(0f, lineY), end = Offset(size.width, lineY), strokeWidth = 3.dp.toPx())
            
            val corner = 30.dp.toPx()
            val stroke = 3.dp.toPx()
            // Corners
            drawLine(Teal, Offset(10f, 10f), Offset(10f + corner, 10f), strokeWidth = stroke)
            drawLine(Teal, Offset(10f, 10f), Offset(10f, 10f + corner), strokeWidth = stroke)
            drawLine(Teal, Offset(size.width - 10f, 10f), Offset(size.width - 10f - corner, 10f), strokeWidth = stroke)
            drawLine(Teal, Offset(size.width - 10f, 10f), Offset(size.width - 10f, 10f + corner), strokeWidth = stroke)
            drawLine(Teal, Offset(10f, size.height - 10f), Offset(10f + corner, size.height - 10f), strokeWidth = stroke)
            drawLine(Teal, Offset(10f, size.height - 10f), Offset(10f, size.height - 10f - corner), strokeWidth = stroke)
            drawLine(Teal, Offset(size.width - 10f, size.height - 10f), Offset(size.width - 10f - corner, size.height - 10f), strokeWidth = stroke)
            drawLine(Teal, Offset(size.width - 10f, size.height - 10f), Offset(size.width - 10f, size.height - 10f - corner), strokeWidth = stroke)
        }
    }
}

@Composable
fun SkeletonBox(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "shimmer"
    )
    val brush = Brush.linearGradient(
        colors = listOf(Color.LightGray.copy(0.4f), Color.LightGray.copy(0.1f), Color.LightGray.copy(0.4f)),
        start = Offset.Zero, end = Offset(translateAnim, translateAnim)
    )
    Box(modifier = modifier.background(brush))
}

@Composable
fun UriImage(uri: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
        update = { imageView -> imageView.setImageURI(uri.toUri()) },
        modifier = modifier,
    )
}
