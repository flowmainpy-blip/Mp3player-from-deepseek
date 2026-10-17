package com.example.vibeplayer.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.vibeplayer.data.LrcParser
import com.example.vibeplayer.data.LyricLine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val title: String = "Трек не выбран",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val lyrics: List<LyricLine> = emptyList(),
    val currentLine: Int = -1
)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {
    private val player = ExoPlayer.Builder(app).build()
    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()
    private var posJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(p: Boolean) {
                _state.value = _state.value.copy(isPlaying = p)
            }
            override fun onPlaybackStateChanged(s: Int) {
                if (s == Player.STATE_READY)
                    _state.value = _state.value.copy(durationMs = player.duration)
            }
        })
        posJob = viewModelScope.launch {
            while (true) {
                val pos = player.currentPosition
                val lyrics = _state.value.lyrics
                val idx = lyrics.indexOfLast { it.timeMs <= pos }
                _state.value = _state.value.copy(
                    positionMs = pos,
                    currentLine = if (idx >= 0) idx else -1
                )
                delay(80)
            }
        }
    }

    fun load(audio: Uri, title: String, lrcText: String?) {
        player.setMediaItem(MediaItem.fromUri(audio))
        player.prepare()
        _state.value = _state.value.copy(
            title = title,
            lyrics = lrcText?.let(LrcParser::parse) ?: emptyList(),
            currentLine = -1,
            durationMs = 0
        )
    }

    fun toggle() { if (player.isPlaying) player.pause() else player.play() }
    fun seekTo(ms: Long) { player.seekTo(ms) }
    fun seekToLine(i: Int) { _state.value.lyrics.getOrNull(i)?.let { player.seekTo(it.timeMs) } }

    override fun onCleared() {
        posJob?.cancel(); player.release(); super.onCleared()
    }
}
