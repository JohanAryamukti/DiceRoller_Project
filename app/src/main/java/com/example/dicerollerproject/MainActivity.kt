package com.example.dicerollerproject

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DiceRollerApp()
                }
            }
        }
    }
}

@Composable
fun DiceRollerApp() {
    val context = LocalContext.current

    var result1 by remember { mutableIntStateOf(1) }
    var result2 by remember { mutableIntStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }

    val rotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val historyList = remember { mutableStateListOf<String>() }

    val imageResource1 = getDiceImageResource(result1)
    val imageResource2 = getDiceImageResource(result2)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Dice Roller (Interactive)",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageResource1),
                contentDescription = "Dadu 1: $result1",
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer { rotationZ = rotation.value }
            )
            Image(
                painter = painterResource(id = imageResource2),
                contentDescription = "Dadu 2: $result2",
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer { rotationZ = rotation.value }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            enabled = !isRolling,
            onClick = {
                coroutineScope.launch {
                    isRolling = true

                    // 1. Putar Efek Suara dari res/raw/dice_roll.mp3
                    try {
                        val mediaPlayer = MediaPlayer.create(context, R.raw.dice_roll)
                        mediaPlayer?.start()
                        mediaPlayer?.setOnCompletionListener { mp ->
                            mp.release() // Lepas memori setelah audio selesai diputar
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // 2. Tentukan angka acak final
                    val final1 = (1..6).random()
                    val final2 = (1..6).random()

                    // 3. Jalankan animasi rotasi
                    val animJob = launch {
                        rotation.animateTo(
                            targetValue = rotation.value + 720f,
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    }

                    // 4. Efek visual acak
                    repeat(8) {
                        result1 = (1..6).random()
                        result2 = (1..6).random()
                        delay(60)
                    }

                    animJob.join()

                    // 5. Kunci hasil akhir
                    result1 = final1
                    result2 = final2

                    val total = final1 + final2
                    val logEntry = "Dadu 1: $final1 | Dadu 2: $final2 (Total: $total)"

                    historyList.add(0, logEntry)
                    if (historyList.size > 5) {
                        historyList.removeLast()
                    }

                    isRolling = false
                }
            }
        ) {
            Text(
                text = if (isRolling) "Mengocok..." else "Roll",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Riwayat Kocokan:",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (historyList.isEmpty()) {
            Text(text = "Belum ada riwayat", fontSize = 14.sp)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                historyList.forEachIndexed { index, log ->
                    val isNewest = index == 0
                    val tag = if (isNewest) " (Terbaru)" else ""

                    Text(
                        text = "${index + 1}. $log$tag",
                        fontSize = 14.sp,
                        fontWeight = if (isNewest) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun getDiceImageResource(result: Int): Int {
    return when (result) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
}