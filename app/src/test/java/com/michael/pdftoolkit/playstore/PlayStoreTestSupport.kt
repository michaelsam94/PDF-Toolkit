package com.michael.pdftoolkit.playstore

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.michael.pdftoolkit.PdfToolkitApp
import com.michael.pdftoolkit.presentation.viewmodel.ConvertViewModel
import com.michael.pdftoolkit.presentation.viewmodel.HomeViewModel
import com.michael.pdftoolkit.presentation.viewmodel.MergeViewModel
import com.michael.pdftoolkit.presentation.viewmodel.PdfViewModelFactory
import com.michael.pdftoolkit.presentation.viewmodel.SignViewModel
import com.michael.pdftoolkit.presentation.viewmodel.SplitViewModel
import kotlinx.coroutines.runBlocking

data class PlayStoreViewModels(
  val home: HomeViewModel,
  val merge: MergeViewModel,
  val split: SplitViewModel,
  val sign: SignViewModel,
  val convert: ConvertViewModel,
)

fun createPlayStoreViewModels(application: Application = ApplicationProvider.getApplicationContext()): PlayStoreViewModels {
  val app = application as PdfToolkitApp
  val factory = PdfViewModelFactory(app)
  return PlayStoreViewModels(
    home = factory.create(HomeViewModel::class.java),
    merge = factory.create(MergeViewModel::class.java),
    split = factory.create(SplitViewModel::class.java),
    sign = factory.create(SignViewModel::class.java),
    convert = factory.create(ConvertViewModel::class.java),
  )
}

fun preparePlayStoreViewModels(
  application: Application = ApplicationProvider.getApplicationContext(),
  configure: PlayStoreViewModels.() -> Unit = {},
): PlayStoreViewModels {
  val app = application as PdfToolkitApp
  runBlocking { PlayStoreTestFixtures.seedAll(app) }
  val viewModels = createPlayStoreViewModels(application)
  configure(viewModels)
  return viewModels
}

fun teardownPlayStoreDatabase() {
  val app = ApplicationProvider.getApplicationContext<PdfToolkitApp>()
  runBlocking {
    app.appDatabase.recentDocumentDao().deleteAll()
    app.appDatabase.savedSignatureDao().deleteAll()
  }
}
