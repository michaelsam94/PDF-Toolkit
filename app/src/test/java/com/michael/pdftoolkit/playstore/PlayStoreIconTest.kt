package com.michael.pdftoolkit.playstore

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import com.michael.pdftoolkit.R
import com.michael.pdftoolkit.ui.theme.MyApplicationTheme
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Category(PlayStoreScreenshotTests::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PlayStoreIconTest {
  @Test
  @Config(qualifiers = "w512dp-h512dp-mdpi")
  fun app_icon_512() {
    val ctx = ApplicationProvider.getApplicationContext<Context>()
    capturePlayStoreImage("app-icon-512.png") {
      MyApplicationTheme(dynamicColor = false) {
        val iconBitmap =
          remember {
            checkNotNull(ctx.getDrawable(R.mipmap.ic_launcher))
              .toBitmap(512, 512)
              .asImageBitmap()
          }
        Image(
          bitmap = iconBitmap,
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}
