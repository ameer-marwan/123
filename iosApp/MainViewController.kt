package com.example

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.example.ui.theme.FieldShiftTheme
import com.example.viewmodel.FieldShiftViewModel

/**
 * Compose Multiplatform Entry Point for iOS.
 *
 * This function is compiled to an Objective-C / Swift framework via Kotlin/Native
 * and consumed inside iOSApp / ContentView.swift using UIViewControllerRepresentable.
 */
fun MainViewController() = ComposeUIViewController {
    val viewModel = remember { FieldShiftViewModel() }
    FieldShiftTheme(themeMode = com.example.ui.theme.AppThemeMode.AGRITECH_EMERALD) {
        FieldShiftApp(viewModel = viewModel)
    }
}
