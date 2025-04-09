package com.example.magicsquere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import kotlin.random.Random

class MagicGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val level = intent.getIntExtra("level", 3) // Number of empty cells
        setContent {
            MagicGameScreen(level, onBackPressed = { finish() })
        }
    }
}

@Composable
fun MagicGameScreen(level: Int, onBackPressed: () -> Unit) {
    val size = 3 // Fixed 3x3 magic square
    val fullMagicSquare = generateMagicSquare(size)
    val emptyPositions = remember { generateEmptyPositions(level, size) }
    val grid = remember {
        mutableStateListOf<List<MutableState<String>>>().apply {
            repeat(size) { row ->
                add(List(size) { col ->
                    if (row to col in emptyPositions)
                        mutableStateOf("")
                    else
                        mutableStateOf(fullMagicSquare[row][col].toString())
                })
            }
        }
    }

    var resultText by remember { mutableStateOf("") }
    var showRulesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(color = Color(0xFFF0F8FF)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(grid.size) { row ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LazyRow {
                        items(grid[row].size) { col ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .padding(4.dp)
                                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE6F7FF), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val isEditable = (row to col) in emptyPositions
                                BasicTextField(
                                    value = grid[row][col].value,
                                    onValueChange = {
                                        if (it.length <= 1 && it.all { c -> c in '1'..'9' }) {
                                            grid[row][col].value = it
                                        }
                                    },
                                    enabled = isEditable,
                                    textStyle = TextStyle(
                                        fontSize = 20.sp,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEditable) Color.Black else Color.Gray
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                    val rowSum = grid[row].sumOf { it.value.toIntOrNull() ?: 0 }
                    Text(" = $rowSum", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    repeat(size) { col ->
                        val colSum = grid.sumOf { it[col].value.toIntOrNull() ?: 0 }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                            Text("=", fontSize = 14.sp)
                            Text("$colSum", fontSize = 16.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                val mainDiagonalSum = (0 until size).sumOf { grid[it][it].value.toIntOrNull() ?: 0 }
                val sideDiagonalSum = (0 until size).sumOf { grid[it][size - 1 - it].value.toIntOrNull() ?: 0 }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Main Diagonal", fontSize = 14.sp)
                        Text("= $mainDiagonalSum", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Side Diagonal", fontSize = 14.sp)
                        Text("= $sideDiagonalSum", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val validation = validateMagicSquare(grid.map { it.map { it.value } })
                resultText = if (validation == "OK") "✅ You win!" else "❌ $validation"
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Check", color = Color.White)
        }

        Text(resultText, color = Color.Red, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onBackPressed, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))) {
                Text("Exit", color = Color.White)
            }
            Button(onClick = { showRulesDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))) {
                Text("Rules", color = Color.White)
            }
        }
    }

    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            title = {
                Text("Rules of Magic Square", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "1. Fill the grid with numbers from 1 to N×N (no repeats).\n" +
                            "2. The sum of each row, column, and diagonal must be the same.\n" +
                            "3. Only numbers from 1 to 9 are allowed.\n" +
                            "4. Use the 'Check' button to validate your solution.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Justify
                )
            },
            confirmButton = {
                Button(onClick = { showRulesDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

fun generateMagicSquare(n: Int): List<List<Int>> {
    val square = MutableList(n) { MutableList(n) { 0 } }
    var i = 0
    var j = n / 2
    for (num in 1..n * n) {
        square[i][j] = num
        val newi = (i - 1 + n) % n
        val newj = (j + 1) % n
        if (square[newi][newj] != 0) {
            i = (i + 1) % n
        } else {
            i = newi
            j = newj
        }
    }
    return square
}

fun generateEmptyPositions(level: Int, size: Int): Set<Pair<Int, Int>> {
    val positions = mutableSetOf<Pair<Int, Int>>()
    while (positions.size < level) {
        positions.add(Random.nextInt(size) to Random.nextInt(size))
    }
    return positions
}

fun validateMagicSquare(grid: List<List<String>>): String {
    val n = grid.size
    val flatList = grid.flatten()
    val numbers = flatList.map { it.toIntOrNull() ?: return "All cells must be filled with digits 1-9." }

    if (numbers.any { it !in 1..9 }) return "Only numbers 1-9 are allowed."
    if (numbers.toSet().size != n * n) return "Each number from 1 to ${n * n} must be used exactly once."

    val matrix = grid.map { row -> row.map { it.toInt() } }
    val expectedSum = matrix[0].sum()

    for (i in 0 until n) {
        if (matrix[i].sum() != expectedSum) return "Row $i doesn't sum to $expectedSum."
        if (matrix.sumOf { it[i] } != expectedSum) return "Column $i doesn't sum to $expectedSum."
    }

    if ((0 until n).sumOf { matrix[it][it] } != expectedSum) return "Main diagonal doesn't sum to $expectedSum."
    if ((0 until n).sumOf { matrix[it][n - it - 1] } != expectedSum) return "Side diagonal doesn't sum to $expectedSum."

    return "OK"
}