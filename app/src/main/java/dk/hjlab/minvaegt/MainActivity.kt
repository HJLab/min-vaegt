package dk.hjlab.minvaegt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AppGreen = Color(0xFF2E7D32)
val PaleGreen = Color(0xFFE8F3E7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MinVaegtApp() }
    }
}

@Composable
fun MinVaegtApp() {
    val context = LocalContext.current
    var profile by remember { mutableStateOf(AppStorage.loadProfile(context)) }
    val weights = remember {
        mutableStateListOf<WeightEntry>().apply { addAll(AppStorage.loadWeights(context)) }
    }
    var selected by remember { mutableIntStateOf(0) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AppGreen,
            primaryContainer = PaleGreen,
            background = Color(0xFFF7F9F6),
            surface = Color.White
        )
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color(0xFFF4F8F2))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("MIN VÆGT", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppGreen)
                        Text("Dit rolige overblik", fontSize = 13.sp, color = Color.DarkGray)
                    }
                    Text("v1.1.0", fontSize = 12.sp, color = Color.Gray)
                }
            },
            bottomBar = {
                NavigationBar {
                    listOf("Overblik", "Ny vejning", "Profil").forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = selected == index,
                            onClick = { selected = index },
                            icon = { Text(listOf("●", "+", "☺")[index], fontSize = 18.sp) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        ) { padding ->
            when (selected) {
                0 -> Dashboard(
                    profile = profile,
                    entries = weights,
                    modifier = Modifier.padding(padding),
                    onDelete = {
                        weights.remove(it)
                        AppStorage.saveWeights(context, weights)
                    }
                )
                1 -> AddWeight(
                    latestWeight = weights.lastOrNull()?.weight ?: profile.startWeight,
                    modifier = Modifier.padding(padding),
                    onSave = {
                        weights.add(WeightEntry(it, System.currentTimeMillis()))
                        AppStorage.saveWeights(context, weights)
                        selected = 0
                    }
                )
                else -> ProfileScreen(
                    initial = profile,
                    modifier = Modifier.padding(padding),
                    onSave = {
                        profile = it
                        AppStorage.saveProfile(context, it)
                        selected = 0
                    }
                )
            }
        }
    }
}
