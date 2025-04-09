package com.example.magicsquere

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MagicSquareHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MagicSquareHomeScreen { level ->
                val intent = Intent(this, MagicGameActivity::class.java)
                intent.putExtra("level", level)
                startActivity(intent)
            }
        }
    }
}

@Composable
fun MagicSquareHomeScreen(onStartGame: (Int) -> Unit) {
    var level by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter level (1-9)")
        BasicTextField(
            value = level,
            onValueChange = { level = it },
            modifier = Modifier.padding(8.dp)
        )
        Button(onClick = {
            val lvl = level.toIntOrNull()
            if (lvl != null && lvl in 1..9) {
                onStartGame(lvl)
            } else {
                message = "Enter a number between 1 and 9"
            }
        }) {
            Text("Play")
        }
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}
