package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.security.DeviceIdentity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrientBorderBlue
import com.example.ui.theme.OrientCoralRed
import com.example.ui.theme.OrientDeepNavy
import com.example.ui.theme.OrientElectricBlue
import com.example.ui.theme.OrientSurfaceNavy
import com.example.ui.theme.OrientTextSecondary
import com.example.ui.theme.OrientThemeStyle
import com.example.ui.theme.OrientTurquoise
import com.example.ui.viewmodel.AppLanguage

@Composable
fun SettingsScreen(
    identity: DeviceIdentity,
    currentLanguage: AppLanguage,
    currentTheme: OrientThemeStyle,
    autoUpdatePlaylist: Boolean,
    parentalEnabled: Boolean,
    parentalPin: String,
    cacheSizeLabel: String,
    supabaseStatus: String,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectTheme: (OrientThemeStyle) -> Unit,
    onToggleAutoUpdate: (Boolean) -> Unit,
    onUpdateParental: (Boolean, String) -> Unit,
    onRefreshPlaylists: () -> Unit,
    onClearCache: () -> Unit,
    onResetApp: () -> Unit
) {
    var newPinInput by remember(parentalPin) { mutableStateOf(parentalPin) }
    var showResetConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Langue & Thème Visuel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OrientBorderBlue, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = OrientTurquoise)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Langue de l'Interface",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = currentLanguage == lang,
                                onClick = { onSelectLanguage(lang) },
                                label = { Text(lang.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrientElectricBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = OrientSurfaceNavy,
                                    labelColor = OrientTextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = OrientTurquoise)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thème Graphique ORIENT PLAYER",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrientThemeStyle.entries.forEach { style ->
                            FilterChip(
                                selected = currentTheme == style,
                                onClick = { onSelectTheme(style) },
                                label = { Text(style.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrientElectricBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = OrientSurfaceNavy,
                                    labelColor = OrientTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Mise à jour de Playlist & Synchronisation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OrientBorderBlue, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mise à jour automatique des Playlists & EPG",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Actualise automatiquement les flux M3U, Xtream Codes et Stalker Portal au démarrage",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrientTextSecondary
                            )
                        }
                        Switch(
                            checked = autoUpdatePlaylist,
                            onCheckedChange = onToggleAutoUpdate
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRefreshPlaylists,
                        colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue),
                        modifier = Modifier.testTag("settings_refresh_playlist_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Forcer la synchronisation des Playlists & EPG")
                    }
                }
            }
        }

        // 3. Contrôle Parental
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OrientBorderBlue, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = OrientTurquoise)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Contrôle Parental (PIN Sécurisé)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Verrouille les catégories adultes et sensibles par code PIN à 4 chiffres",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OrientTextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = parentalEnabled,
                            onCheckedChange = { onUpdateParental(it, newPinInput) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                            label = { Text("Code PIN à 4 chiffres") },
                            singleLine = true,
                            modifier = Modifier.width(200.dp)
                        )
                        Button(
                            onClick = { onUpdateParental(parentalEnabled, newPinInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue)
                        ) {
                            Text("Enregistrer PIN")
                        }
                    }
                }
            }
        }

        // 4. Gestion du Cache & Réinitialisation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OrientBorderBlue, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Gestion du Cache Vidéo & EPG",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Taille actuelle du cache : $cacheSizeLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrientTurquoise
                            )
                        }

                        OutlinedButton(
                            onClick = onClearCache,
                            modifier = Modifier.testTag("settings_clear_cache_btn")
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = OrientTurquoise)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Vider le Cache", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.testTag("settings_reset_app_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = OrientCoralRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Réinitialiser l'application & Régénérer la MAC virtuelle", color = OrientCoralRed)
                    }
                }
            }
        }

        // 5. Informations de l'Application & Appareil
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OrientBorderBlue, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = OrientTurquoise)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Informations Système ORIENT PLAYER",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adresse MAC Virtuelle : ${identity.virtualMac}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrientTurquoise
                    )
                    Text(
                        text = "Code d'Activation : ${identity.activationCode}  •  Clé Appareil : ${identity.deviceKey}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                    Text(
                        text = "ID d'Installation : ${identity.installationId}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = OrientTextSecondary
                    )
                    Text(
                        text = "Plateforme : ${identity.deviceType} (${identity.deviceModel}) • Moteur : AndroidX Media3 1.5.1",
                        style = MaterialTheme.typography.bodySmall,
                        color = OrientTextSecondary
                    )
                    Text(
                        text = "Backend : $supabaseStatus",
                        style = MaterialTheme.typography.bodySmall,
                        color = OrientTurquoise
                    )
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Réinitialiser ORIENT PLAYER ?") },
            text = {
                Text("Cette action réinitialisera les playlists locales, générera une nouvelle adresse MAC virtuelle et restaurera la configuration d'usine.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetApp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrientCoralRed)
                ) {
                    Text("Confirmer la Réinitialisation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
