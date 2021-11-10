package com.github.capntrips.vbmetapatcher

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.capntrips.vbmetapatcher.ui.theme.Orange500
import com.github.capntrips.vbmetapatcher.ui.theme.VbmetaPatcherTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MainContent(viewModel: MainViewModelInterface) {
    val uiState by viewModel.uiState.collectAsState()
    Column {
        DataCard (title = stringResource(R.string.device)) {
            DataRow(
                label = stringResource(R.string.model),
                value = "${Build.MODEL} (${Build.DEVICE})"
            )
            DataRow(
                label = stringResource(R.string.build_number),
                value = Build.ID
            )
            DataRow(
                label = stringResource(R.string.slot_suffix),
                value = uiState.slotSuffix
            )
        }
        Spacer(Modifier.height(16.dp))
        SlotCard(
            title = "vbmeta_a",
            slotStateFlow = uiState.slotA,
            isActive = uiState.slotSuffix == "_a"
        )
        Spacer(Modifier.height(16.dp))
        SlotCard(
            title = "vbmeta_b",
            slotStateFlow = uiState.slotB,
            isActive = uiState.slotSuffix == "_b"
        )
    }
}

@Composable
fun SlotCard(
    title: String,
    slotStateFlow: StateFlow<SlotStateInterface>,
    isActive: Boolean
) {
    val slot by slotStateFlow.collectAsState()
    val isRefreshing by slot.isRefreshing.collectAsState()
    DataCard (
        title = title,
        button = {
            if (!isRefreshing) {
                if (isActive) {
                    if (slot.patchStatus == PatchStatus.Patched) {
                        RestoreButton(slot)
                    } else {
                        PatchButton(slot)
                    }
                } else {
                    if (slot.patchStatus == PatchStatus.Stock) {
                        PatchButton(slot)
                    }
                }
            }
        }
    ) {
        IsPatchedDataRow(
            label = stringResource(R.string.status),
            isPatched = slot.patchStatus == PatchStatus.Patched
        )
        DataRow(
            label = "SHA1",
            value = slot.sha1.substring(0, 8),
            valueStyle = MaterialTheme.typography.titleSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Thin
            )
        )
    }
}

@Composable
fun PatchButton(slot: SlotStateInterface) {
    val context = LocalContext.current
    Button(
        modifier = Modifier.padding(0.dp),
        shape = RoundedCornerShape(4.0.dp),
        onClick = { slot.patch(context) }
    ) {
        Text(stringResource(R.string.patch))
    }
}

@Composable
fun RestoreButton(slot: SlotStateInterface) {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    TextButton(
        modifier = Modifier.padding(0.dp),
        shape = RoundedCornerShape(4.0.dp),
        contentPadding = PaddingValues(
            horizontal = ButtonDefaults.ContentPadding.calculateLeftPadding(LayoutDirection.Ltr) - (6.667).dp,
            vertical = ButtonDefaults.ContentPadding.calculateTopPadding()
        ),
        onClick = { openDialog.value = true }
    ) {
        Text(stringResource(R.string.restore))
    }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            icon = {
                Icon(
                    modifier = Modifier.width(48.dp).height(48.dp),
                    imageVector = Icons.Filled.Warning,
                    tint = Orange500,
                    contentDescription = stringResource(R.string.warning)
                )
            },
            title = { Text(stringResource(R.string.warning)) },
            text = { Text(stringResource(R.string.do_not_reboot)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        slot.restore(context)
                    }
                ) {
                    Text(stringResource(R.string.proceed))
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun DataCard(
    title: String,
    button: @Composable (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(0.dp, 9.dp, 8.dp, 9.dp),
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            if (button != null) {
                button()
            }
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

// TODO: Remove when card is supported in material3: https://m3.material.io/components/cards/implementation/android
@Composable
fun Card(
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    tonalElevation: Dp = 2.dp,
    shadowElevation: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp, (13.788).dp, 18.dp, 18.dp),
            content = content
        )
    }
}

@Composable
fun DataRow(
    label: String,
    value: String,
    valueStyle: TextStyle = MaterialTheme.typography.titleSmall
) {
    Row {
        Text(
            modifier = Modifier.alignByBaseline(),
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.width(8.dp))
        Text(
            modifier = Modifier.alignByBaseline(),
            text = value,
            style = valueStyle
        )
    }
}

@Composable
fun HasStatusDataRow(
    label: String,
    value: String,
    hasStatus: Boolean
) {
    Row {
        val color = if (hasStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        Text(
            modifier = Modifier.alignByBaseline(),
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.width(8.dp))
        Text(
            modifier = Modifier.alignByBaseline(),
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color
        )
    }
}

@Composable
fun IsPatchedDataRow(
    label: String,
    isPatched: Boolean
) {
    val text = stringResource(if (isPatched) R.string.patched else R.string.stock)
    HasStatusDataRow(
        label = label,
        value = text,
        hasStatus = isPatched
    )
}

@ExperimentalMaterial3Api
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MainContentPreviewDark() {
    MainContentPreviewLight()
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun MainContentPreviewLight() {
    VbmetaPatcherTheme {
        Scaffold {
            val viewModel: MainViewModelPreview = viewModel()
            MainContent(viewModel)
        }
    }
}
