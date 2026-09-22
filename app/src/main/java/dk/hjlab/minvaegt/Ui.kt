package dk.hjlab.minvaegt

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

private val DanishDate = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("da", "DK"))
private val DanishDateTime = DateTimeFormatter.ofPattern("dd.MM.yyyy 'kl.' HH:mm", Locale("da", "DK"))

@Composable
fun Dashboard(
    profile: Profile,
    entries: List<WeightEntry>,
    modifier: Modifier = Modifier,
    onDelete: (WeightEntry) -> Unit
) {
    val current = entries.lastOrNull()?.weight ?: profile.startWeight
    val lost = profile.startWeight - current
    val remaining = max(0.0, current - profile.goalWeight)
    val heightMetres = profile.heightCm / 100
    val bmi = if (heightMetres > 0) current / (heightMetres * heightMetres) else 0.0

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                if (profile.name.isBlank()) "Dit vægtforløb" else "Hej ${profile.name}",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Startdato: ${prettyDate(profile.startDate)}", color = Color.Gray)
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PaleGreen),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Nuværende vægt", color = Color.DarkGray)
                    Text(
                        "${oneDecimal(current)} kg",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppGreen
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Stat("Tabt", "${oneDecimal(lost)} kg", Modifier.weight(1f))
                        Stat("Til mål", "${oneDecimal(remaining)} kg", Modifier.weight(1f))
                        Stat("BMI", oneDecimal(bmi), Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Udvikling", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(10.dp))
                    WeightChart(entries.ifEmpty { listOf(WeightEntry(profile.startWeight, 0L)) })
                    Text(
                        "Ønsket vægt: ${oneDecimal(profile.goalWeight)} kg",
                        color = AppGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        item { Text("Historik", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        if (entries.isEmpty()) {
            item {
                Text("Ingen vejninger endnu. Tryk på “Ny vejning” nederst.", color = Color.Gray)
            }
        } else {
            items(entries.sortedByDescending { it.timestamp }, key = { it.timestamp }) { entry ->
                Card {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${oneDecimal(entry.weight)} kg",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                            Text(formatTimestamp(entry.timestamp), color = Color.Gray, fontSize = 13.sp)
                        }
                        OutlinedButton(onClick = { onDelete(entry) }) { Text("Slet") }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun Stat(title: String, value: String, modifier: Modifier = Modifier) {
    Box(modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(10.dp)) {
        Column {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun WeightChart(entries: List<WeightEntry>) {
    val values = entries.sortedBy { it.timestamp }.map { it.weight }
    val low = values.minOrNull() ?: 0.0
    val high = values.maxOrNull() ?: 1.0
    val range = max(1.0, high - low)

    Canvas(Modifier.fillMaxWidth().height(150.dp)) {
        val points = values.mapIndexed { index, value ->
            val x = if (values.size == 1) size.width / 2
            else index * size.width / (values.size - 1)
            val y = size.height -
                (((value - low) / range).toFloat() * (size.height * 0.75f)) -
                size.height * 0.1f
            Offset(x, y)
        }
        points.zipWithNext().forEach { (a, b) ->
            drawLine(AppGreen, a, b, strokeWidth = 7f, cap = StrokeCap.Round)
        }
        points.forEach { drawCircle(AppGreen, 9f, it) }
    }
}

@Composable
fun AddWeight(
    latestWeight: Double,
    modifier: Modifier = Modifier,
    onSave: (Double) -> Unit
) {
    var value by remember { mutableStateOf(oneDecimal(latestWeight)) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Ny vejning", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Dato og klokkeslæt gemmes automatisk.", color = Color.Gray)
        OutlinedTextField(
            value = value,
            onValueChange = { value = it; error = "" },
            label = { Text("Din vægt i kg") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error.isNotEmpty(),
            supportingText = { if (error.isNotEmpty()) Text(error) }
        )
        Button(
            onClick = {
                val parsed = value.replace(",", ".").toDoubleOrNull()
                if (parsed == null || parsed !in 30.0..300.0) {
                    error = "Skriv en vægt mellem 30 og 300 kg"
                } else {
                    onSave(parsed)
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text("Gem vejning", fontSize = 17.sp)
        }
    }
}

@Composable
fun ProfileScreen(
    initial: Profile,
    modifier: Modifier = Modifier,
    onSave: (Profile) -> Unit
) {
    var name by remember(initial) { mutableStateOf(initial.name) }
    var height by remember(initial) { mutableStateOf(oneDecimal(initial.heightCm)) }
    var startWeight by remember(initial) { mutableStateOf(oneDecimal(initial.startWeight)) }
    var goalWeight by remember(initial) { mutableStateOf(oneDecimal(initial.goalWeight)) }
    var startDate by remember(initial) { mutableStateOf(initial.startDate) }
    var error by remember { mutableStateOf("") }

    LazyColumn(
        modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Din profil", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Udfyld én ønsket idealvægt.", color = Color.Gray)
        }
        item { InputField(name, { name = it }, "Navn", KeyboardType.Text) }
        item { InputField(height, { height = it }, "Højde i cm", KeyboardType.Decimal) }
        item { InputField(startWeight, { startWeight = it }, "Startvægt i kg", KeyboardType.Decimal) }
        item { InputField(goalWeight, { goalWeight = it }, "Ønsket idealvægt i kg", KeyboardType.Decimal) }
        item { InputField(startDate, { startDate = it }, "Startdato (åååå-mm-dd)", KeyboardType.Number) }
        if (error.isNotEmpty()) {
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }
        item {
            Button(
                onClick = {
                    val h = height.replace(",", ".").toDoubleOrNull()
                    val start = startWeight.replace(",", ".").toDoubleOrNull()
                    val goal = goalWeight.replace(",", ".").toDoubleOrNull()
                    val validDate = runCatching { LocalDate.parse(startDate) }.isSuccess
                    if (
                        h == null || h !in 100.0..230.0 ||
                        start == null || start !in 30.0..300.0 ||
                        goal == null || goal !in 30.0..300.0 ||
                        !validDate
                    ) {
                        error = "Kontrollér højde, vægte og dato."
                    } else {
                        onSave(Profile(name.trim(), h, start, goal, startDate))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("Gem profil", fontSize = 17.sp)
            }
        }
        item {
            Text(
                "Oplysningerne gemmes kun lokalt på denne telefon.",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun InputField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

fun oneDecimal(value: Double): String =
    String.format(Locale("da", "DK"), "%.1f", value)

private fun prettyDate(raw: String): String =
    runCatching { LocalDate.parse(raw).format(DanishDate) }.getOrDefault(raw)

private fun formatTimestamp(timestamp: Long): String =
    Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DanishDateTime)
