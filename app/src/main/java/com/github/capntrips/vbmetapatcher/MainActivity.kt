package com.github.capntrips.vbmetapatcher

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.animation.doOnEnd
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

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val scale = ObjectAnimator.ofPropertyValuesHolder(
                splashScreenView,
                PropertyValuesHolder.ofFloat(
                    View.SCALE_X,
                    1f,
                    0f
                ),
                PropertyValuesHolder.ofFloat(
                    View.SCALE_Y,
                    1f,
                    0f
                )
            )
            scale.interpolator = AccelerateInterpolator()
            scale.duration = 250L
            scale.doOnEnd { splashScreenView.remove() }
            scale.start()
        }

        val viewModel: MainViewModel by viewModels { MainViewModelFactory(this) }
        setContent {
            VbmetaPatcherTheme {
                val systemUiController = rememberSystemUiController()
                val darkIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = darkIcons
                    )
                }
                ProvideWindowInsets {
                    Shell.getShell()
                    if (Shell.rootAccess()) {
                        Shell.su("cd $filesDir").exec()
                        MainScreen(viewModel)
                    } else {
                        Scaffold {
                            ErrorScreen()
                        }
                    }
                }
            }
        }
    }
}
