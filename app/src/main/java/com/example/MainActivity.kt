package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.ConsistencyHubApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ConsistencyHubViewModel
import com.example.viewmodel.ConsistencyHubViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request Push Notification Permissions on modern Android 13+ (API 33) devices
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissionRule = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permissionRule) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permissionRule), 101)
            }
        }

        // Initialize Room Database persistence modules
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database.appDao())

        // Modern dynamic constructor ViewModel injection
        val viewModel: ConsistencyHubViewModel by viewModels {
            ConsistencyHubViewModelFactory(application, repository)
        }
        
        // Handle Actionable Notifications
        if (intent.getStringExtra("NAVIGATE_TO_TAB") == "METRICS") {
            viewModel.selectedTab.value = com.example.viewmodel.DashboardTab.Metrics
        }

        setContent {
            val isLightMode by viewModel.isLightMode.collectAsState()
            MyApplicationTheme(darkTheme = !isLightMode) {
                ConsistencyHubApp(viewModel = viewModel)
            }
        }
    }
}
