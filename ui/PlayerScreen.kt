package com.example.vibeplayer.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(vm: PlayerViewModel) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("Трек не выбран") }
    var lrcText by remember { mutableStateOf<String?>(null) }

    val pickAudio = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            audioUri = uri
            title = displayName(ctx, uri) ?: "Трек"
            vm.load(uri, title, lrcText)
        }
    }
    val pickLrc = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            lrcText = ctx.contentResolver.openInputStream(uri)
                ?.bufferedReader()?.use { it.readText() }
            audioUri?.let { vm.load(it, title, lrcText) }
        }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.currentLine) {
        if (state.currentLine >= 0)
            scope.launch { listState.animateScrollToItem(state.currentLine) }
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF07070F)).padding(16.dp)
    ) {
        Text(title, color = Color(0xFFE8E8FF), fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { pickAudio.launch(arrayOf("audio/*")) },
                modifier = Modifier.weight(1f)) { Text("Аудио") }
            Button(onClick = { pickLrc.launch(arrayOf("*/*")) },
                modifier = Modifier.weight(1f)) { Text("LRC") }
        }

        Spacer(Modifier.height(12.dp))

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (state.lyrics.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Выбери .mp3 и .lrc с тем же текстом",
                        color = Color(0x88E8E8FF))
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 120.dp)
                ) {
                    itemsIndexed(state.lyrics) { idx, line ->
                        val active = idx == state.currentLine
                        Text(
                            text = line.text,
                            color = if (active) Color(0xFF22D3EE) else Color(0x99E8E8FF),
                            fontSize = if (active) 22.sp else 18.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.fillMaxWidth()
                                .clickable { vm.seekToLine(idx) }
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        Slider(
            value = if (state.durationMs > 0)
                state.positionMs.toFloat() / state.durationMs else 0f,
            onValueChange = { f -> vm.seekTo((f * state.durationMs).toLong()) },
            modifier = Modifier.fillMaxWidth()
        )

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(onClick = { vm.toggle() }, modifier = Modifier.size(72.dp)) {
                Text(if (state.isPlaying) "❚❚" else "▶", fontSize = 24.sp)
            }
        }
    }
}

private fun displayName(ctx: Context, uri: Uri): String? {
    ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
        val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (i >= 0 && c.moveToFirst()) return c.getString(i)
    }
    return null
}
