package org.shakeit.app.data

import android.content.Context

class ShakePreferences(context: Context) {
    private val store = context.getSharedPreferences("shakeit", Context.MODE_PRIVATE)
    var listeningEnabled: Boolean
        get() = store.getBoolean("listening", false)
        set(value) { store.edit().putBoolean("listening", value).apply() }
    var calibration: Calibration
        get() = Calibration(store.getFloat("light", 1.35f), store.getFloat("medium", 1.9f), store.getFloat("hard", 2.4f))
        set(value) { store.edit().putFloat("light", value.light).putFloat("medium", value.medium).putFloat("hard", value.hard).apply() }
    fun shortcuts(): List<Shortcut> = (0 until store.getInt("shortcut_count", 3)).map { index -> val fallback = defaultShortcuts.getOrNull(index) ?: Shortcut(index, ShortcutAction.FLASHLIGHT, ShakeStrength.MEDIUM, 2); Shortcut(index, ShortcutAction.entries[store.getInt("shortcut_${index}_action", fallback.action.ordinal)], ShakeStrength.entries[store.getInt("shortcut_${index}_strength", fallback.strength.ordinal)], store.getInt("shortcut_${index}_shakes", fallback.shakes), store.getBoolean("shortcut_${index}_enabled", true)) }
    fun saveShortcuts(shortcuts: List<Shortcut>) { val edit = store.edit().putInt("shortcut_count", shortcuts.size); shortcuts.forEachIndexed { index, shortcut -> edit.putInt("shortcut_${index}_action", shortcut.action.ordinal).putInt("shortcut_${index}_strength", shortcut.strength.ordinal).putInt("shortcut_${index}_shakes", shortcut.shakes).putBoolean("shortcut_${index}_enabled", shortcut.enabled) }; edit.apply() }
    init { if (!store.contains("shortcut_count")) saveShortcuts(defaultShortcuts) }
}
