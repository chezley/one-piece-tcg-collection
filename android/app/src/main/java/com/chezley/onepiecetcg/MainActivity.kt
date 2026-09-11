package com.chezley.onepiecetcg

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.chezley.onepiecetcg.data.catalog.CatalogLoader
import com.chezley.onepiecetcg.data.db.AppDatabase
import com.chezley.onepiecetcg.navigation.RootScaffold
import com.chezley.onepiecetcg.ui.theme.OnePieceTCGTheme
import kotlinx.coroutines.launch

private const val LOG_TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        lifecycleScope.launch {
            val insertedCount = CatalogLoader.seedCatalog(applicationContext, database)
            Log.i(LOG_TAG, "Catalog seed complete: $insertedCount new card(s) inserted")
        }

        setContent {
            OnePieceTCGTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootScaffold()
                }
            }
        }
    }
}
