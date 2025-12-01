package com.cebolao.lotofacil.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.cebolao.lotofacil.ui.theme.Dimen

sealed interface IconSource {
    data class Vector(val imageVector: ImageVector) : IconSource
    data class Resource(@DrawableRes val resId: Int) : IconSource
}

@Composable
fun InfoListCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
) {
    InfoListCardImpl(title, subtitle, IconSource.Vector(icon), modifier, trailingIcon)
}

@Composable
fun InfoListCard(
    title: String,
    subtitle: String,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
) {
    InfoListCardImpl(title, subtitle, IconSource.Resource(iconRes), modifier, trailingIcon)
}

@Composable
private fun InfoListCardImpl(
    title: String,
    subtitle: String,
    iconSource: IconSource,
    modifier: Modifier,
    trailingIcon: ImageVector
) {
    SectionCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
        ) {
            when (iconSource) {
                is IconSource.Vector -> {
                    Icon(
                        imageVector = iconSource.imageVector,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimen.MediumIcon)
                    )
                }
                is IconSource.Resource -> {
                    Image(
                        painter = painterResource(id = iconSource.resId),
                        contentDescription = null,
                        modifier = Modifier.size(Dimen.MediumIcon)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}