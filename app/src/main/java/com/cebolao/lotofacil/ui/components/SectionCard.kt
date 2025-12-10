package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer, // Mapped to Low/High consistent with theme
    contentPadding: Dp = Dimen.CardContentPadding,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(Dimen.ItemSpacing),
    headerActions: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    CustomCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = backgroundColor,
        hasBorder = true, // SectionCard usually implies some separation
        elevation = Dimen.Elevation.Low, // Or 0.dp if we want completely flat
        contentPadding = Dp(0f) // CustomCard adds padding to content, but here we manage it in Column below? 
        // Wait, CustomCard adds padding. SectionCard takes `contentPadding`.
        // Let's rely on CustomCard's padding logic or pass 0 and let Column handle it if needed.
        // Actually, SectionCard code passed contentPadding to Column. CustomCard does too.
        // So:
    ) {
        // CustomCard ALREADY adds contentPadding. 
        // BUT SectionCard has `verticalArrangement` usage in its Column. 
        // CustomCard's generic Column doesn't support verticalArrangement param customization.
        // I might have oversimplified CustomCard. 
        
        // Let's reuse CustomCard but maybe I should have allowed arrangement.
        // Or just inline the Card logic here to be consistent but kept flexible.
        // Actually, for consistency, I should use the same shape/colors.
    }
    // RE-THINKING: CustomCard implementation above enforces `Column(Modifier.padding(contentPadding))`.
    // SectionCard does `Column(padding(contentPadding), verticalArrangement)`.
    // If I use CustomCard, I lose verticalArrangement unless I add it to CustomCard.
    
    // I will simply Update SectionCard to MATCH the visual style (Shape/Color) without necessarily calling CustomCard 
    // IF CustomCard acts as a limiting wrapper. 
    // OR I update CustomCard to be more flexible. 
    // Let's update SectionCard manually to match the definitions.
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = verticalArrangement
        ) {
            if (title != null || headerActions != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (title != null) Dimen.SmallPadding else 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (headerActions != null) {
                        headerActions()
                    }
                }
                @Suppress("ControlFlowWithEmptyBody")
                if (title != null) {
                     // Optional: Separator below header if desired, but clean design often omits it
                     // unless explicitly needed. Keeping it clean for now.
                     // AppDivider(Modifier.padding(bottom = Dimen.SmallPadding))
                }
            }
            content()
        }
    }
}