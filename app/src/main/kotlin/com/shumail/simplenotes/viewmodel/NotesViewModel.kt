package com.shumail.simplenotes.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shumail.simplenotes.data.LocalFileStorageManager
import com.shumail.simplenotes.model.AccentPalette
import com.shumail.simplenotes.model.AppPreferences
import com.shumail.simplenotes.model.AppSection
import com.shumail.simplenotes.model.EditorFont
import com.shumail.simplenotes.model.EditorViewMode
import com.shumail.simplenotes.model.LocalSaveResult
import com.shumail.simplenotes.model.NoteDocument
import com.shumail.simplenotes.model.NoteFormat
import com.shumail.simplenotes.model.SortOrder
import com.shumail.simplenotes.model.ThemeMode
import com.shumail.simplenotes.model.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NotesUiState(
    val activeSection: AppSection = AppSection.NOTES,
    val notes: List<NoteDocument> = emptyList(),
    val folders: List<String> = listOf("Notes", "Work", "Journal", "Templates", "QuickNote"),
    val selectedFolder: String? = null,
    val selectedFormat: NoteFormat? = null,
    val selectedTag: String? = null,
    val searchQuery: String = "",
    val activeNote: NoteDocument? = null,
    val isMobileEditorOpen: Boolean = false,
    val editorViewMode: EditorViewMode = EditorViewMode.EDIT,
    val undoStack: List<String> = emptyList(),
    val redoStack: List<String> = emptyList(),
    val findQuery: String = "",
    val replaceQuery: String = "",
    val isFindBarVisible: Boolean = false,
    val isSaveLocallySheetOpen: Boolean = false,
    val isNewNoteDialogOpen: Boolean = false,
    val preferences: AppPreferences = AppPreferences(),
    val lastSaveResult: LocalSaveResult? = null,
    val workspacePath: String = "",
    val publicDocumentsPath: String = ""
) {
    val filteredNotes: List<NoteDocument>
        get() {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            val base = notes.filter { note ->
                val matchesFolder = selectedFolder == null || note.folderName.equals(selectedFolder, ignoreCase = true)
                val matchesFormat = selectedFormat == null || note.format == selectedFormat
                val matchesTag = selectedTag == null || note.tags.any { it.equals(selectedTag, ignoreCase = true) }
                val matchesQuery = q.isEmpty() ||
                    note.title.lowercase(Locale.ROOT).contains(q) ||
                    note.fileName.lowercase(Locale.ROOT).contains(q) ||
                    note.content.lowercase(Locale.ROOT).contains(q) ||
                    note.tags.any { it.contains(q) }
                matchesFolder && matchesFormat && matchesTag && matchesQuery
            }

            return when (preferences.sortOrder) {
                SortOrder.MODIFIED_DESC -> base.sortedWith(
                    compareByDescending<NoteDocument> { it.isPinned }.thenByDescending { it.lastModified }
                )
                SortOrder.TITLE_ASC -> base.sortedWith(
                    compareByDescending<NoteDocument> { it.isPinned }.thenBy { it.title.lowercase(Locale.ROOT) }
                )
                SortOrder.FORMAT -> base.sortedWith(
                    compareByDescending<NoteDocument> { it.isPinned }.thenBy { it.format.label }
                )
                SortOrder.WORD_COUNT -> base.sortedWith(
                    compareByDescending<NoteDocument> { it.isPinned }.thenByDescending { it.wordCount }
                )
            }
        }

    val allTags: List<String>
        get() = notes.flatMap { it.tags }.distinct().sorted()

    val todoDocument: NoteDocument?
        get() = notes.firstOrNull { it.format == NoteFormat.TODOTXT }
            ?: notes.firstOrNull { it.fileName.contains("todo", ignoreCase = true) }

    val todoItems: List<TodoItem>
        get() {
            val doc = todoDocument ?: return emptyList()
            return doc.content.lines().mapIndexedNotNull { index, line ->
                TodoItem.parseLine(index, line)
            }
        }

    val quickNoteDocument: NoteDocument?
        get() = notes.firstOrNull { it.folderName == "QuickNote" || it.fileName.equals("QuickNote.md", ignoreCase = true) }
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = LocalFileStorageManager(application.applicationContext)

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        refreshWorkspace(selectDefaultIfEmpty = true)
    }

    fun refreshWorkspace(selectDefaultIfEmpty: Boolean = false) {
        val prefs = storage.loadPreferences()
        val folders = storage.listFolders()
        val allNotes = storage.loadAllNotes()
        val currentActiveId = _uiState.value.activeNote?.id
        val nextActive = allNotes.firstOrNull { it.id == currentActiveId }
            ?: if (selectDefaultIfEmpty) allNotes.firstOrNull { it.isPinned } ?: allNotes.firstOrNull() else _uiState.value.activeNote

        _uiState.update { state ->
            state.copy(
                notes = allNotes,
                folders = folders,
                activeNote = nextActive,
                preferences = prefs,
                workspacePath = storage.getWorkspaceRootPath(),
                publicDocumentsPath = storage.getPublicDocumentsPath()
            )
        }
    }

    fun setSection(section: AppSection) {
        _uiState.update { it.copy(activeSection = section, isMobileEditorOpen = false) }
    }

    fun selectNote(note: NoteDocument, openOnMobile: Boolean = true) {
        _uiState.update {
            it.copy(
                activeNote = note,
                isMobileEditorOpen = openOnMobile,
                undoStack = emptyList(),
                redoStack = emptyList()
            )
        }
    }

    fun closeMobileEditor() {
        _uiState.update { it.copy(isMobileEditorOpen = false) }
    }

    fun setEditorViewMode(mode: EditorViewMode) {
        _uiState.update { it.copy(editorViewMode = mode) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedFolder(folder: String?) {
        _uiState.update { it.copy(selectedFolder = folder) }
    }

    fun setSelectedFormat(format: NoteFormat?) {
        _uiState.update { it.copy(selectedFormat = format) }
    }

    fun setSelectedTag(tag: String?) {
        _uiState.update { it.copy(selectedTag = tag) }
    }

    fun toggleFindBar() {
        _uiState.update { it.copy(isFindBarVisible = !it.isFindBarVisible) }
    }

    fun setFindQuery(query: String) {
        _uiState.update { it.copy(findQuery = query) }
    }

    fun setReplaceQuery(query: String) {
        _uiState.update { it.copy(replaceQuery = query) }
    }

    fun replaceInActiveNote(replaceAll: Boolean) {
        val state = _uiState.value
        val active = state.activeNote ?: return
        val find = state.findQuery
        if (find.isEmpty()) return

        val updatedContent = if (replaceAll) {
            active.content.replace(find, state.replaceQuery, ignoreCase = true)
        } else {
            active.content.replaceFirst(find, state.replaceQuery, ignoreCase = true)
        }
        updateActiveNoteContent(updatedContent)
    }

    fun setSaveLocallySheetOpen(open: Boolean) {
        _uiState.update { it.copy(isSaveLocallySheetOpen = open) }
    }

    fun setNewNoteDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isNewNoteDialogOpen = open) }
    }

    fun clearLastSaveResult() {
        _uiState.update { it.copy(lastSaveResult = null) }
    }

    fun updateActiveNoteContent(newContent: String) {
        val current = _uiState.value.activeNote ?: return
        if (current.content == newContent) return

        val nextUndo = (_uiState.value.undoStack + current.content).takeLast(40)
        val savedNote = if (_uiState.value.preferences.autoSave) {
            storage.saveNoteToWorkspace(
                folderName = current.folderName,
                fileName = current.fileName,
                content = newContent,
                isPinned = current.isPinned,
                isFavorite = current.isFavorite
            )
        } else {
            current.copy(content = newContent, lastModified = System.currentTimeMillis())
        }

        _uiState.update { state ->
            val updatedList = state.notes.map { if (it.id == savedNote.id) savedNote else it }
            state.copy(
                activeNote = savedNote,
                notes = updatedList,
                undoStack = nextUndo,
                redoStack = emptyList()
            )
        }
    }

    fun undo() {
        val state = _uiState.value
        val active = state.activeNote ?: return
        val previous = state.undoStack.lastOrNull() ?: return
        val remainingUndo = state.undoStack.dropLast(1)
        val nextRedo = (state.redoStack + active.content).takeLast(40)

        val savedNote = storage.saveNoteToWorkspace(
            folderName = active.folderName,
            fileName = active.fileName,
            content = previous,
            isPinned = active.isPinned,
            isFavorite = active.isFavorite
        )
        _uiState.update { s ->
            s.copy(
                activeNote = savedNote,
                notes = s.notes.map { if (it.id == savedNote.id) savedNote else it },
                undoStack = remainingUndo,
                redoStack = nextRedo
            )
        }
    }

    fun redo() {
        val state = _uiState.value
        val active = state.activeNote ?: return
        val nextContent = state.redoStack.lastOrNull() ?: return
        val remainingRedo = state.redoStack.dropLast(1)
        val nextUndo = (state.undoStack + active.content).takeLast(40)

        val savedNote = storage.saveNoteToWorkspace(
            folderName = active.folderName,
            fileName = active.fileName,
            content = nextContent,
            isPinned = active.isPinned,
            isFavorite = active.isFavorite
        )
        _uiState.update { s ->
            s.copy(
                activeNote = savedNote,
                notes = s.notes.map { if (it.id == savedNote.id) savedNote else it },
                undoStack = nextUndo,
                redoStack = remainingRedo
            )
        }
    }

    fun createNewNote(
        titleOrFileName: String,
        folderName: String,
        format: NoteFormat,
        initialTemplate: String = ""
    ) {
        val cleanBase = titleOrFileName.trim().ifBlank {
            "Note_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}"
        }.removeSuffix(format.extension)
        val fileName = "$cleanBase${format.extension}"

        val defaultBody = initialTemplate.ifBlank {
            when (format) {
                NoteFormat.MARKDOWN -> "# $cleanBase\n\n"
                NoteFormat.TODOTXT -> "(A) ${TodoItem.todayIso()} $cleanBase +SimpleNotes @task\n"
                NoteFormat.CSV -> "Column 1,Column 2,Status\nItem 1,Details,Active\n"
                NoteFormat.ORGMODE -> "#+TITLE: $cleanBase\n#+DATE: ${TodoItem.todayIso()}\n\n* Heading 1\n"
                NoteFormat.ASCIIDOC -> "= $cleanBase\n:Date: ${TodoItem.todayIso()}\n\n== Section 1\n"
                NoteFormat.PLAINTEXT -> "$cleanBase\n\n"
            }
        }

        val created = storage.saveNoteToWorkspace(
            folderName = folderName,
            fileName = fileName,
            content = defaultBody,
            isPinned = false,
            isFavorite = false
        )
        val allNotes = storage.loadAllNotes()
        _uiState.update {
            it.copy(
                notes = allNotes,
                folders = storage.listFolders(),
                activeNote = created,
                activeSection = AppSection.NOTES,
                isMobileEditorOpen = true,
                isNewNoteDialogOpen = false
            )
        }
    }

    fun createNewFolder(folderName: String) {
        if (storage.createFolder(folderName)) {
            _uiState.update {
                it.copy(
                    folders = storage.listFolders(),
                    lastSaveResult = LocalSaveResult(
                        success = true,
                        displayPath = folderName,
                        message = "Created folder '$folderName'"
                    )
                )
            }
        }
    }

    fun renameActiveNote(newFileName: String, newFolderName: String) {
        val current = _uiState.value.activeNote ?: return
        val updated = storage.renameOrMoveNote(current, newFolderName, newFileName)
        val allNotes = storage.loadAllNotes()
        _uiState.update {
            it.copy(
                notes = allNotes,
                activeNote = updated,
                lastSaveResult = LocalSaveResult(
                    success = true,
                    displayPath = "${updated.folderName}/${updated.fileName}",
                    message = "Renamed to ${updated.fileName}"
                )
            )
        }
    }

    fun duplicateNote(note: NoteDocument) {
        val base = note.fileName.substringBeforeLast(".")
        val copyName = "${base}_copy${note.format.extension}"
        val created = storage.saveNoteToWorkspace(
            folderName = note.folderName,
            fileName = copyName,
            content = note.content,
            isPinned = false,
            isFavorite = note.isFavorite
        )
        val allNotes = storage.loadAllNotes()
        _uiState.update {
            it.copy(
                notes = allNotes,
                activeNote = created,
                lastSaveResult = LocalSaveResult(
                    success = true,
                    displayPath = copyName,
                    message = "Duplicated as $copyName"
                )
            )
        }
    }

    fun deleteNote(note: NoteDocument) {
        storage.deleteNote(note)
        val allNotes = storage.loadAllNotes()
        val nextActive = if (_uiState.value.activeNote?.id == note.id) {
            allNotes.firstOrNull()
        } else {
            _uiState.value.activeNote
        }
        _uiState.update {
            it.copy(
                notes = allNotes,
                activeNote = nextActive,
                isMobileEditorOpen = false,
                lastSaveResult = LocalSaveResult(
                    success = true,
                    displayPath = note.fileName,
                    message = "Deleted ${note.fileName}"
                )
            )
        }
    }

    fun togglePin(note: NoteDocument) {
        storage.togglePin(note)
        refreshWorkspace()
    }

    fun toggleFavorite(note: NoteDocument) {
        storage.toggleFavorite(note)
        refreshWorkspace()
    }

    /**
     * Saves the note locally to device storage (Documents/SimpleNotes or Downloads/SimpleNotes).
     */
    fun saveNoteLocallyToDevice(
        note: NoteDocument? = _uiState.value.activeNote,
        customFileName: String? = null,
        toDownloads: Boolean = false
    ) {
        val target = note ?: return
        viewModelScope.launch {
            val fileName = customFileName?.trim()?.takeIf { it.isNotEmpty() } ?: target.fileName
            val dirType = if (toDownloads) Environment.DIRECTORY_DOWNLOADS else Environment.DIRECTORY_DOCUMENTS
            val result = storage.saveNoteLocallyToDevice(target, fileName, dirType)
            val allNotes = storage.loadAllNotes()
            val refreshedActive = allNotes.firstOrNull { it.id == target.id } ?: target
            _uiState.update {
                it.copy(
                    notes = allNotes,
                    activeNote = refreshedActive,
                    lastSaveResult = result,
                    isSaveLocallySheetOpen = false
                )
            }
        }
    }

    /**
     * Saves the note to a user-selected Uri via the Android system file saver ("Save File As...").
     */
    fun saveNoteToCustomUri(uri: Uri, customContent: String? = null) {
        val target = _uiState.value.activeNote ?: return
        viewModelScope.launch {
            val result = storage.saveToCustomUri(target, uri, customContent ?: target.content)
            val allNotes = storage.loadAllNotes()
            val refreshedActive = allNotes.firstOrNull { it.id == target.id } ?: target
            _uiState.update {
                it.copy(
                    notes = allNotes,
                    activeNote = refreshedActive,
                    lastSaveResult = result,
                    isSaveLocallySheetOpen = false
                )
            }
        }
    }

    fun exportActiveNoteAsHtmlLocally() {
        val target = _uiState.value.activeNote ?: return
        viewModelScope.launch {
            val result = storage.exportNoteAsHtmlLocally(target)
            _uiState.update {
                it.copy(
                    lastSaveResult = result,
                    isSaveLocallySheetOpen = false
                )
            }
        }
    }

    fun exportAllNotesLocally() {
        viewModelScope.launch {
            val result = storage.exportAllNotesLocally()
            refreshWorkspace()
            _uiState.update { it.copy(lastSaveResult = result) }
        }
    }

    fun importFileFromUri(uri: Uri) {
        viewModelScope.launch {
            val folder = _uiState.value.selectedFolder ?: "Notes"
            val imported = storage.importNoteFromUri(uri, folder)
            if (imported != null) {
                val allNotes = storage.loadAllNotes()
                _uiState.update {
                    it.copy(
                        notes = allNotes,
                        activeNote = imported,
                        activeSection = AppSection.NOTES,
                        isMobileEditorOpen = true,
                        lastSaveResult = LocalSaveResult(
                            success = true,
                            displayPath = imported.fileName,
                            message = "Imported ${imported.fileName} into $folder"
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        lastSaveResult = LocalSaveResult(
                            success = false,
                            displayPath = "",
                            message = "Could not import selected file"
                        )
                    )
                }
            }
        }
    }

    // --- Todo.txt Operations ---

    fun addTodoItem(
        description: String,
        priority: Char?,
        project: String?,
        contextTag: String?,
        dueDate: String?
    ) {
        if (description.isBlank()) return
        val doc = ensureTodoDocument()
        val item = TodoItem(
            lineIndex = -1,
            isCompleted = false,
            priority = priority,
            creationDate = TodoItem.todayIso(),
            description = description.trim(),
            projects = listOfNotNull(project?.trim()?.takeIf { it.isNotEmpty() }),
            contexts = listOfNotNull(contextTag?.trim()?.takeIf { it.isNotEmpty() }),
            dueDate = dueDate?.trim()?.takeIf { it.isNotEmpty() },
            rawLine = ""
        )
        val newLine = item.toTodoTxtLine()
        val nextContent = if (doc.content.isBlank()) newLine else "${doc.content.trimEnd()}\n$newLine"
        storage.saveNoteToWorkspace(doc.folderName, doc.fileName, nextContent, doc.isPinned, doc.isFavorite)
        refreshWorkspace()
    }

    fun toggleTodoItem(item: TodoItem) {
        val doc = ensureTodoDocument()
        val lines = doc.content.lines().toMutableList()
        if (item.lineIndex !in lines.indices) return

        val toggled = if (item.isCompleted) {
            item.copy(isCompleted = false, completionDate = null)
        } else {
            item.copy(isCompleted = true, completionDate = TodoItem.todayIso())
        }
        lines[item.lineIndex] = toggled.toTodoTxtLine()
        storage.saveNoteToWorkspace(doc.folderName, doc.fileName, lines.joinToString("\n"), doc.isPinned, doc.isFavorite)
        refreshWorkspace()
    }

    fun deleteTodoItem(item: TodoItem) {
        val doc = ensureTodoDocument()
        val lines = doc.content.lines().toMutableList()
        if (item.lineIndex !in lines.indices) return
        lines.removeAt(item.lineIndex)
        storage.saveNoteToWorkspace(doc.folderName, doc.fileName, lines.joinToString("\n"), doc.isPinned, doc.isFavorite)
        refreshWorkspace()
    }

    fun updateRawTodoContent(newContent: String) {
        val doc = ensureTodoDocument()
        storage.saveNoteToWorkspace(doc.folderName, doc.fileName, newContent, doc.isPinned, doc.isFavorite)
        refreshWorkspace()
    }

    fun saveTodoLocallyToDevice() {
        val doc = ensureTodoDocument()
        saveNoteLocallyToDevice(doc, "todo.txt", toDownloads = false)
    }

    private fun ensureTodoDocument(): NoteDocument {
        val existing = _uiState.value.todoDocument
        if (existing != null) return existing
        return storage.saveNoteToWorkspace(
            folderName = "Notes",
            fileName = "todo.todo.txt",
            content = "(A) ${TodoItem.todayIso()} Welcome to SimpleNotes To-Do +SimpleNotes @today\n"
        )
    }

    // --- QuickNote Operations ---

    fun updateQuickNoteContent(newContent: String) {
        val doc = ensureQuickNoteDocument()
        storage.saveNoteToWorkspace(doc.folderName, doc.fileName, newContent, doc.isPinned, doc.isFavorite)
        refreshWorkspace()
    }

    fun appendTimestampToQuickNote() {
        val doc = ensureQuickNoteDocument()
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val updated = "${doc.content.trimEnd()}\n\n### $stamp\n- "
        updateQuickNoteContent(updated)
    }

    fun saveQuickNoteLocallyToDevice() {
        val doc = ensureQuickNoteDocument()
        saveNoteLocallyToDevice(doc, "QuickNote.md", toDownloads = false)
    }

    private fun ensureQuickNoteDocument(): NoteDocument {
        val existing = _uiState.value.quickNoteDocument
        if (existing != null) return existing
        return storage.saveNoteToWorkspace(
            folderName = "QuickNote",
            fileName = "QuickNote.md",
            content = "# QuickNote Scratchpad\n\n"
        )
    }

    // --- Interactive Markdown Checkbox Toggle in Preview ---

    fun toggleMarkdownCheckboxAtLine(lineIndex: Int) {
        val active = _uiState.value.activeNote ?: return
        val lines = active.content.lines().toMutableList()
        if (lineIndex !in lines.indices) return
        val line = lines[lineIndex]
        lines[lineIndex] = when {
            line.contains("- [ ]") -> line.replaceFirst("- [ ]", "- [x]")
            line.contains("- [x]") -> line.replaceFirst("- [x]", "- [ ]")
            line.contains("- [X]") -> line.replaceFirst("- [X]", "- [ ]")
            line.contains("* [ ]") -> line.replaceFirst("* [ ]", "* [x]")
            line.contains("* [x]") -> line.replaceFirst("* [x]", "* [ ]")
            else -> line
        }
        updateActiveNoteContent(lines.joinToString("\n"))
    }

    // --- Preferences Operations ---

    fun updateThemeMode(mode: ThemeMode) {
        updatePreferences { it.copy(themeMode = mode) }
    }

    fun updateAccentPalette(palette: AccentPalette) {
        updatePreferences { it.copy(accentPalette = palette) }
    }

    fun updateEditorFont(font: EditorFont) {
        updatePreferences { it.copy(editorFont = font) }
    }

    fun updateFontSize(sizeSp: Int) {
        updatePreferences { it.copy(fontSizeSp = sizeSp.coerceIn(12, 26)) }
    }

    fun updateShowLineNumbers(show: Boolean) {
        updatePreferences { it.copy(showLineNumbers = show) }
    }

    fun updateWordWrap(wrap: Boolean) {
        updatePreferences { it.copy(wordWrap = wrap) }
    }

    fun updateAutoSave(autoSave: Boolean) {
        updatePreferences { it.copy(autoSave = autoSave) }
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        updatePreferences { it.copy(sortOrder = sortOrder) }
    }

    fun restoreStarterTemplates() {
        storage.restoreStarterTemplates()
        refreshWorkspace(selectDefaultIfEmpty = true)
        _uiState.update {
            it.copy(
                lastSaveResult = LocalSaveResult(
                    success = true,
                    displayPath = "Workspace",
                    message = "Restored sample templates and starter documents"
                )
            )
        }
    }

    private fun updatePreferences(transform: (AppPreferences) -> AppPreferences) {
        val next = transform(_uiState.value.preferences)
        storage.savePreferences(next)
        _uiState.update { it.copy(preferences = next) }
    }
}
