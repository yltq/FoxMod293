package com.vpn.fox.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun CenterColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, content = content)
}

@Composable
fun CenterRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, content = content, horizontalArrangement = horizontalArrangement)
}

@Composable
fun TextWhite(
    text: String,
    fontSize: Int,
    modifier: Modifier = Modifier,
    fontWeight: Int = 600,
    fontStyle: FontStyle? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    Text(text, modifier, color = Color.White, fontSize.sp, fontStyle, FontWeight(fontWeight), fontFamily, textAlign = textAlign, overflow = overflow)
}

@Composable
fun Text(
    text: String,
    fontSize: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: Int = 600,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    Text(text, modifier, color, fontSize.sp, fontStyle, FontWeight(fontWeight), fontFamily, textAlign = textAlign, overflow = overflow)
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Glide(
    model: Any?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.FillBounds,
    colorFilter: ColorFilter? = null,
) {
    GlideImage(model, "", modifier, alignment, contentScale, colorFilter = colorFilter)
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideCrop(
    model: Any?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop,
) {
    GlideImage(model, "", modifier, alignment, contentScale)
}