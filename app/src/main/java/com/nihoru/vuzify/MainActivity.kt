package com.nihoru.vuzify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nihoru.vuzify.data.local.VuzifyDatabase
import com.nihoru.vuzify.data.remote.MtuciApiClient
import com.nihoru.vuzify.data.remote.MtuciSessionBridge
import com.nihoru.vuzify.data.repository.ScheduleRepository
import com.nihoru.vuzify.ui.screens.ScheduleScreen
import com.nihoru.vuzify.ui.theme.VuzifyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = VuzifyDatabase.getDatabase(this)
        val apiClient = MtuciApiClient()
        val sessionBridge = MtuciSessionBridge(this)
        val repository = ScheduleRepository(
            lessonDao = database.lessonDao(),
            apiClient = apiClient,
            sessionBridge = sessionBridge
        )

        setContent {
            VuzifyTheme {
                ScheduleScreen(repository = repository)
            }
        }
    }
}
