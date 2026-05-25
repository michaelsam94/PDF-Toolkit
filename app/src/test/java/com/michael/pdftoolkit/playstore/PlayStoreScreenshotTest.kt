package com.michael.pdftoolkit.playstore

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.michael.pdftoolkit.playstore.PlayStoreTestFixtures.seedMerge
import com.michael.pdftoolkit.playstore.PlayStoreTestFixtures.seedSign
import org.junit.After
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PHONE = "w360dp-h640dp-xxhdpi"
private const val TABLET = "w800dp-h1280dp-xhdpi"

@RunWith(RobolectricTestRunner::class)
@Category(PlayStoreScreenshotTests::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PlayStoreScreenshotTest {
  private val app: Application
    get() = ApplicationProvider.getApplicationContext()

  @After
  fun tearDown() {
    teardownPlayStoreDatabase()
  }

  @Test
  @Config(qualifiers = PHONE)
  fun phone_01_dashboard() {
    val viewModels = preparePlayStoreViewModels(app)
    capturePlayStoreImage("phone/01_dashboard.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Dashboard, viewModels)
    }
  }

  @Test
  @Config(qualifiers = PHONE)
  fun phone_02_search() {
    val viewModels = preparePlayStoreViewModels(app)
    capturePlayStoreImage("phone/02_search.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Search, viewModels)
    }
  }

  @Test
  @Config(qualifiers = PHONE)
  fun phone_03_merge() {
    val viewModels =
      preparePlayStoreViewModels(app) {
        seedMerge(merge)
      }
    capturePlayStoreImage("phone/03_merge.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Merge, viewModels)
    }
  }

  @Test
  @Config(qualifiers = PHONE)
  fun phone_04_sign() {
    val viewModels =
      preparePlayStoreViewModels(app) {
        seedSign(sign)
      }
    capturePlayStoreImage("phone/04_sign.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Sign, viewModels)
    }
  }

  @Test
  @Config(qualifiers = TABLET)
  fun tablet_01_dashboard() {
    val viewModels = preparePlayStoreViewModels(app)
    capturePlayStoreImage("tablet/01_dashboard.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Dashboard, viewModels)
    }
  }

  @Test
  @Config(qualifiers = TABLET)
  fun tablet_02_search() {
    val viewModels = preparePlayStoreViewModels(app)
    capturePlayStoreImage("tablet/02_search.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Search, viewModels)
    }
  }

  @Test
  @Config(qualifiers = TABLET)
  fun tablet_03_merge() {
    val viewModels =
      preparePlayStoreViewModels(app) {
        seedMerge(merge)
      }
    capturePlayStoreImage("tablet/03_merge.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Merge, viewModels)
    }
  }

  @Test
  @Config(qualifiers = TABLET)
  fun tablet_04_sign() {
    val viewModels =
      preparePlayStoreViewModels(app) {
        seedSign(sign)
      }
    capturePlayStoreImage("tablet/04_sign.png") {
      PlayStoreScreenshotFrame(PlayStoreScene.Sign, viewModels)
    }
  }
}
