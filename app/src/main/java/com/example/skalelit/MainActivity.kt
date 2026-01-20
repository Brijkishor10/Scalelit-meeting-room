package com.example.skalelit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skalelit.ui.screens.AppRoot
import com.example.skalelit.ui.theme.NeoScheme
import com.example.skalelit.ui.viewmodel.MainViewModel
import com.example.skalelit.ui.viewmodel.VMFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                val factory = VMFactory(application)
                val vm: MainViewModel = viewModel(factory = factory)
                AppRoot(vm)
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NeoScheme) {
        Surface {
            content()
        }
    }
}