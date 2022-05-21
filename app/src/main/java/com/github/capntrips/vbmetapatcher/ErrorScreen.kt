package com.github.capntrips.vbmetapatcher

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.capntrips.vbmetapatcher.ui.theme.Orange500
import com.github.capntrips.vbmetapatcher.ui.theme.VbmetaPatcherTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues

@ExperimentalMaterial3Api
@Composable
fun ErrorScreen(e: Exception) {
    val hscroll = rememberScrollState(0)
    Scaffold {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            SelectionContainer() {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(rememberInsetsPaddingValues(
                            LocalWindowInsets.current.statusBars
                        ))
                ) {
                    Icon(
                        modifier = Modifier
                            .width(68.dp)
                            .height(68.dp)
                            .padding(16.dp),
                        imageVector = Icons.Filled.Warning,
                        tint = Orange500,
                        contentDescription = e.message
                    )
                    // Spacer(Modifier.height(8.dp))
                    Text(
                        text = e.stackTraceToString().replace("com.github.capntrips.vbmetapatcher.", ""),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(hscroll)
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ErrorScreen(message: String) {
    Scaffold {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    modifier = Modifier
                        .width(36.dp)
                        .height(36.dp),
                    imageVector = Icons.Filled.Warning,
                    tint = Orange500,
                    contentDescription = message
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ErrorScreenPreviewDark() {
    ErrorScreenPreviewLight()
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun ErrorScreenPreviewLight() {
    VbmetaPatcherTheme {
        ErrorScreen(stringResource(R.string.root_required))
    }
}
