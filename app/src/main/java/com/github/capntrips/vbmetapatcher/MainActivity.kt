package com.github.capntrips.vbmetapatcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import com.github.capntrips.vbmetapatcher.ui.theme.VbmetaPatcherTheme
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.topjohnwu.superuser.Shell

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            VbmetaPatcherTheme {
                val systemUiController = rememberSystemUiController()
                val darkIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = darkIcons,
                    )
                }
                ProvideWindowInsets {
                    Shell.getShell()
                    if (Shell.rootAccess()) {
                        val viewModel = MainViewModel()
                        MainScreen(
                            viewModel = viewModel,
                        )
                    } else {
                        ErrorScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = stringResource(R.string.root_required),
            color = MaterialTheme.colorScheme.error,
        )
    }
}
