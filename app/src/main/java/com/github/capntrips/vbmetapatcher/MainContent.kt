package com.github.capntrips.vbmetapatcher

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.capntrips.vbmetapatcher.ui.theme.VbmetaPatcherTheme

@ExperimentalMaterial3Api
@Composable
fun MainContent(
    viewModel: MainViewModelInterface,
) {
    Column {
        val isPatchedA by viewModel.isPatchedA.collectAsState()
        val isPatchedB by viewModel.isPatchedB.collectAsState()
        DataPoint(
            label = stringResource(R.string.model),
            content = "${Build.MODEL} (${Build.DEVICE})",
        )
        DataPoint(
            label = stringResource(R.string.build_number),
            content = Build.ID,
        )
        DataPoint(
            label = stringResource(R.string.slot_suffix),
            content = viewModel.slotSuffix,
        )
        IsPatchedDataPoint(
            label = "vbmeta_a",
            isPatched = isPatchedA,
        )
        IsPatchedDataPoint(
            label = "vbmeta_b",
            isPatched = isPatchedB,
        )
    }
}

@Composable
fun DataPoint(
    label: String,
    content: String,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
    )
    Spacer(
        modifier = Modifier.height(4.dp),
    )
    Text(
        text = content,
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(
        modifier = Modifier.height(16.dp),
    )
}

@Composable
fun IsPatchedDataPoint(
    label: String,
    isPatched: Boolean,
) {
    val color = if (isPatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val text = stringResource(if (isPatched) R.string.patched else R.string.unpatched)
    val imageVector = if (isPatched) Icons.Outlined.CheckCircle else Icons.Rounded.Cancel
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = color,
        )
        Spacer(
            modifier = Modifier.size(ButtonDefaults.IconSpacing),
        )
        Icon(
            modifier = Modifier.size(
                size = 20.dp,
            ),
            imageVector = imageVector,
            tint = color,
            contentDescription = text,
        )
    }
    Spacer(
        modifier = Modifier.height(
            height = 12.dp,
        ),
    )
}

@ExperimentalMaterial3Api
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun ContentPreview() {
    VbmetaPatcherTheme {
        val viewModel: MainViewModelPreview = viewModel()
        MainContent(
            viewModel = viewModel,
        )
    }
}
