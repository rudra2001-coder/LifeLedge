package com.rudra.lifeledge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rudra.lifeledge.ui.navigation.MainApp
import com.rudra.lifeledge.ui.theme.LifeLedgeTheme
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeLedgeTheme {
                KoinContext {
                    MainApp()
                }
            }
        }
    }
}
