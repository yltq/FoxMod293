package com.vpn.fox.base

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.vpn.fox.ui.theme.AppTheme
import com.vpn.fox.ui.theme.Main

abstract class BaseActivity : ComponentActivity() {
    var baseFoxVisible: Boolean = false
    var baseFoxShowAdPermission: Boolean = false //满足请求广告条件，如冷启动、热启动
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseFoxShowAdPermission = true
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()))
        initData()
        setContent {
            AppTheme {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().background(Main)) {
                        Content(innerPadding)
                    }
                }
            }
        }
    }

    open fun initData() {
    }

    @Composable
    abstract fun Content(padding: PaddingValues)

    override fun onResume() {
        super.onResume()
        baseFoxVisible = true
        when(baseFoxShowAdPermission) {
            false -> {}
            true -> {
                foxResumeShowA()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        baseFoxVisible = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        baseFoxShowAdPermission = true
        reInitData()
    }

    open fun reInitData() {}

    open fun foxResumeShowA() {}
}