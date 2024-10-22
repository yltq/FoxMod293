package com.vpn.fox

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vpn.fox.base.BaseActivity
import com.vpn.fox.ui.CenterColumn
import com.vpn.fox.ui.Glide
import com.vpn.fox.ui.TextWhite


class SettingsActivity : BaseActivity() {
    @Composable
    override fun Content(padding: PaddingValues) {
        CenterColumn(Modifier.padding(padding)) {
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
                    "Settings", 18,
                    Modifier
                        .weight(1F)
                        .padding(start = 10.dp, end = 30.dp),
                    textAlign = TextAlign.Center,
                )
            }
            Glide(
                R.drawable.start_icon,
                Modifier
                    .padding(top = 20.dp)
                    .size(71.dp, 86.dp)
            )
            TextWhite(stringResource(R.string.app_name), 16)

            TextWhite(
                "Post a comment", 14,
                Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF353C4D))
                    .padding(vertical = 24.dp)
                    .clickable { sendEmail() },
                textAlign = TextAlign.Center
            )
            TextWhite(
                "Share with Friends", 14,
                Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF353C4D))
                    .padding(vertical = 24.dp)
                    .clickable { shareApp() },
                textAlign = TextAlign.Center
            )
            TextWhite(
                "Privacy Policy", 14,
                Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF353C4D))
                    .padding(vertical = 24.dp)
                    .clickable { toWeb("https://www.baidu.com") }, // TODO privacy policy url

                textAlign = TextAlign.Center
            )
//            TextWhite(
//                "Terms of Service", 14,
//                Modifier
//                    .padding(start = 20.dp, end = 20.dp, top = 12.dp)
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(Color(0xFF353C4D))
//                    .padding(vertical = 24.dp)
//                    .clickable { toWeb("https://www.google.com") }, // TODO user terms url
//                textAlign = TextAlign.Center
//            )
        }
    }

    private fun sendEmail() {
        runCatching {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.setData(Uri.parse("mailto:example@example.com")) // TODO email
            startActivity(emailIntent)
        }.onFailure {
            Toast.makeText(this, "Send failed!", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareApp() {
        runCatching {
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.setType("text/plain")
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=$packageName")
            startActivity(Intent.createChooser(sendIntent, "Share ${getString(R.string.app_name)}"))
        }.onFailure {
            Toast.makeText(this, "Share failed!", Toast.LENGTH_LONG).show()
        }
    }

    private fun toWeb(url: String) {
        startActivity(Intent(this, WebActivity::class.java).putExtra("url", url))
    }
}