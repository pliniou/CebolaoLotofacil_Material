package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun LoadingDialog(
    title: String,
    message: String,
    onDismissRequest: () -> Unit,
    isCancelable: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = isCancelable,
            dismissOnClickOutside = isCancelable,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.LargePadding),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(Dimen.Elevation.High),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(Dimen.LargePadding),
                verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp), // Loader maior
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = message, 
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(Dimen.ExtraSmallPadding))
            }
        }
    }
}