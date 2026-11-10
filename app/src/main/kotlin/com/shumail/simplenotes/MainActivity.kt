package com.shumail.simplenotes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.shumail.simplenotes.model.NoteFormat
import com.shumail.simplenotes.ui.screens.NotesWorkspaceScreen
import com.shumail.simplenotes.ui.theme.SimpleNotesTheme
import com.shumail.simplenotes.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            val saveAsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("*/*")
            ) { uri ->
                if (uri != null) {
                    viewModel.saveNoteToCustomUri(uri)
                }
            }

            val importFileLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    viewModel.importFileFromUri(uri)
                }
            }

            SimpleNotesTheme(
                themeMode = uiState.preferences.themeMode,
                accentPalette = uiState.preferences.accentPalette
            ) {
                NotesWorkspaceScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onRequestSaveAsSystemPicker = { suggestedName ->
                        saveAsLauncher.launch(suggestedName)
                    },
                    onRequestImportFilePicker = {
                        importFileLauncher.launch(arrayOf("text/*", "application/octet-stream", "*/*"))
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                if (!sharedText.isNullOrBlank()) {
                    val title = sharedSubject?.takeIf { it.isNotBlank() } ?: "Shared Note"
                    viewModel.createNewNote(
                        titleOrFileName = title,
                        folderName = "Notes",
                        format = NoteFormat.MARKDOWN,
                        initialTemplate = "# $title\n\n$sharedText"
                    )
                }
            }
            Intent.ACTION_VIEW, Intent.ACTION_EDIT -> {
                val dataUri = intent.data
                if (dataUri != null) {
                    viewModel.importFileFromUri(dataUri)
                }
            }
        }
    }
}
