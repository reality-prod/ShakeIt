package org.shakeit.app.data

enum class ShakeStrength(val title: String) { LIGHT("Light"), MEDIUM("Medium"), HARD("Hard") }
enum class ShortcutAction(val title: String, val description: String) { WALLET("Wallet", "Open your default card"), FLASHLIGHT("Flashlight", "Toggle the torch"), DO_NOT_DISTURB("Do not disturb", "Toggle interruption filter"), SOUND("Sound mode", "Toggle ring and vibrate") }
data class Shortcut(val id: Int, val action: ShortcutAction, val strength: ShakeStrength, val shakes: Int, val enabled: Boolean = true)
data class Calibration(val light: Float = 1.35f, val medium: Float = 1.9f, val hard: Float = 2.4f)
val defaultShortcuts = listOf(Shortcut(0, ShortcutAction.WALLET, ShakeStrength.HARD, 1), Shortcut(1, ShortcutAction.FLASHLIGHT, ShakeStrength.MEDIUM, 2), Shortcut(2, ShortcutAction.DO_NOT_DISTURB, ShakeStrength.LIGHT, 4))
