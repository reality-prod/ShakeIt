package org.shakeit.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.shakeit.app.data.*

private val Ink = Color(0xFF211D2B); private val Muted = Color(0xFF6F6A78); private val Lilac = Color(0xFFEDE9FF)

@Composable fun ShakeItApp(preferences: ShakePreferences, openWalletOnStart: Boolean, onListeningChanged: (Boolean) -> Unit, onRequestWallet: (() -> Unit) -> Unit) {
    var tab by remember { mutableIntStateOf(0) }; var listening by remember { mutableStateOf(preferences.listeningEnabled) }; var shortcuts by remember { mutableStateOf(preferences.shortcuts()) }; var wallet by remember { mutableStateOf(false) }
    LaunchedEffect(openWalletOnStart) { if (openWalletOnStart) onRequestWallet { wallet = true } }
    Scaffold(bottomBar = { NavigationBar { listOf("Shortcuts" to Icons.Default.Bolt, "Calibrate" to Icons.Default.Tune, "Settings" to Icons.Default.Settings).forEachIndexed { index, pair -> NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Icon(pair.second, null) }, label = { Text(pair.first) }) } } }) { padding ->
        when (tab) { 0 -> HomeScreen(Modifier.padding(padding), preferences, listening, shortcuts, { listening = it; onListeningChanged(it) }, { shortcuts = it; preferences.saveShortcuts(it) }, { onRequestWallet { wallet = true } }); 1 -> CalibrationScreen(Modifier.padding(padding), preferences); else -> SettingsScreen(Modifier.padding(padding), listening, { listening = it; onListeningChanged(it) }) }
    }
    if (wallet) WalletDialog(onDismiss = { wallet = false })
}

@Composable private fun HomeScreen(modifier: Modifier, preferences: ShakePreferences, listening: Boolean, shortcuts: List<Shortcut>, onListening: (Boolean) -> Unit, onShortcuts: (List<Shortcut>) -> Unit, onWallet: () -> Unit) {
    var editing by remember { mutableStateOf<Shortcut?>(null) }; var adding by remember { mutableStateOf(false) }
    LazyColumn(modifier.fillMaxSize().background(Color(0xFFF7F5FB)).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 22.dp, bottom = 24.dp)) {
        item { Text("ShakeIt!", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Ink); Text("Shortcuts that move with you.", fontSize = 18.sp, color = Muted, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)) }
        item { Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = if (listening) Lilac else Color.White)) { Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(if (listening) "Listening for shakes" else "Shake detection is off", fontWeight = FontWeight.Bold, color = Ink, fontSize = 18.sp); Text(if (listening) "A visible foreground service is active" else "Turn this on to use shortcuts anywhere", color = Muted, modifier = Modifier.padding(top = 4.dp)) }; Switch(listening, onListening) } } }
        item { Text("Your shortcuts", fontWeight = FontWeight.Bold, color = Ink, fontSize = 20.sp, modifier = Modifier.padding(top = 4.dp)) }
        items(shortcuts, key = { it.id }) { shortcut -> ShortcutCard(shortcut, onClick = { if (shortcut.action == ShortcutAction.WALLET) onWallet() else editing = shortcut }) }
        item { OutlinedButton(onClick = { adding = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add shortcut") } }
        item { Text("Everything stays on-device. Wallet shortcuts always require Android biometrics.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
    }
    if (editing != null) ShortcutEditor(editing!!, onDismiss = { editing = null }, onSave = { updated -> onShortcuts(shortcuts.map { if (it.id == updated.id) updated else it }); editing = null }, onDelete = { onShortcuts(shortcuts.filterNot { it.id == editing!!.id }); editing = null })
    if (adding) ShortcutEditor(Shortcut(shortcuts.size, ShortcutAction.FLASHLIGHT, ShakeStrength.MEDIUM, 2), onDismiss = { adding = false }, onSave = { onShortcuts(shortcuts + it); adding = false }, onDelete = { adding = false })
}

@Composable private fun ShortcutCard(shortcut: Shortcut, onClick: () -> Unit) { Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(46.dp).background(Lilac, RoundedCornerShape(14.dp)), Alignment.Center) { Icon(if (shortcut.action == ShortcutAction.WALLET) Icons.Default.AccountBalanceWallet else if (shortcut.action == ShortcutAction.FLASHLIGHT) Icons.Default.FlashOn else if (shortcut.action == ShortcutAction.DO_NOT_DISTURB) Icons.Default.DoNotDisturb else Icons.Default.VolumeUp, null, tint = Color(0xFF7658F6)) }; Column(Modifier.padding(start = 14.dp).weight(1f)) { Text(shortcut.action.title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 17.sp); Text("${shortcut.strength.title} · ${shortcut.shakes} ${if (shortcut.shakes == 1) "shake" else "shakes"}", color = Muted, fontSize = 14.sp) }; Icon(Icons.Default.ChevronRight, "Edit", tint = Color(0xFF9A96A2)) } } }

@Composable private fun ShortcutEditor(initial: Shortcut, onDismiss: () -> Unit, onSave: (Shortcut) -> Unit, onDelete: () -> Unit) { var action by remember { mutableStateOf(initial.action) }; var strength by remember { mutableStateOf(initial.strength) }; var count by remember { mutableIntStateOf(initial.shakes) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit shortcut") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Action", fontWeight = FontWeight.Bold); ActionChips(action) { action = it }; Text("Shake level", fontWeight = FontWeight.Bold); StrengthChips(strength) { strength = it }; Text("Number of shakes: $count", fontWeight = FontWeight.Bold); Slider(count.toFloat(), { count = it.toInt().coerceIn(1, 6) }, valueRange = 1f..6f, steps = 4) } }, confirmButton = { TextButton({ onSave(initial.copy(action = action, strength = strength, shakes = count)) }) { Text("Save") } }, dismissButton = { Row { TextButton(onDelete) { Text("Delete") }; TextButton(onDismiss) { Text("Cancel") } } }) }
@Composable private fun ActionChips(selected: ShortcutAction, onSelect: (ShortcutAction) -> Unit) { Column { ShortcutAction.entries.forEach { item -> FilterChip(selected = selected == item, onClick = { onSelect(item) }, label = { Text(item.title) }, modifier = Modifier.padding(end = 4.dp)) } } }
@Composable private fun StrengthChips(selected: ShakeStrength, onSelect: (ShakeStrength) -> Unit) { Row { ShakeStrength.entries.forEach { item -> FilterChip(selected = selected == item, onClick = { onSelect(item) }, label = { Text(item.title) }, modifier = Modifier.padding(end = 4.dp)) } } }

@Composable private fun SettingsScreen(modifier: Modifier, listening: Boolean, onListening: (Boolean) -> Unit) { Column(modifier.fillMaxSize().background(Color(0xFFF7F5FB)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { Text("Settings", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Ink); Card(shape = RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Motion listener", fontWeight = FontWeight.Bold); Text("Keep shake shortcuts active in the background", color = Muted) }; Switch(listening, onListening) } }; Text("Privacy", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink); Text("ShakeIt! processes motion locally. It does not record, upload, or retain sensor data. The listener is always visible as an Android foreground-service notification.", color = Muted); Text("ShakeIt! 0.1 · AGPL-3.0-or-later", color = Muted, fontSize = 13.sp) } }

@Composable private fun WalletDialog(onDismiss: () -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text("Wallet unlocked") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Default card", color = Muted); Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF28223A)), shape = RoundedCornerShape(18.dp)) { Column(Modifier.fillMaxWidth().padding(20.dp)) { Text("SHAKEIT WALLET", color = Color.White, fontSize = 12.sp); Spacer(Modifier.height(28.dp)); Text("••••  4821", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold); Text("Default card", color = Color(0xFFD2C9FF), modifier = Modifier.padding(top = 8.dp)) } }; Text("This local wallet view is a secure shortcut surface. Payment-provider integration is intentionally out of scope.", color = Muted, fontSize = 12.sp) } }, confirmButton = { TextButton(onDismiss) { Text("Done") } }) }
