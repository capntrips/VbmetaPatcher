package com.github.capntrips.vbmetapatcher

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.capntrips.vbmetapatcher.ui.theme.Orange500
import com.github.capntrips.vbmetapatcher.ui.theme.VbmetaPatcherTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@ExperimentalMaterial3Api
@Composable
fun MainScreen(
    viewModel: MainViewModel,
) {
    val isPatchedA by viewModel.isPatchedA.collectAsState()
    val isPatchedB by viewModel.isPatchedB.collectAsState()
    val openDialog = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxSize(),
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                contentPadding = rememberInsetsPaddingValues(
                    LocalWindowInsets.current.statusBars,
                    applyBottom = false,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isPatchedA && isPatchedB) {
                        openDialog.value = true
                    } else {
                        viewModel.togglePatched()
                    }
                },
            ) {
                Row(
                    modifier = Modifier.padding(ButtonDefaults.ContentPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val text = stringResource(if (isPatchedA && isPatchedB) R.string.restore_button else R.string.patch_button)
                    Icon(
                        imageVector = if (isPatchedA && isPatchedB) Icons.Outlined.Restore else Icons.Outlined.Healing,
                        contentDescription = text,
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = text,
                    )
                }
            }
            if (openDialog.value) {
                AlertDialog(
                    onDismissRequest = { openDialog.value = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            tint = Orange500,
                            contentDescription = stringResource(R.string.warning),
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.warning),
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.do_not_reboot),
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openDialog.value = false
                                viewModel.togglePatched()
                            },
                        ) {
                            Text(
                                text = stringResource(R.string.proceed),
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                openDialog.value = false
                            },
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                            )
                        }
                    },
                )
            }
        },
    ) {
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        SwipeRefresh(
            modifier = Modifier.fillMaxSize(),
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.refresh() },
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp, 0.dp, 16.dp, 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                MainContent(
                    viewModel = viewModel,
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun MainScreenPreview() {
    VbmetaPatcherTheme {
        val viewModel: MainViewModel = viewModel()
        MainScreen(
            viewModel = viewModel,
        )
    }
}
