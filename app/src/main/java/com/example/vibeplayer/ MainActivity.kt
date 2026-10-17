package com.example.vibeplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vibeplayer.ui.PlayerScreen
import com.example.vibeplayer.ui.PlayerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF07070F)),
                    color = Color(0xFF07070F)
                ) {
                    val vm: PlayerViewModel = viewModel()
                    PlayerScreen(vm)
                }
            }
        }
    }
}
