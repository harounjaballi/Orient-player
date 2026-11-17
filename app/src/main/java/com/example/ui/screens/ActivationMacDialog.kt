package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.security.DeviceIdentity
import com.example.core.security.QrCodeGenerator
import com.example.data.local.DeviceEntity
import com.example.data.local.SubscriptionPlan
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrientBorderBlue
import com.example.ui.theme.OrientCardNavy
import com.example.ui.theme.OrientCoralRed
import com.example.ui.theme.OrientDeepNavy
import com.example.ui.theme.OrientElectricBlue
import com.example.ui.theme.OrientEmerald
import com.example.ui.theme.OrientGold
import com.example.ui.theme.OrientMidnightDark
import com.example.ui.theme.OrientTextSecondary
import com.example.ui.theme.OrientTurquoise
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivationMacDialog(
    identity: DeviceIdentity,
    deviceRecord: DeviceEntity?,
    supabaseStatus: String,
    onDismiss: () -> Unit,
    onSyncBackend: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onCopyFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val qrBitmap = remember(identity.virtualMac, identity.activationCode, identity.deviceKey) {
        QrCodeGenerator.generateActivationQrBitmap(
            virtualMac = identity.virtualMac,
            activationCode = identity.activationCode,
            deviceKey = identity.deviceKey,
            sizePx = 420
        )
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }
    val statusStr = deviceRecord?.status ?: "ACTIVE"
    val planLabel = SubscriptionPlan.entries.find { it.name == deviceRecord?.subscriptionPlan }?.label
        ?: "Abonnement 12 Mois"
    val expDateFormatted = if (deviceRecord != null) {
        dateFormatter.format(Date(deviceRecord.expirationDateMillis))
    } else {
        "Permanent"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 760.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(OrientElectricBlue, OrientTurquoise)),
                    shape = RoundedCornerShape(24.dp)
                ),
            color = OrientMidnightDark
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(OrientElectricBlue, OrientTurquoise))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = OrientMidnightDark,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "IDENTIFICATION & ACTIVATION MAC",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Système sécurisé d'association d'appareil ORIENT PLAYER",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrientTurquoise
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_mac_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = OrientBorderBlue)
                Spacer(modifier = Modifier.height(18.dp))

                // Main Content: QR Code + MAC Credentials
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Scannable QR Code Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.border(1.dp, OrientBorderBlue, RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR Code Activation MAC ${identity.virtualMac}",
                                modifier = Modifier
                                    .size(168.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, OrientTurquoise, RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "SCANNER POUR ACTIVER",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrientTurquoise,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Right: Virtual MAC, Activation Code, Device Key, Installation ID
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Virtual MAC Box
                        Surface(
                            color = OrientCardNavy,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OrientTurquoise.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ADRESSE MAC VIRTUELLE STABLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OrientTextSecondary
                                    )
                                    Text(
                                        text = identity.virtualMac,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = OrientTurquoise
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        copyToClipboard(context, "MAC Orient Player", identity.virtualMac)
                                        onCopyFeedback("Adresse MAC ${identity.virtualMac} copiée dans le presse-papiers")
                                    },
                                    modifier = Modifier.testTag("copy_mac_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copier l'adresse MAC",
                                        tint = OrientTurquoise
                                    )
                                }
                            }
                        }

                        // Activation Code & Device Key Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = OrientDeepNavy,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, OrientBorderBlue, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "CODE D'ACTIVATION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OrientTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = identity.activationCode,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = OrientGold
                                    )
                                }
                            }

                            Surface(
                                color = OrientDeepNavy,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, OrientBorderBlue, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "CLÉ APPAREIL (KEY)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OrientTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = identity.deviceKey,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Installation UUID & Device Info
                        Surface(
                            color = OrientDeepNavy,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OrientBorderBlue, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "ID D'INSTALLATION UNIQUE : ${identity.installationId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrientTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = OrientTurquoise,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${identity.deviceType} • ${identity.deviceModel} • Token HMAC: ${identity.securityToken}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subscription & Backend Verification Status Box
                Surface(
                    color = OrientDeepNavy,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OrientBorderBlue, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (statusStr == "ACTIVE") Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (statusStr == "ACTIVE") OrientEmerald else OrientCoralRed,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Statut : $statusStr • $planLabel (Expire le $expDateFormatted)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = supabaseStatus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OrientTextSecondary
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onSyncBackend,
                                modifier = Modifier.testTag("sync_mac_backend_button")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = OrientTurquoise, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vérifier Activation", color = Color.White)
                            }

                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenAdminPanel()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue),
                                modifier = Modifier.testTag("open_admin_from_mac_button")
                            ) {
                                Text("Panneau Admin")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}
