package com.charmings.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.charmings.app.ui.components.CocoaShadow
import com.charmings.app.ui.components.cozyCard
import com.charmings.app.ui.theme.Cocoa
import com.charmings.app.ui.theme.CocoaSoft
import com.charmings.app.ui.theme.Honey
import com.charmings.app.ui.theme.Paper

const val PRIVACY_POLICY_URL = "https://thetix.org/privacy-policy.html"

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val version = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull()

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))
            Column(Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
                Text("Налаштування", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))
                Column(Modifier.fillMaxWidth().cozyCard(corner = 28.dp).padding(8.dp)) {
                    SettingsRow(Icons.Rounded.Lock, "Політика конфіденційності") {
                        uriHandler.openUri(PRIVACY_POLICY_URL)
                    }
                }
                if (version != null) {
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "Чарівнятка $version",
                        style = MaterialTheme.typography.bodySmall,
                        color = CocoaSoft,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }

        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Paper, contentColor = Cocoa),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 8.dp)
                .shadow(4.dp, CircleShape, ambientColor = CocoaShadow, spotColor = CocoaShadow)
        ) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).background(Honey.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = Cocoa, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = CocoaSoft)
    }
}
