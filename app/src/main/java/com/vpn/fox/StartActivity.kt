package com.vpn.fox

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.vpn.fox.base.BaseActivity
import com.vpn.fox.main.MainActivity
import com.vpn.fox.ui.CenterColumn
import com.vpn.fox.ui.Glide
import com.vpn.fox.ui.TextWhite
import com.vpn.fox.utils.DataManager
import com.vpn.fox.utils.FoxAdManager.ONP_KEY
import com.vpn.fox.utils.FoxAdManager.foxAdEnable
import com.vpn.fox.utils.FoxAdManager.foxLoad
import com.vpn.fox.utils.FoxAdManager.foxShow
import com.vpn.fox.utils.FoxAdManager.showConsentInfoUpdateDialog
import com.vpn.fox.utils.FoxFbManager.foxFbDataLoadedFinished
import com.vpn.fox.utils.NetManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StartActivity : BaseActivity() { // TODO request permission

    companion object {
        var hot: Boolean = false
    }
    @Composable
    override fun Content(padding: PaddingValues) {
        var target by remember { mutableFloatStateOf(0F) }
        val rotation by animateFloatAsState(
            target,
            infiniteRepeatable(
                animation = tween(1400, 0, easing = LinearEasing)
            ),
            label = "start rotation"
        )
        CenterColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 200.dp)
        ) {
            Glide(R.drawable.start_icon, Modifier.size(104.dp, 126.dp))

            TextWhite(getString(R.string.app_name), 34, Modifier.padding(top = 16.dp, bottom = 120.dp), 700)

            Glide(
                R.drawable.ic_loading,
                Modifier
                    .width(50.dp)
                    .aspectRatio(36F/37F)
                    .rotate(rotation)
            )
        }
        LaunchedEffect(Unit) {
            target = 360F
        }
    }

    private suspend fun showAd() {
        while (!baseFoxVisible) delay(200)
        if (DataManager.completeGuide) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, GuideActivity::class.java))
        }
        finish()
    }

    override fun initData() {
        super.initData()
        NetManager.getTogging(System.currentTimeMillis())
        NetManager.region()
    }

    override fun reInitData() {
        super.reInitData()
        NetManager.getTogging(System.currentTimeMillis())
        NetManager.region()
    }

    override fun foxResumeShowA() {
        super.foxResumeShowA()
        this.showConsentInfoUpdateDialog(start = {
            when(it) {
                true -> {
                    lifecycleScope.launch {
                        if (hot) {
                            hot = false
                            baseFoxShowAdPermission = false
                            var lastFinish = 0
                            for (i in 0 until 40) {
                                delay(100)
                                lastFinish = i
                                if (foxFbDataLoadedFinished) break
                            }
                            ONP_KEY.foxLoad()
                            for (i in lastFinish until 100) {
                                delay(100)
                                if (ONP_KEY.foxAdEnable()) break
                            }
                            if (ONP_KEY.foxAdEnable()) {
                                ONP_KEY.foxShow(this@StartActivity, dismiss = {
                                    lifecycleScope.launch {
                                        delay(300)
                                        showAd()
                                    }
                                })
                            } else {
                                showAd()
                            }
                        } else {
                            for (i in 0 until 100) {
                                delay(100)
                                if (foxFbDataLoadedFinished && i >= 20) break
                            }
                            showAd()
                        }
                    }
                }
                false -> {}
            }
        })
    }
}