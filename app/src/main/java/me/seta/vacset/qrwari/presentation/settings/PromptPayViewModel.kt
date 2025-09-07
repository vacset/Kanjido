package me.seta.vacset.qrwari.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.seta.vacset.qrwari.data.storage.Prefs

class PromptPayViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)

    // null if unset
    val promptPayIdFlow = prefs.promptPayIdFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setPromptPayId(id: String) {
        viewModelScope.launch { prefs.setPromptPayId(id.trim()) }
    }
}
