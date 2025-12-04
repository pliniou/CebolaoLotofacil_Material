package com.cebolao.lotofacil.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun FormattedText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val annotatedString = remember(text) { htmlToAnnotatedString(text) }
    Text(
        text = annotatedString,
        style = style,
        color = color,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun TitleWithIcon(
          text: String,
          modifier: Modifier = Modifier,
          iconVector: ImageVector? = null,
          @DrawableRes iconRes: Int? = null,
          tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
              modifier = modifier, 
              verticalAlignment = Alignment.CenterVertically, 
              horizontalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
          ) {
              if (iconRes != null) {
                  Image(
                      painter = painterResource(id = iconRes),
                      contentDescription = null,
                      modifier = Modifier.size(Dimen.MediumIcon)
                  )
              } else if (iconVector != null) {
                  Icon(
                      imageVector = iconVector,
                      contentDescription = null,
                      tint = tint,
                      modifier = Modifier.size(Dimen.MediumIcon)
                  )
              }
              
              Text(
                  text = text, 
                  style = MaterialTheme.typography.headlineMedium,
                  color = MaterialTheme.colorScheme.onSurface
              )
          }
      }

@Composable
fun InfoPoint(title: String, description: String, modifier: Modifier = Modifier) {
          Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)) {
              Text(
                  text = title, 
                  style = MaterialTheme.typography.titleMedium, 
                  fontWeight = FontWeight.Bold, 
                  color = MaterialTheme.colorScheme.primary
              )
              Text(
                  text = description, 
                  style = MaterialTheme.typography.bodyMedium, 
                  color = MaterialTheme.colorScheme.onSurface
              )
          }
}

private fun htmlToAnnotatedString(html: String): AnnotatedString {
          val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
          return buildAnnotatedString {
              append(spanned.toString())
              spanned.getSpans(0, spanned.length, Any::class.java)?.forEach { span ->
                  val start = spanned.getSpanStart(span)
                  val end = spanned.getSpanEnd(span)
                  when (span) {
                      is android.text.style.StyleSpan -> when (span.style) {
                          android.graphics.Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                          android.graphics.Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                      }
                      is android.text.style.UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                  }
              }
          }
}