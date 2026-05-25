package com.michael.pdftoolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.michael.pdftoolkit.presentation.ui.ConvertScreenTab
import com.michael.pdftoolkit.presentation.ui.HomeScreen
import com.michael.pdftoolkit.presentation.ui.MergeScreenTab
import com.michael.pdftoolkit.presentation.ui.SignScreenTab
import com.michael.pdftoolkit.presentation.ui.SplitScreenTab
import com.michael.pdftoolkit.presentation.viewmodel.ConvertViewModel
import com.michael.pdftoolkit.presentation.viewmodel.HomeViewModel
import com.michael.pdftoolkit.presentation.viewmodel.MergeViewModel
import com.michael.pdftoolkit.presentation.viewmodel.PdfViewModelFactory
import com.michael.pdftoolkit.presentation.viewmodel.SignViewModel
import com.michael.pdftoolkit.presentation.viewmodel.SplitViewModel
import com.michael.pdftoolkit.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as PdfToolkitApp
        val factory = PdfViewModelFactory(app)

        setContent {
            MyApplicationTheme {
                MainAppLayout(factory)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    factory: PdfViewModelFactory
) {
    var currentTab by remember { mutableStateOf("home") }
    var mergeSplitSubTab by remember { mutableIntStateOf(0) } // 0 = Merge, 1 = Split

    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val mergeViewModel: MergeViewModel = viewModel(factory = factory)
    val splitViewModel: SplitViewModel = viewModel(factory = factory)
    val signViewModel: SignViewModel = viewModel(factory = factory)
    val convertViewModel: ConvertViewModel = viewModel(factory = factory)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            "home" -> "PDF Toolkit"
                            "merge_split" -> "Merge & Split"
                            "sign" -> "Sign PDF"
                            "convert" -> "Convert to PDF"
                            else -> "PDF Toolkit"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { currentTab = "home" },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = currentTab == "merge_split",
                    onClick = { currentTab = "merge_split" },
                    icon = { Icon(imageVector = Icons.Default.Merge, contentDescription = "Merge and split") },
                    label = { Text("Merge/Split") }
                )

                NavigationBarItem(
                    selected = currentTab == "sign",
                    onClick = { currentTab = "sign" },
                    icon = { Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Sign PDF") },
                    label = { Text("Sign") }
                )

                NavigationBarItem(
                    selected = currentTab == "convert",
                    onClick = { currentTab = "convert" },
                    icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Convert to PDF") },
                    label = { Text("Convert") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "home" -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToTab = { target ->
                            currentTab = target
                        }
                    )
                }
                "merge_split" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Sliding sub-tabs for segment selectors
                        TabRow(selectedTabIndex = mergeSplitSubTab) {
                            Tab(
                                selected = mergeSplitSubTab == 0,
                                onClick = { mergeSplitSubTab = 0 },
                                text = { Text("Merge Files", fontWeight = FontWeight.Bold) },
                                icon = { Icon(imageVector = Icons.Default.Merge, contentDescription = "Merge Tools", modifier = Modifier.size(18.dp)) }
                            )
                            Tab(
                                selected = mergeSplitSubTab == 1,
                                onClick = { mergeSplitSubTab = 1 },
                                text = { Text("Split Slices", fontWeight = FontWeight.Bold) },
                                icon = { Icon(imageVector = Icons.Default.CallSplit, contentDescription = "Split Slicer", modifier = Modifier.size(18.dp)) }
                            )
                        }
                        
                        Box(modifier = Modifier.weight(1f)) {
                            if (mergeSplitSubTab == 0) {
                                MergeScreenTab(viewModel = mergeViewModel)
                            } else {
                                SplitScreenTab(viewModel = splitViewModel)
                            }
                        }
                    }
                }
                "sign" -> {
                    SignScreenTab(viewModel = signViewModel)
                }
                "convert" -> {
                    ConvertScreenTab(viewModel = convertViewModel)
                }
            }
        }
    }
}
