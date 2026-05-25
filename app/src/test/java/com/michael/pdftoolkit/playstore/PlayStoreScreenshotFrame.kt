package com.michael.pdftoolkit.playstore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.michael.pdftoolkit.presentation.ui.ConvertScreenTab
import com.michael.pdftoolkit.presentation.ui.HomeScreen
import com.michael.pdftoolkit.presentation.ui.MergeScreenTab
import com.michael.pdftoolkit.presentation.ui.SignScreenTab
import com.michael.pdftoolkit.presentation.ui.SplitScreenTab
import com.michael.pdftoolkit.ui.theme.MyApplicationTheme

enum class PlayStoreScene {
  Dashboard,
  Search,
  Merge,
  Sign,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayStoreScreenshotFrame(
  scene: PlayStoreScene,
  viewModels: PlayStoreViewModels,
) {
  MyApplicationTheme(dynamicColor = false) {
    val currentTab =
      when (scene) {
        PlayStoreScene.Dashboard, PlayStoreScene.Search -> "home"
        PlayStoreScene.Merge -> "merge_split"
        PlayStoreScene.Sign -> "sign"
      }
    val mergeSplitSubTab =
      when (scene) {
        PlayStoreScene.Merge -> 0
        else -> 0
      }
    val topBarTitle =
      when (scene) {
        PlayStoreScene.Dashboard, PlayStoreScene.Search -> "PDF Toolkit"
        PlayStoreScene.Merge -> "Merge & Split"
        PlayStoreScene.Sign -> "Sign PDF"
      }
    val initialSearchQuery =
      when (scene) {
        PlayStoreScene.Search -> "Contract"
        else -> ""
      }

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = topBarTitle,
              style = MaterialTheme.typography.titleLarge,
            )
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface,
              titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
      },
      bottomBar = {
        NavigationBar {
          NavigationBarItem(
            selected = currentTab == "home",
            onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
          )
          NavigationBarItem(
            selected = currentTab == "merge_split",
            onClick = {},
            icon = { Icon(Icons.Default.Merge, contentDescription = "Merge and split") },
            label = { Text("Merge/Split") },
          )
          NavigationBarItem(
            selected = currentTab == "sign",
            onClick = {},
            icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Sign PDF") },
            label = { Text("Sign") },
          )
          NavigationBarItem(
            selected = currentTab == "convert",
            onClick = {},
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Convert to PDF") },
            label = { Text("Convert") },
          )
        }
      },
    ) { innerPadding ->
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(innerPadding),
      ) {
        when (currentTab) {
          "home" -> {
            HomeScreen(
              viewModel = viewModels.home,
              onNavigateToTab = {},
              initialSearchQuery = initialSearchQuery,
            )
          }
          "merge_split" -> {
            Column(modifier = Modifier.fillMaxSize()) {
              TabRow(selectedTabIndex = mergeSplitSubTab) {
                Tab(
                  selected = mergeSplitSubTab == 0,
                  onClick = {},
                  text = { Text("Merge Files", fontWeight = FontWeight.Bold) },
                  icon = {
                    Icon(
                      Icons.Default.Merge,
                      contentDescription = "Merge Tools",
                      modifier = Modifier.size(18.dp),
                    )
                  },
                )
                Tab(
                  selected = mergeSplitSubTab == 1,
                  onClick = {},
                  text = { Text("Split Slices", fontWeight = FontWeight.Bold) },
                  icon = {
                    Icon(
                      Icons.Default.CallSplit,
                      contentDescription = "Split Slicer",
                      modifier = Modifier.size(18.dp),
                    )
                  },
                )
              }
              Box(modifier = Modifier.weight(1f)) {
                if (mergeSplitSubTab == 0) {
                  MergeScreenTab(viewModel = viewModels.merge)
                } else {
                  SplitScreenTab(viewModel = viewModels.split)
                }
              }
            }
          }
          "sign" -> {
            SignScreenTab(viewModel = viewModels.sign)
          }
          else -> {
            ConvertScreenTab(viewModel = viewModels.convert)
          }
        }
      }
    }
  }
}
