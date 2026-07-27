package com.chezley.onepiecetcg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.chezley.onepiecetcg.navigation.RootScaffold
import com.chezley.onepiecetcg.ui.theme.OnePieceTCGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OnePieceTCGTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootScaffold()
                }
            }
        }
    }
}
