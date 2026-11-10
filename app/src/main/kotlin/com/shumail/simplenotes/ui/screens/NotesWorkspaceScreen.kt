package com.shumail.simplenotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shumail.simplenotes.model.AppSection
import com.shumail.simplenotes.model.NoteDocument
import com.shumail.simplenotes.model.NoteFormat
import com.shumail.simplenotes.ui.components.SimpleNotesLogo
import com.shumail.simplenotes.ui.theme.InstrumentSerifFamily
import com.shumail.simplenotes.viewmodel.NotesUiState
import com.shumail.simplenotes.viewmodel.NotesViewModel

@Composable
fun NotesWorkspaceScreen(
    uiState: NotesUiState,
    viewModel: NotesViewModel,
    onRequestSaveAsSystemPicker: (String) -> Unit,
    onRequestImportFilePicker: () -> Unit
) {
    // Handle system Back button on phones when editor is open or on secondary sections
    BackHandler(enabled = uiState.isMobileEditorOpen || uiState.activeSection != AppSection.NOTES) {
        if (uiState.isMobileEditorOpen) {
            viewModel.closeMobileEditor()
        } else {
            viewModel.setSection(AppSection.NOTES)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // 16:10 Tablet & Expanded Foldable detection (width >= 700.dp)
        val isExpandedTablet = maxWidth >= 700.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Toast / Save Banner Notification
            AnimatedVisibility(
                visible = uiState.lastSaveResult != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.lastSaveResult?.let { saveRes ->
                    Surface(
                        color = if (saveRes.success) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Save,
                                    contentDescription = null,
                                    tint = if (saveRes.success) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = saveRes.message,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (saveRes.success) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onErrorContainer,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearLastSaveResult() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Dismiss notification",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isExpandedTablet) {
                // =========================================================================
                // 16:10 TABLET CANONICAL MULTI-PANE LAYOUT (NavigationRail + List + Detail)
                // =========================================================================
                Row(modifier = Modifier.fillMaxSize()) {
                    TabletNavigationRail(
                        activeSection = uiState.activeSection,
                        onSelectSection = { viewModel.setSection(it) },
                        onNewNoteClick = { viewModel.setNewNoteDialogOpen(true) },
                        onQuickSaveClick = { viewModel.saveNoteLocallyToDevice() }
                    )
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    when (uiState.activeSection) {
                        AppSection.NOTES -> {
                            // Left/Middle Note Explorer Pane (~330.dp)
                            NotesListExplorerPane(
                                uiState = uiState,
                                isExpandedTablet = true,
                                onSearchQueryChange = viewModel::setSearchQuery,
                                onSelectFolder = viewModel::setSelectedFolder,
                                onSelectFormat = viewModel::setSelectedFormat,
                                onSelectTag = viewModel::setSelectedTag,
                                onSelectNote = { note -> viewModel.selectNote(note, openOnMobile = false) },
                                onNewNoteClick = { viewModel.setNewNoteDialogOpen(true) },
                                onImportClick = onRequestImportFilePicker,
                                onSaveNoteLocally = { note -> viewModel.saveNoteLocallyToDevice(note) },
                                modifier = Modifier
                                    .width(336.dp)
                                    .fillMaxHeight()
                            )
                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                            // Right 16:10 Editor & Live Preview Pane
                            val active = uiState.activeNote
                            if (active != null) {
                                DocumentEditorPane(
                                    note = active,
                                    preferences = uiState.preferences,
                                    viewMode = uiState.editorViewMode,
                                    isExpandedTablet = true,
                                    canUndo = uiState.undoStack.isNotEmpty(),
                                    canRedo = uiState.redoStack.isNotEmpty(),
                                    isFindBarVisible = uiState.isFindBarVisible,
                                    findQuery = uiState.findQuery,
                                    replaceQuery = uiState.replaceQuery,
                                    isSaveLocallySheetOpen = uiState.isSaveLocallySheetOpen,
                                    folders = uiState.folders,
                                    onBack = null,
                                    onContentChange = viewModel::updateActiveNoteContent,
                                    onViewModeChange = viewModel::setEditorViewMode,
                                    onUndo = viewModel::undo,
                                    onRedo = viewModel::redo,
                                    onToggleFindBar = viewModel::toggleFindBar,
                                    onFindQueryChange = viewModel::setFindQuery,
                                    onReplaceQueryChange = viewModel::setReplaceQuery,
                                    onReplace = viewModel::replaceInActiveNote,
                                    onTogglePin = { viewModel.togglePin(active) },
                                    onOpenSaveLocally = { viewModel.setSaveLocallySheetOpen(true) },
                                    onDismissSaveLocally = { viewModel.setSaveLocallySheetOpen(false) },
                                    onSaveToDeviceDocuments = { fileName ->
                                        viewModel.saveNoteLocallyToDevice(active, fileName, toDownloads = false)
                                    },
                                    onSaveToDeviceDownloads = { fileName ->
                                        viewModel.saveNoteLocallyToDevice(active, fileName, toDownloads = true)
                                    },
                                    onSaveAsSystemPicker = onRequestSaveAsSystemPicker,
                                    onExportHtmlLocally = viewModel::exportActiveNoteAsHtmlLocally,
                                    onRenameOrMove = viewModel::renameActiveNote,
                                    onDuplicate = { viewModel.duplicateNote(active) },
                                    onDelete = { viewModel.deleteNote(active) },
                                    onToggleTaskLine = viewModel::toggleMarkdownCheckboxAtLine,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            } else {
                                EmptyTabletEditorPlaceholder(
                                    onNewNote = { viewModel.setNewNoteDialogOpen(true) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                        }

                        AppSection.TODO -> {
                            TodoTxtScreen(
                                todoItems = uiState.todoItems,
                                rawTodoContent = uiState.todoDocument?.content.orEmpty(),
                                onAddTodo = viewModel::addTodoItem,
                                onToggleTodo = viewModel::toggleTodoItem,
                                onDeleteTodo = viewModel::deleteTodoItem,
                                onUpdateRawContent = viewModel::updateRawTodoContent,
                                onSaveTodoLocally = viewModel::saveTodoLocallyToDevice,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        AppSection.QUICKNOTE -> {
                            QuickNoteScreen(
                                quickNoteContent = uiState.quickNoteDocument?.content.orEmpty(),
                                fontSizeSp = uiState.preferences.fontSizeSp,
                                onContentChange = viewModel::updateQuickNoteContent,
                                onAppendTimestamp = viewModel::appendTimestampToQuickNote,
                                onSaveLocally = viewModel::saveQuickNoteLocallyToDevice,
                                onPromoteToNote = { content ->
                                    viewModel.createNewNote(
                                        titleOrFileName = "QuickNote_${System.currentTimeMillis() % 10000}",
                                        folderName = "Notes",
                                        format = NoteFormat.MARKDOWN,
                                        initialTemplate = content
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        AppSection.FILES -> {
                            LocalFilesBrowserScreen(
                                notes = uiState.notes,
                                folders = uiState.folders,
                                selectedFolder = uiState.selectedFolder,
                                publicDocumentsPath = uiState.publicDocumentsPath,
                                onSelectFolder = viewModel::setSelectedFolder,
                                onOpenNote = { note ->
                                    viewModel.selectNote(note, openOnMobile = false)
                                    viewModel.setSection(AppSection.NOTES)
                                },
                                onSaveNoteLocally = { note -> viewModel.saveNoteLocallyToDevice(note) },
                                onExportAllLocally = viewModel::exportAllNotesLocally,
                                onImportLocalFile = onRequestImportFilePicker,
                                onCreateFolder = viewModel::createNewFolder,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        AppSection.SETTINGS -> {
                            SettingsScreen(
                                preferences = uiState.preferences,
                                workspacePath = uiState.workspacePath,
                                publicDocumentsPath = uiState.publicDocumentsPath,
                                onThemeModeChange = viewModel::updateThemeMode,
                                onAccentPaletteChange = viewModel::updateAccentPalette,
                                onEditorFontChange = viewModel::updateEditorFont,
                                onFontSizeChange = viewModel::updateFontSize,
                                onShowLineNumbersChange = viewModel::updateShowLineNumbers,
                                onWordWrapChange = viewModel::updateWordWrap,
                                onAutoSaveChange = viewModel::updateAutoSave,
                                onExportAllLocally = viewModel::exportAllNotesLocally,
                                onImportLocalFile = onRequestImportFilePicker,
                                onRestoreTemplates = viewModel::restoreStarterTemplates,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                // =========================================================================
                // COMPACT / HANDHELD LAYOUT
                // =========================================================================
                val showFullScreenEditor = uiState.activeSection == AppSection.NOTES &&
                    uiState.isMobileEditorOpen &&
                    uiState.activeNote != null

                if (showFullScreenEditor) {
                    val active = uiState.activeNote!!
                    DocumentEditorPane(
                        note = active,
                        preferences = uiState.preferences,
                        viewMode = uiState.editorViewMode,
                        isExpandedTablet = false,
                        canUndo = uiState.undoStack.isNotEmpty(),
                        canRedo = uiState.redoStack.isNotEmpty(),
                        isFindBarVisible = uiState.isFindBarVisible,
                        findQuery = uiState.findQuery,
                        replaceQuery = uiState.replaceQuery,
                        isSaveLocallySheetOpen = uiState.isSaveLocallySheetOpen,
                        folders = uiState.folders,
                        onBack = { viewModel.closeMobileEditor() },
                        onContentChange = viewModel::updateActiveNoteContent,
                        onViewModeChange = viewModel::setEditorViewMode,
                        onUndo = viewModel::undo,
                        onRedo = viewModel::redo,
                        onToggleFindBar = viewModel::toggleFindBar,
                        onFindQueryChange = viewModel::setFindQuery,
                        onReplaceQueryChange = viewModel::setReplaceQuery,
                        onReplace = viewModel::replaceInActiveNote,
                        onTogglePin = { viewModel.togglePin(active) },
                        onOpenSaveLocally = { viewModel.setSaveLocallySheetOpen(true) },
                        onDismissSaveLocally = { viewModel.setSaveLocallySheetOpen(false) },
                        onSaveToDeviceDocuments = { fileName ->
                            viewModel.saveNoteLocallyToDevice(active, fileName, toDownloads = false)
                        },
                        onSaveToDeviceDownloads = { fileName ->
                            viewModel.saveNoteLocallyToDevice(active, fileName, toDownloads = true)
                        },
                        onSaveAsSystemPicker = onRequestSaveAsSystemPicker,
                        onExportHtmlLocally = viewModel::exportActiveNoteAsHtmlLocally,
                        onRenameOrMove = viewModel::renameActiveNote,
                        onDuplicate = { viewModel.duplicateNote(active) },
                        onDelete = { viewModel.deleteNote(active) },
                        onToggleTaskLine = viewModel::toggleMarkdownCheckboxAtLine,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Scaffold(
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        floatingActionButton = {
                            if (uiState.activeSection == AppSection.NOTES) {
                                ExtendedFloatingActionButton(
                                    onClick = { viewModel.setNewNoteDialogOpen(true) },
                                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                                    text = { Text("New Note") },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.testTag("new_note_fab")
                                )
                            }
                        },
                        bottomBar = {
                            MobileBottomNavigationBar(
                                activeSection = uiState.activeSection,
                                onSelectSection = viewModel::setSection
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (uiState.activeSection) {
                                AppSection.NOTES -> {
                                    NotesListExplorerPane(
                                        uiState = uiState,
                                        isExpandedTablet = false,
                                        onSearchQueryChange = viewModel::setSearchQuery,
                                        onSelectFolder = viewModel::setSelectedFolder,
                                        onSelectFormat = viewModel::setSelectedFormat,
                                        onSelectTag = viewModel::setSelectedTag,
                                        onSelectNote = { note -> viewModel.selectNote(note, openOnMobile = true) },
                                        onNewNoteClick = { viewModel.setNewNoteDialogOpen(true) },
                                        onImportClick = onRequestImportFilePicker,
                                        onSaveNoteLocally = { note -> viewModel.saveNoteLocallyToDevice(note) },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                AppSection.TODO -> {
                                    TodoTxtScreen(
                                        todoItems = uiState.todoItems,
                                        rawTodoContent = uiState.todoDocument?.content.orEmpty(),
                                        onAddTodo = viewModel::addTodoItem,
                                        onToggleTodo = viewModel::toggleTodoItem,
                                        onDeleteTodo = viewModel::deleteTodoItem,
                                        onUpdateRawContent = viewModel::updateRawTodoContent,
                                        onSaveTodoLocally = viewModel::saveTodoLocallyToDevice
                                    )
                                }

                                AppSection.QUICKNOTE -> {
                                    QuickNoteScreen(
                                        quickNoteContent = uiState.quickNoteDocument?.content.orEmpty(),
                                        fontSizeSp = uiState.preferences.fontSizeSp,
                                        onContentChange = viewModel::updateQuickNoteContent,
                                        onAppendTimestamp = viewModel::appendTimestampToQuickNote,
                                        onSaveLocally = viewModel::saveQuickNoteLocallyToDevice,
                                        onPromoteToNote = { content ->
                                            viewModel.createNewNote(
                                                titleOrFileName = "QuickNote_${System.currentTimeMillis() % 10000}",
                                                folderName = "Notes",
                                                format = NoteFormat.MARKDOWN,
                                                initialTemplate = content
                                            )
                                        }
                                    )
                                }

                                AppSection.FILES -> {
                                    LocalFilesBrowserScreen(
                                        notes = uiState.notes,
                                        folders = uiState.folders,
                                        selectedFolder = uiState.selectedFolder,
                                        publicDocumentsPath = uiState.publicDocumentsPath,
                                        onSelectFolder = viewModel::setSelectedFolder,
                                        onOpenNote = { note ->
                                            viewModel.selectNote(note, openOnMobile = true)
                                            viewModel.setSection(AppSection.NOTES)
                                        },
                                        onSaveNoteLocally = { note -> viewModel.saveNoteLocallyToDevice(note) },
                                        onExportAllLocally = viewModel::exportAllNotesLocally,
                                        onImportLocalFile = onRequestImportFilePicker,
                                        onCreateFolder = viewModel::createNewFolder
                                    )
                                }

                                AppSection.SETTINGS -> {
                                    SettingsScreen(
                                        preferences = uiState.preferences,
                                        workspacePath = uiState.workspacePath,
                                        publicDocumentsPath = uiState.publicDocumentsPath,
                                        onThemeModeChange = viewModel::updateThemeMode,
                                        onAccentPaletteChange = viewModel::updateAccentPalette,
                                        onEditorFontChange = viewModel::updateEditorFont,
                                        onFontSizeChange = viewModel::updateFontSize,
                                        onShowLineNumbersChange = viewModel::updateShowLineNumbers,
                                        onWordWrapChange = viewModel::updateWordWrap,
                                        onAutoSaveChange = viewModel::updateAutoSave,
                                        onExportAllLocally = viewModel::exportAllNotesLocally,
                                        onImportLocalFile = onRequestImportFilePicker,
                                        onRestoreTemplates = viewModel::restoreStarterTemplates
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isNewNoteDialogOpen) {
        NewNoteCreationDialog(
            folders = uiState.folders,
            defaultFolder = uiState.selectedFolder ?: uiState.preferences.defaultFolder,
            defaultFormat = uiState.preferences.defaultFormat,
            onDismiss = { viewModel.setNewNoteDialogOpen(false) },
            onCreate = { title, folder, format ->
                viewModel.createNewNote(title, folder, format)
            }
        )
    }
}

@Composable
private fun NotesListExplorerPane(
    uiState: NotesUiState,
    isExpandedTablet: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectFolder: (String?) -> Unit,
    onSelectFormat: (NoteFormat?) -> Unit,
    onSelectTag: (String?) -> Unit,
    onSelectNote: (NoteDocument) -> Unit,
    onNewNoteClick: () -> Unit,
    onImportClick: () -> Unit,
    onSaveNoteLocally: (NoteDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Brand Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isExpandedTablet) {
                    SimpleNotesLogo(size = 38.dp)
                }
                Column {
                    Text(
                        text = "SimpleNotes",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = InstrumentSerifFamily
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${uiState.notes.size} documents · Local-first workspace",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onImportClick,
                modifier = Modifier.testTag("header_import_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileOpen,
                    contentDescription = "Import file from device",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Fluent Search Input
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search notes, #tags, or content...") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_notes_input")
        )

        // Folder & Format Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = uiState.selectedFolder == null && uiState.selectedFormat == null && uiState.selectedTag == null,
                onClick = {
                    onSelectFolder(null)
                    onSelectFormat(null)
                    onSelectTag(null)
                },
                label = { Text("All") }
            )
            uiState.folders.forEach { folder ->
                FilterChip(
                    selected = uiState.selectedFolder == folder,
                    onClick = {
                        onSelectFolder(if (uiState.selectedFolder == folder) null else folder)
                    },
                    label = { Text(folder) }
                )
            }
            NoteFormat.entries.forEach { format ->
                FilterChip(
                    selected = uiState.selectedFormat == format,
                    onClick = {
                        onSelectFormat(if (uiState.selectedFormat == format) null else format)
                    },
                    label = { Text(format.shortBadge) }
                )
            }
        }

        // Tag Row if any tags exist
        if (uiState.allTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.allTags.forEach { tag ->
                    val active = uiState.selectedTag == tag
                    Surface(
                        color = if (active) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectTag(if (active) null else tag) }
                    ) {
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Notes List
        val notes = uiState.filteredNotes
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SimpleNotesLogo(size = 48.dp)
                    Text(
                        text = "No matching notes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onNewNoteClick) {
                        Text("Create a new note")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    val isSelectedOnTablet = isExpandedTablet && uiState.activeNote?.id == note.id
                    NoteListItemCard(
                        note = note,
                        isSelected = isSelectedOnTablet,
                        onClick = { onSelectNote(note) },
                        onQuickSaveLocally = { onSaveNoteLocally(note) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteListItemCard(
    note: NoteDocument,
    isSelected: Boolean,
    onClick: () -> Unit,
    onQuickSaveLocally: () -> Unit
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        note.isPinned -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        note.isPinned -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .testTag("note_card_${note.fileName}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = note.format.shortBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onQuickSaveLocally,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("card_save_locally_${note.fileName}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Save file locally to device",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = note.previewSnippet,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${note.folderName} · ${note.wordCount}w",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = if (note.localSavedPath != null) "Saved on device" else note.formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (note.localSavedPath != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun TabletNavigationRail(
    activeSection: AppSection,
    onSelectSection: (AppSection) -> Unit,
    onNewNoteClick: () -> Unit,
    onQuickSaveClick: () -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            ) {
                SimpleNotesLogo(size = 42.dp)
                FloatingActionButton(
                    onClick = onNewNoteClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("tablet_new_note_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create new note")
                }
            }
        }
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        AppSection.entries.forEach { section ->
            val selected = activeSection == section
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectSection(section) },
                icon = {
                    Icon(
                        imageVector = sectionIcon(section, selected),
                        contentDescription = section.label
                    )
                },
                label = { Text(section.label, style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.testTag("nav_rail_${section.name.lowercase()}")
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onQuickSaveClick,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .testTag("rail_quick_save_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Save,
                contentDescription = "Quick save active note locally",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MobileBottomNavigationBar(
    activeSection: AppSection,
    onSelectSection: (AppSection) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        windowInsets = WindowInsets.navigationBars
    ) {
        AppSection.entries.forEach { section ->
            val selected = activeSection == section
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectSection(section) },
                icon = {
                    Icon(
                        imageVector = sectionIcon(section, selected),
                        contentDescription = section.label
                    )
                },
                label = { Text(section.label, maxLines = 1) },
                modifier = Modifier.testTag("bottom_nav_${section.name.lowercase()}")
            )
        }
    }
}

private fun sectionIcon(section: AppSection, selected: Boolean): ImageVector = when (section) {
    AppSection.NOTES -> if (selected) Icons.Filled.Description else Icons.Outlined.Description
    AppSection.TODO -> if (selected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle
    AppSection.QUICKNOTE -> if (selected) Icons.Filled.FlashOn else Icons.Outlined.FlashOn
    AppSection.FILES -> if (selected) Icons.Filled.Folder else Icons.Outlined.Folder
    AppSection.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
}

@Composable
private fun EmptyTabletEditorPlaceholder(
    onNewNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            SimpleNotesLogo(size = 64.dp)
            Text(
                text = "16:10 Studio Workspace",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select a document from the left pane or create a new Markdown, Todo.txt, or CSV note.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onNewNote,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Document")
            }
        }
    }
}

@Composable
private fun NewNoteCreationDialog(
    folders: List<String>,
    defaultFolder: String,
    defaultFormat: NoteFormat,
    onDismiss: () -> Unit,
    onCreate: (String, String, NoteFormat) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var selectedFolder by remember(defaultFolder) { mutableStateOf(defaultFolder) }
    var selectedFormat by remember(defaultFormat) { mutableStateOf(defaultFormat) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Document",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Document Title or Filename") },
                    placeholder = { Text("e.g., Product Spec") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_note_title_input")
                )

                Text(
                    text = "Format",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteFormat.entries.forEach { format ->
                        val active = selectedFormat == format
                        Surface(
                            color = if (active) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedFormat = format }
                        ) {
                            Text(
                                text = "${format.label} (${format.extension})",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Workspace Folder",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    folders.forEach { folder ->
                        val active = selectedFolder == folder
                        Surface(
                            color = if (active) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedFolder = folder }
                        ) {
                            Text(
                                text = folder,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(titleInput, selectedFolder, selectedFormat) },
                modifier = Modifier.testTag("confirm_create_note_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
