package me.seta.vacset.qrwari.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("kanjido_prefs")
object PrefsKeys { val PROMPTPAY_ID = stringPreferencesKey("promptpay_id") }

class Prefs(private val ctx: Context) {
    val promptPayIdFlow = ctx.dataStore.data.map { it[PrefsKeys.PROMPTPAY_ID] }
    suspend fun setPromptPayId(id: String) = ctx.dataStore.edit { it[PrefsKeys.PROMPTPAY_ID] = id }
}
