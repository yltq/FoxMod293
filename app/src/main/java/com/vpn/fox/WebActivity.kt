package com.vpn.fox

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vpn.fox.base.BaseActivity
import com.vpn.fox.ui.Glide
import com.vpn.fox.ui.TextWhite

class WebActivity : BaseActivity() {

    @Composable
    override fun Content(padding: PaddingValues) {
        Column(Modifier.padding(padding)) {
            var title by remember { mutableStateOf("") }
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp)
            ) {
                Glide(R.drawable.ic_back,
                    Modifier
                        .size(20.dp)
                        .clickable { finish() })
                TextWhite(
                    title, 18,
                    Modifier
                        .weight(1F)
                        .padding(start = 10.dp, end = 30.dp),
                    textAlign = TextAlign.Center,
                )
            }
            Web { title = it }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun ColumnScope.Web(block: (String) -> Unit) {
        AndroidView({ context ->
            WebView(context).apply {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                settings.javaScriptEnabled = true
                webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        title?.let { block.invoke(it) }
                    }
                }
                webViewClient = object : WebViewClient() {}
                loadUrl(intent.getStringExtra("url") ?: "")
            }
        }, Modifier.weight(1F))
    }
}