package com.example.dermascan.ui.components

import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
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

@Composable
fun GradientScreen(colors: List<Color>, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(colors)).statusBarsPadding().navigationBarsPadding().padding(24.dp)) {
        Column(modifier = Modifier.fillMaxSize(), content = content)
    }
}

@Composable
fun ScreenColumn(contentPadding: PaddingValues = PaddingValues(bottom = 24.dp), content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 12.dp).padding(contentPadding), content = content)
}

@Composable
fun GradientHeader(title: String, subtitle: String, colors: List<Color>, action: (@Composable () -> Unit)? = null, content: (@Composable ColumnScope.() -> Unit)? = null) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Brush.linearGradient(colors)).padding(22.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(subtitle, color = Color.White.copy(alpha = 0.82f))
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
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(Brush.linearGradient(colors)).padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(backIcon, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    if (!subtitle.isNullOrBlank()) Text(subtitle, color = Color.White.copy(alpha = 0.82f))
                }
            }
            Row(content = actions)
        }
    }
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(32.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(22.dp), content = content)
    }
}

@Composable
fun AppTextField(label: String, value: String, onValueChange: (String) -> Unit, leading: @Composable (() -> Unit)? = null, trailing: @Composable (() -> Unit)? = null, visualTransformation: VisualTransformation = VisualTransformation.None) {
    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, leadingIcon = leading, trailingIcon = trailing, visualTransformation = visualTransformation, shape = RoundedCornerShape(18.dp))
}

@Composable
fun SectionTitle(title: String) {
    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, actionLabel: String, onAction: () -> Unit) {
    InfoCard {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(subtitle, color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAction, shape = RoundedCornerShape(16.dp)) { Text(actionLabel) }
        }
    }
}

@Composable
fun ScorePill(score: Int) {
    Box(modifier = Modifier.clip(CircleShape).background(scoreColor(score).copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text("$score%", color = scoreColor(score), fontWeight = FontWeight.Bold)
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
        Text(severity, color = foreground, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ScanListItem(scan: ScanRecord, fallbackIcon: ImageVector, onClick: () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)).background(Teal.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                if (scan.imageUri != null) UriImage(scan.imageUri, Modifier.fillMaxSize()) else Icon(fallbackIcon, contentDescription = null, tint = Teal)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(scan.type, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatDate(scan.dateMillis), color = Color.Gray)
            }
            ScorePill(scan.score)
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Teal)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun ListNavRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Teal)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Text(">", color = Color.Gray)
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title.uppercase(), color = Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(10.dp))
    ElevatedCard(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun SettingToggleRow(title: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Teal)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider()
}

@Composable
fun SettingStaticRow(title: String, icon: ImageVector, trailing: String? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Teal)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium)
        }
        if (trailing != null) Text(trailing, color = Color.Gray)
    }
    HorizontalDivider()
}

@Composable
fun RowScope.SmallStatCard(label: String, value: String, change: String) {
    ElevatedCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(change, color = Green, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

@Composable
fun ScoreChart(scans: List<ScanRecord>) {
    if (scans.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
            Text("No data available yet", color = Color.Gray)
        }
        return
    }
    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            val leftPad = 18.dp.toPx()
            val rightPad = 18.dp.toPx()
            val topPad = 20.dp.toPx()
            val bottomPad = 28.dp.toPx()
            val chartWidth = size.width - leftPad - rightPad
            val chartHeight = size.height - topPad - bottomPad
            val stepX = if (scans.size == 1) 0f else chartWidth / (scans.size - 1)
            repeat(5) { index ->
                val y = topPad + chartHeight / 4f * index
                drawLine(Color(0xFFE5E7EB), Offset(leftPad, y), Offset(size.width - rightPad, y), strokeWidth = 1.dp.toPx())
            }
            scans.forEachIndexed { index, scan ->
                val x = leftPad + stepX * index
                val y = topPad + chartHeight * (1f - scan.score / 100f)
                if (index < scans.lastIndex) {
                    val next = scans[index + 1]
                    val nextX = leftPad + stepX * (index + 1)
                    val nextY = topPad + chartHeight * (1f - next.score / 100f)
                    drawLine(Teal, Offset(x, y), Offset(nextX, nextY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                }
                drawCircle(Teal, radius = 6.dp.toPx(), center = Offset(x, y))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            scans.forEach { Text(shortDate(it.dateMillis), color = Color.Gray, fontSize = 12.sp) }
        }
    }
}

@Composable
fun UriImage(uri: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
        update = { imageView -> imageView.setImageURI(uri.toUri()) },
        modifier = modifier,
    )
}
