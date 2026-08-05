package com.chezley.onepiecetcg.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chezley.onepiecetcg.data.model.Card as CardModel
import com.chezley.onepiecetcg.ui.theme.OnePieceTCGTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CardDetailViewModel = viewModel(
        factory = CardDetailViewModel.factory(cardId, LocalContext.current),
    ),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.card?.name ?: "Card") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            uiState.card == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Card not found")
            }
            else -> CardDetailContent(
                card = uiState.card!!,
                quantity = uiState.quantity,
                isOwned = uiState.isOwned,
                onToggleOwned = viewModel::toggleOwned,
                onIncrement = viewModel::incrementQuantity,
                onDecrement = viewModel::decrementQuantity,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun CardDetailContent(
    card: CardModel,
    quantity: Int,
    isOwned: Boolean,
    onToggleOwned: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        CardImagePlaceholder(cardNumber = card.cardNumber)

        Spacer(modifier = Modifier.height(16.dp))

        Text(card.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("${card.setCode} · ${card.cardNumber} · ${card.rarity}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        CardStatRow(label = "Cost", value = card.cost?.toString() ?: "-")
        CardStatRow(label = "Power", value = card.power?.toString() ?: "-")
        CardStatRow(label = "Attribute", value = card.attribute ?: "-")
        CardStatRow(label = "Type", value = card.type ?: "-")

        Spacer(modifier = Modifier.height(24.dp))

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Owned", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = isOwned, onCheckedChange = { onToggleOwned() })
                }

                if (isOwned) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDecrement) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity")
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.width(48.dp),
                            textAlign = TextAlign.Center,
                        )
                        IconButton(onClick = onIncrement) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase quantity")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardImagePlaceholder(cardNumber: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.7f),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(cardNumber, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun CardStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
private fun CardDetailContentPreview() {
    OnePieceTCGTheme {
        CardDetailContent(
            card = CardModel(
                id = "OP01-001",
                name = "Monkey D. Luffy",
                setCode = "OP01",
                cardNumber = "OP01-001",
                rarity = "L",
                cost = 0,
                power = 5000,
                attribute = "Strike",
                type = "Straw Hat Crew",
            ),
            quantity = 2,
            isOwned = true,
            onToggleOwned = {},
            onIncrement = {},
            onDecrement = {},
        )
    }
}
