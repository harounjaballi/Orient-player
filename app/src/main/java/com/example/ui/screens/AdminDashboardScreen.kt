package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.security.DeviceIdentity
import com.example.data.local.ActivationLogEntity
import com.example.data.local.DeviceEntity
import com.example.data.local.DeviceStatus
import com.example.data.local.PlaylistConfigEntity
import com.example.data.local.PlaylistProtocol
import com.example.data.local.SubscriptionPlan
import com.example.data.remote.SupabaseService
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrientBorderBlue
import com.example.ui.theme.OrientCardNavy
import com.example.ui.theme.OrientCoralRed
import com.example.ui.theme.OrientDeepNavy
import com.example.ui.theme.OrientElectricBlue
import com.example.ui.theme.OrientEmerald
import com.example.ui.theme.OrientGold
import com.example.ui.theme.OrientMidnightDark
import com.example.ui.theme.OrientSurfaceNavy
import com.example.ui.theme.OrientTextSecondary
import com.example.ui.theme.OrientTurquoise
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AdminSubSection(val label: String) {
    OVERVIEW("Dashboard & KPIs"),
    DEVICES("Gestion Appareils MAC"),
    PLAYLISTS("Gestion Playlists"),
    SUBSCRIPTIONS("Gestion Abonnements"),
    LOGS("Historique & Sécurité")
}

@Composable
fun AdminDashboardScreen(
    currentIdentity: DeviceIdentity,
    devices: List<DeviceEntity>,
    playlists: List<PlaylistConfigEntity>,
    logs: List<ActivationLogEntity>,
    supabaseStatus: String,
    onSyncSupabase: () -> Unit,
    onSaveDevice: (mac: String, name: String, model: String, type: String, plan: SubscriptionPlan, status: DeviceStatus) -> Unit,
    onChangeDeviceStatus: (DeviceEntity, DeviceStatus) -> Unit,
    onRenewSubscription: (DeviceEntity, SubscriptionPlan) -> Unit,
    onAddPlaylist: (mac: String, name: String, protocol: PlaylistProtocol, url: String, user: String, pass: String, stalkerMac: String, autoUpdate: Boolean) -> Unit,
    onImportLocalM3u: (Uri, String) -> Unit,
    onDeletePlaylist: (PlaylistConfigEntity) -> Unit,
    onPostMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var activeSection by remember { mutableStateOf(AdminSubSection.OVERVIEW) }
    var macSearchQuery by remember { mutableStateOf("") }

    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var showAddPlaylistDialog by remember { mutableStateOf(false) }
    var showSqlSchemaDialog by remember { mutableStateOf(false) }
    var preselectedMacForPlaylist by remember { mutableStateOf(currentIdentity.virtualMac) }

    val m3uFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportLocalM3u(uri, "Playlist M3U Importée")
        }
    }

    val totalDevices = devices.size
    val activeDevices = devices.count { it.status == DeviceStatus.ACTIVE.name }
    val expiredDevices = devices.count { it.status == DeviceStatus.EXPIRED.name }
    val blockedDevices = devices.count { it.status == DeviceStatus.BLOCKED.name }

    val filteredDevices = remember(devices, macSearchQuery) {
        if (macSearchQuery.isBlank()) {
            devices
        } else {
            devices.filter {
                it.virtualMac.contains(macSearchQuery, ignoreCase = true) ||
                    it.clientName.contains(macSearchQuery, ignoreCase = true) ||
                    it.activationCode.contains(macSearchQuery, ignoreCase = true)
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    val shortDateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen")
    ) {
        // Sub-navigation Tabs for the Web Admin Console
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(AdminSubSection.entries) { section ->
                FilterChip(
                    selected = activeSection == section,
                    onClick = { activeSection = section },
                    label = { Text(section.label, style = MaterialTheme.typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrientElectricBlue,
                        selectedLabelColor = Color.White,
                        containerColor = OrientSurfaceNavy,
                        labelColor = OrientTextSecondary
                    ),
                    modifier = Modifier.testTag("admin_tab_${section.name}")
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Cloud Status & Quick Actions Bar
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OrientBorderBlue, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PANNEAU D'ADMINISTRATION WEB & CLOUD SUPABASE",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = supabaseStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = OrientTurquoise
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showSqlSchemaDialog = true },
                                modifier = Modifier.testTag("admin_sql_schema_button")
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = OrientTurquoise, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SQL Supabase", color = Color.White)
                            }

                            Button(
                                onClick = onSyncSupabase,
                                colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue),
                                modifier = Modifier.testTag("admin_sync_cloud_button")
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Cloud")
                            }
                        }
                    }
                }
            }

            // 1. KPI CARDS (Always visible in OVERVIEW)
            if (activeSection == AdminSubSection.OVERVIEW) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminKpiCard(
                            title = "Total Appareils",
                            count = totalDevices,
                            accentColor = OrientTurquoise,
                            icon = Icons.Default.Devices,
                            modifier = Modifier.weight(1f)
                        )
                        AdminKpiCard(
                            title = "Appareils Actifs",
                            count = activeDevices,
                            accentColor = OrientEmerald,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        AdminKpiCard(
                            title = "Expirés",
                            count = expiredDevices,
                            accentColor = OrientGold,
                            icon = Icons.Default.WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        AdminKpiCard(
                            title = "Bloqués",
                            count = blockedDevices,
                            accentColor = OrientCoralRed,
                            icon = Icons.Default.Block,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Quick Action Bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showAddDeviceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_add_device_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ajouter / Activer MAC")
                        }

                        Button(
                            onClick = {
                                preselectedMacForPlaylist = currentIdentity.virtualMac
                                showAddPlaylistDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrientCardNavy),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, OrientTurquoise, RoundedCornerShape(24.dp))
                                .testTag("admin_add_playlist_btn")
                        ) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = OrientTurquoise)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Associer Playlist (M3U / Xtream / Stalker)", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { m3uFilePicker.launch(arrayOf("*/*")) },
                            modifier = Modifier.testTag("admin_import_m3u_file_btn")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = OrientGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importer .M3U", color = Color.White)
                        }
                    }
                }
            }

            // 2. DEVICE MANAGEMENT SECTION (Shown in OVERVIEW and DEVICES)
            if (activeSection == AdminSubSection.OVERVIEW || activeSection == AdminSubSection.DEVICES) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GESTION DES APPAREILS PAR ADRESSE MAC (${filteredDevices.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showAddDeviceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nouvel Appareil")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = macSearchQuery,
                        onValueChange = { macSearchQuery = it },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OrientTurquoise) },
                        placeholder = { Text("Rechercher par adresse MAC (ex: 00:1A:79...), nom ou code d'activation…") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_mac_search_input")
                    )
                }

                items(filteredDevices, key = { it.virtualMac }) { dev ->
                    val statusColor = when (dev.status) {
                        DeviceStatus.ACTIVE.name -> OrientEmerald
                        DeviceStatus.EXPIRED.name -> OrientGold
                        else -> OrientCoralRed
                    }
                    val planObj = SubscriptionPlan.entries.find { it.name == dev.subscriptionPlan } ?: SubscriptionPlan.MONTH_12

                    Card(
                        colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OrientBorderBlue, RoundedCornerShape(14.dp))
                            .testTag("admin_device_row_${dev.virtualMac}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dev.virtualMac,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OrientTurquoise
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Surface(
                                        color = statusColor.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = dev.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Code: ${dev.activationCode} • Clé: ${dev.deviceKey}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrientGold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${dev.clientName} • ${dev.deviceType} (${dev.deviceModel})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Forfait : ${planObj.label} • Début : ${shortDateFormatter.format(Date(dev.startDateMillis))} • Expire : ${shortDateFormatter.format(Date(dev.expirationDateMillis))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrientTextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Buttons: Activate, Expire/Deactivate, Block, Associate Playlist
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    OutlinedButton(
                                        onClick = { onChangeDeviceStatus(dev, DeviceStatus.ACTIVE) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Activer", color = OrientEmerald, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                item {
                                    OutlinedButton(
                                        onClick = { onChangeDeviceStatus(dev, DeviceStatus.EXPIRED) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Désactiver / Expirer", color = OrientGold, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                item {
                                    OutlinedButton(
                                        onClick = { onChangeDeviceStatus(dev, DeviceStatus.BLOCKED) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Bloquer MAC", color = OrientCoralRed, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                item {
                                    OutlinedButton(
                                        onClick = {
                                            preselectedMacForPlaylist = dev.virtualMac
                                            showAddPlaylistDialog = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("+ Associer Playlist", color = OrientTurquoise, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. SUBSCRIPTIONS MANAGEMENT SECTION
            if (activeSection == AdminSubSection.SUBSCRIPTIONS || activeSection == AdminSubSection.OVERVIEW) {
                item {
                    Text(
                        text = "GESTION DES ABONNEMENTS & RENOUVELLEMENTS",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cliquez sur une formule pour renouveler ou activer instantanément l'appareil sélectionné",
                        style = MaterialTheme.typography.bodySmall,
                        color = OrientTextSecondary
                    )
                }

                items(filteredDevices, key = { "sub_${it.virtualMac}" }) { dev ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OrientCardNavy),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OrientBorderBlue, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${dev.virtualMac} — ${dev.clientName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Expire le ${shortDateFormatter.format(Date(dev.expirationDateMillis))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrientGold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val plans = listOf(
                                    SubscriptionPlan.MONTH_1,
                                    SubscriptionPlan.MONTH_3,
                                    SubscriptionPlan.MONTH_6,
                                    SubscriptionPlan.MONTH_12,
                                    SubscriptionPlan.PERMANENT
                                )
                                items(plans) { plan ->
                                    val isCurrentPlan = dev.subscriptionPlan == plan.name
                                    Button(
                                        onClick = { onRenewSubscription(dev, plan) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCurrentPlan) OrientElectricBlue else OrientDeepNavy
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = plan.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isCurrentPlan) Color.White else OrientTurquoise
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. PLAYLISTS MANAGEMENT SECTION
            if (activeSection == AdminSubSection.PLAYLISTS || activeSection == AdminSubSection.OVERVIEW) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PLAYLISTS ASSOCIÉES AUX ADRESSES MAC (${playlists.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                preselectedMacForPlaylist = currentIdentity.virtualMac
                                showAddPlaylistDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue)
                        ) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ajouter Playlist")
                        }
                    }
                }

                items(playlists, key = { "pl_${it.id}" }) { pl ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OrientBorderBlue, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = OrientElectricBlue.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = pl.protocol,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OrientTurquoise,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "MAC : ${pl.deviceMac}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OrientGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = pl.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Serveur / URL : ${pl.serverUrl}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OrientTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${pl.channelCount} Chaînes Direct • ${pl.vodCount} Films VOD • ${pl.seriesCount} Séries",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrientEmerald
                                )
                            }

                            IconButton(
                                onClick = { onDeletePlaylist(pl) },
                                modifier = Modifier.testTag("delete_playlist_btn_${pl.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = OrientCoralRed)
                            }
                        }
                    }
                }
            }

            // 5. ACTIVATION & SECURITY AUDIT LOGS
            if (activeSection == AdminSubSection.LOGS || activeSection == AdminSubSection.OVERVIEW) {
                item {
                    Text(
                        text = "JOURNAL DES ACTIVATIONS & SÉCURITÉ (${logs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(logs.take(15), key = { "log_${it.id}" }) { log ->
                    Surface(
                        color = OrientDeepNavy,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OrientBorderBlue.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = OrientTurquoise,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "[${log.actionType}] MAC ${log.deviceMac} • ${log.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "${dateFormatter.format(Date(log.timestampMillis))} • Par ${log.performedBy}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrientTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Device MAC Dialog
    if (showAddDeviceDialog) {
        AddDeviceAdminDialog(
            defaultMac = currentIdentity.virtualMac,
            onDismiss = { showAddDeviceDialog = false },
            onConfirm = { mac, clientName, model, type, plan, status ->
                onSaveDevice(mac, clientName, model, type, plan, status)
                showAddDeviceDialog = false
            }
        )
    }

    // Add Playlist (M3U / Xtream Codes / Stalker Portal) Dialog
    if (showAddPlaylistDialog) {
        AddPlaylistAdminDialog(
            initialMac = preselectedMacForPlaylist,
            onDismiss = { showAddPlaylistDialog = false },
            onConfirm = { mac, name, protocol, url, user, pass, stalkerMac, autoUpdate ->
                onAddPlaylist(mac, name, protocol, url, user, pass, stalkerMac, autoUpdate)
                showAddPlaylistDialog = false
            }
        )
    }

    // Supabase PostgreSQL SQL Schema Viewer & Copier Dialog
    if (showSqlSchemaDialog) {
        Dialog(
            onDismissRequest = { showSqlSchemaDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 760.dp)
                    .height(560.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.5.dp, OrientTurquoise, RoundedCornerShape(22.dp)),
                color = OrientMidnightDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SCHÉMA SQL SUPABASE POSTGRESQL",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Configurez SUPABASE_URL & SUPABASE_ANON_KEY dans le panneau Secrets d'AI Studio",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrientTurquoise
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(
                                    ClipData.newPlainText("Supabase SQL Orient Player", SupabaseService.SUPABASE_SQL_SCHEMA)
                                )
                                onPostMessage("Script SQL Supabase copié dans le presse-papiers")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copier le SQL")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = OrientDeepNavy,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .border(1.dp, OrientBorderBlue, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(14.dp)
                        ) {
                            Text(
                                text = SupabaseService.SUPABASE_SQL_SCHEMA.trim(),
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.bodySmall,
                                color = OrientTurquoise
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSqlSchemaDialog = false }) {
                            Text("Fermer", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
    title: String,
    count: Int,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$count",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = OrientTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AddDeviceAdminDialog(
    defaultMac: String,
    onDismiss: () -> Unit,
    onConfirm: (mac: String, clientName: String, model: String, type: String, plan: SubscriptionPlan, status: DeviceStatus) -> Unit
) {
    var mac by remember { mutableStateOf(defaultMac) }
    var clientName by remember { mutableStateOf("Client VIP Orient") }
    var model by remember { mutableStateOf("Android TV Box 4K") }
    var selectedPlan by remember { mutableStateOf(SubscriptionPlan.MONTH_12) }
    var selectedStatus by remember { mutableStateOf(DeviceStatus.ACTIVE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter / Activer un Appareil MAC") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = mac,
                    onValueChange = { mac = it },
                    label = { Text("Adresse MAC Virtuelle (ex: 00:1A:79:XX:XX:XX)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nom du Client / Salon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Modèle d'appareil (TV, Box, Smartphone)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Formule d'Abonnement :", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SubscriptionPlan.entries) { plan ->
                        FilterChip(
                            selected = selectedPlan == plan,
                            onClick = { selectedPlan = plan },
                            label = { Text(plan.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mac.isNotBlank()) {
                        onConfirm(mac, clientName, model, "Android TV / Box", selectedPlan, selectedStatus)
                    }
                }
            ) {
                Text("Enregistrer & Activer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun AddPlaylistAdminDialog(
    initialMac: String,
    onDismiss: () -> Unit,
    onConfirm: (mac: String, name: String, protocol: PlaylistProtocol, url: String, user: String, pass: String, stalkerMac: String, autoUpdate: Boolean) -> Unit
) {
    var mac by remember { mutableStateOf(initialMac) }
    var name by remember { mutableStateOf("Bouquet Orient Pro") }
    var selectedProtocol by remember { mutableStateOf(PlaylistProtocol.XTREAM) }
    var serverUrl by remember { mutableStateOf("http://line.orient-iptv.pro:8080") }
    var username by remember { mutableStateOf("orient_user") }
    var password by remember { mutableStateOf("orient_pass") }
    var stalkerMac by remember { mutableStateOf(initialMac) }
    var autoUpdate by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Associer une Playlist à une MAC") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Protocole IPTV :", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PlaylistProtocol.entries.forEach { proto ->
                        FilterChip(
                            selected = selectedProtocol == proto,
                            onClick = {
                                selectedProtocol = proto
                                serverUrl = when (proto) {
                                    PlaylistProtocol.M3U -> "https://iptv-org.github.io/iptv/index.m3u"
                                    PlaylistProtocol.XTREAM -> "http://line.orient-iptv.pro:8080"
                                    PlaylistProtocol.STALKER -> "http://mag.orient-portal.tv/stalker_portal/c/"
                                }
                            },
                            label = { Text(proto.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = mac,
                    onValueChange = { mac = it },
                    label = { Text("Adresse MAC cible") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la Playlist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = {
                        Text(
                            when (selectedProtocol) {
                                PlaylistProtocol.M3U -> "URL de la Playlist M3U / M3U8"
                                PlaylistProtocol.XTREAM -> "Server URL Xtream Codes"
                                PlaylistProtocol.STALKER -> "Portal URL Stalker / Ministra"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedProtocol == PlaylistProtocol.XTREAM) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username Xtream") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password Xtream") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedProtocol == PlaylistProtocol.STALKER) {
                    OutlinedTextField(
                        value = stalkerMac,
                        onValueChange = { stalkerMac = it },
                        label = { Text("Adresse MAC virtuelle du Portail Stalker") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mise à jour automatique au démarrage", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = autoUpdate, onCheckedChange = { autoUpdate = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mac.isNotBlank() && serverUrl.isNotBlank()) {
                        onConfirm(mac, name, selectedProtocol, serverUrl, username, password, stalkerMac, autoUpdate)
                    }
                },
                modifier = Modifier.testTag("confirm_add_playlist_dialog_btn")
            ) {
                Text("Associer & Synchroniser")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
