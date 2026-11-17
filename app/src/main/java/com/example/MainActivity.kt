package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.OrientMainScreen
import com.example.ui.theme.OrientPlayerTheme
import com.example.ui.viewmodel.OrientViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val orientViewModel: OrientViewModel = viewModel()
            val themeStyle by orientViewModel.themeStyle.collectAsStateWithLifecycle()

            OrientPlayerTheme(themeStyle = themeStyle) {
                OrientMainScreen(viewModel = orientViewModel)
            }
        }
    }
}
