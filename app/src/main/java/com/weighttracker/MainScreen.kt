package com.weighttracker

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import com.weighttracker.ui.screens.HistoryScreen
import com.weighttracker.ui.screens.HomeScreen
import com.weighttracker.ui.screens.SettingsScreen
import com.weighttracker.ui.screens.WeightViewModel
import com.weighttracker.data.local.UserSettings
import com.weighttracker.data.local.SettingsDataStore
import java.io.File

sealed class Screen {
    object Home : Screen()
    object Settings : Screen()
    object History : Screen()
}

@Composable
fun MainScreen(
    viewModel: WeightViewModel,
    settings: UserSettings,
    settingsDataStore: SettingsDataStore
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSettings = { currentScreen = Screen.Settings },
                onNavigateToHistory = { currentScreen = Screen.History }
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                viewModel = viewModel,
                settings = settings,
                settingsDataStore = settingsDataStore,
                onNavigateBack = { currentScreen = Screen.Home }
            )
        }
        is Screen.History -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            
            HistoryScreen(
                viewModel = viewModel,
                height = settings.height,
                onNavigateBack = { currentScreen = Screen.Home },
                onExportCsv = { csv ->
                    try {
                        val file = File(context.cacheDir, "weight_export.csv")
                        file.writeText(csv)
                        
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        
                        context.startActivity(Intent.createChooser(shareIntent, "导出体重记录"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                onImportCsv = { csv ->
                    viewModel.importFromCsv(csv)
                    Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
