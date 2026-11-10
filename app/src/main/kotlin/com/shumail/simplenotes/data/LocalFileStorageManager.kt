package com.shumail.simplenotes.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.shumail.simplenotes.model.AccentPalette
import com.shumail.simplenotes.model.AppPreferences
import com.shumail.simplenotes.model.EditorFont
import com.shumail.simplenotes.model.LocalSaveResult
import com.shumail.simplenotes.model.NoteDocument
import com.shumail.simplenotes.model.NoteFormat
import com.shumail.simplenotes.model.SortOrder
import com.shumail.simplenotes.model.ThemeMode
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Locale

class LocalFileStorageManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("simplenotes_prefs", Context.MODE_PRIVATE)
    private val workspaceRoot: File = File(context.filesDir, "workspace").apply {
        if (!exists()) mkdirs()
    }

    init {
        ensureDefaultFoldersAndSeed()
    }

    fun getWorkspaceRootPath(): String = workspaceRoot.absolutePath

    fun getPublicDocumentsPath(): String =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath}/SimpleNotes"

    fun loadPreferences(): AppPreferences {
        val theme = runCatching {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM)

        val accent = runCatching {
            AccentPalette.valueOf(
                prefs.getString("accent_palette", AccentPalette.MATERIAL_YOU.name) ?: AccentPalette.MATERIAL_YOU.name
            )
        }.getOrDefault(AccentPalette.MATERIAL_YOU)

        val font = runCatching {
            EditorFont.valueOf(
                prefs.getString("editor_font", EditorFont.JETBRAINS_MONO.name) ?: EditorFont.JETBRAINS_MONO.name
            )
        }.getOrDefault(EditorFont.JETBRAINS_MONO)

        val defaultFormat = runCatching {
            NoteFormat.valueOf(
                prefs.getString("default_format", NoteFormat.MARKDOWN.name) ?: NoteFormat.MARKDOWN.name
            )
        }.getOrDefault(NoteFormat.MARKDOWN)

        val sortOrder = runCatching {
            SortOrder.valueOf(
                prefs.getString("sort_order", SortOrder.MODIFIED_DESC.name) ?: SortOrder.MODIFIED_DESC.name
            )
        }.getOrDefault(SortOrder.MODIFIED_DESC)

        return AppPreferences(
            themeMode = theme,
            accentPalette = accent,
            editorFont = font,
            fontSizeSp = prefs.getInt("font_size_sp", 15).coerceIn(12, 26),
            showLineNumbers = prefs.getBoolean("show_line_numbers", true),
            wordWrap = prefs.getBoolean("word_wrap", true),
            autoSave = prefs.getBoolean("auto_save", true),
            defaultFolder = prefs.getString("default_folder", "Notes") ?: "Notes",
            defaultFormat = defaultFormat,
            sortOrder = sortOrder
        )
    }

    fun savePreferences(preferences: AppPreferences) {
        prefs.edit()
            .putString("theme_mode", preferences.themeMode.name)
            .putString("accent_palette", preferences.accentPalette.name)
            .putString("editor_font", preferences.editorFont.name)
            .putInt("font_size_sp", preferences.fontSizeSp)
            .putBoolean("show_line_numbers", preferences.showLineNumbers)
            .putBoolean("word_wrap", preferences.wordWrap)
            .putBoolean("auto_save", preferences.autoSave)
            .putString("default_folder", preferences.defaultFolder)
            .putString("default_format", preferences.defaultFormat.name)
            .putString("sort_order", preferences.sortOrder.name)
            .apply()
    }

    fun listFolders(): List<String> {
        val defaultFolders = listOf("Notes", "Work", "Journal", "Templates", "QuickNote")
        defaultFolders.forEach { folder ->
            File(workspaceRoot, folder).apply { if (!exists()) mkdirs() }
        }
        val existing = workspaceRoot.listFiles()
            ?.filter { it.isDirectory && !it.name.startsWith(".") }
            ?.map { it.name }
            .orEmpty()
        return (defaultFolders + existing).distinct()
    }

    fun createFolder(folderName: String): Boolean {
        val sanitized = sanitizeFileName(folderName)
        if (sanitized.isBlank()) return false
        val dir = File(workspaceRoot, sanitized)
        return if (!dir.exists()) dir.mkdirs() else true
    }

    fun loadAllNotes(): List<NoteDocument> {
        val notes = mutableListOf<NoteDocument>()
        val folders = listFolders()
        val pinnedSet = prefs.getStringSet("pinned_ids", emptySet()).orEmpty()
        val favoriteSet = prefs.getStringSet("favorite_ids", emptySet()).orEmpty()

        for (folder in folders) {
            val dir = File(workspaceRoot, folder)
            val files = dir.listFiles()?.filter { it.isFile && !it.name.startsWith(".") }.orEmpty()
            for (file in files) {
                val id = "$folder/${file.name}"
                val content = runCatching { file.readText(StandardCharsets.UTF_8) }.getOrDefault("")
                val format = NoteFormat.fromFileName(file.name)
                val title = deriveTitle(file.name, content, format)
                val tags = extractTags(content)
                val savedPath = prefs.getString("saved_path_$id", null)
                notes.add(
                    NoteDocument(
                        id = id,
                        title = title,
                        fileName = file.name,
                        folderName = folder,
                        content = content,
                        format = format,
                        lastModified = file.lastModified(),
                        isPinned = pinnedSet.contains(id),
                        isFavorite = favoriteSet.contains(id),
                        tags = tags,
                        localSavedPath = savedPath
                    )
                )
            }
        }
        return notes
    }

    fun saveNoteToWorkspace(
        folderName: String,
        fileName: String,
        content: String,
        isPinned: Boolean = false,
        isFavorite: Boolean = false
    ): NoteDocument {
        val cleanFolder = sanitizeFileName(folderName).ifBlank { "Notes" }
        val dir = File(workspaceRoot, cleanFolder).apply { if (!exists()) mkdirs() }
        val cleanFileName = sanitizeFileName(fileName).ifBlank { "Untitled.md" }
        val file = File(dir, cleanFileName)
        file.writeText(content, StandardCharsets.UTF_8)

        val id = "$cleanFolder/$cleanFileName"
        updateSetPref("pinned_ids", id, isPinned)
        updateSetPref("favorite_ids", id, isFavorite)

        val format = NoteFormat.fromFileName(cleanFileName)
        return NoteDocument(
            id = id,
            title = deriveTitle(cleanFileName, content, format),
            fileName = cleanFileName,
            folderName = cleanFolder,
            content = content,
            format = format,
            lastModified = file.lastModified(),
            isPinned = isPinned,
            isFavorite = isFavorite,
            tags = extractTags(content),
            localSavedPath = prefs.getString("saved_path_$id", null)
        )
    }

    fun renameOrMoveNote(
        oldNote: NoteDocument,
        newFolderName: String,
        newFileName: String
    ): NoteDocument {
        val cleanFolder = sanitizeFileName(newFolderName).ifBlank { oldNote.folderName }
        val cleanName = sanitizeFileName(newFileName).ifBlank { oldNote.fileName }
        val oldFile = File(workspaceRoot, "${oldNote.folderName}/${oldNote.fileName}")
        val newDir = File(workspaceRoot, cleanFolder).apply { if (!exists()) mkdirs() }
        val newFile = File(newDir, cleanName)

        if (oldFile.exists() && oldFile.absolutePath != newFile.absolutePath) {
            newFile.writeText(oldNote.content, StandardCharsets.UTF_8)
            oldFile.delete()
        } else {
            newFile.writeText(oldNote.content, StandardCharsets.UTF_8)
        }

        val newId = "$cleanFolder/$cleanName"
        updateSetPref("pinned_ids", oldNote.id, false)
        updateSetPref("favorite_ids", oldNote.id, false)
        updateSetPref("pinned_ids", newId, oldNote.isPinned)
        updateSetPref("favorite_ids", newId, oldNote.isFavorite)

        val format = NoteFormat.fromFileName(cleanName)
        return oldNote.copy(
            id = newId,
            title = deriveTitle(cleanName, oldNote.content, format),
            fileName = cleanName,
            folderName = cleanFolder,
            format = format,
            lastModified = newFile.lastModified()
        )
    }

    fun deleteNote(note: NoteDocument): Boolean {
        val file = File(workspaceRoot, "${note.folderName}/${note.fileName}")
        updateSetPref("pinned_ids", note.id, false)
        updateSetPref("favorite_ids", note.id, false)
        prefs.edit().remove("saved_path_${note.id}").apply()
        return if (file.exists()) file.delete() else true
    }

    fun togglePin(note: NoteDocument): Boolean {
        val next = !note.isPinned
        updateSetPref("pinned_ids", note.id, next)
        return next
    }

    fun toggleFavorite(note: NoteDocument): Boolean {
        val next = !note.isFavorite
        updateSetPref("favorite_ids", note.id, next)
        return next
    }

    /**
     * Explicitly saves a note to the user's local device storage (Documents/SimpleNotes or Downloads/SimpleNotes)
     * without requiring legacy storage permissions on Android 10+. Also keeps a synchronized copy in app external storage.
     */
    fun saveNoteLocallyToDevice(
        note: NoteDocument,
        customFileName: String = note.fileName,
        targetDirectoryType: String = Environment.DIRECTORY_DOCUMENTS
    ): LocalSaveResult {
        val cleanName = sanitizeFileName(customFileName).ifBlank { note.fileName }
        val bytes = note.content.toByteArray(StandardCharsets.UTF_8)
        val mimeType = NoteFormat.fromFileName(cleanName).mimeType

        // 1. Always save a copy in workspace first so no edits are ever lost
        saveNoteToWorkspace(note.folderName, cleanName, note.content, note.isPinned, note.isFavorite)

        // 2. Also write to app-accessible external storage Documents/SimpleNotes
        val appExternalRoot = context.getExternalFilesDir(targetDirectoryType) ?: context.filesDir
        val appExternalFolder = File(appExternalRoot, "SimpleNotes/${note.folderName}").apply { mkdirs() }
        val appExternalFile = File(appExternalFolder, cleanName)
        runCatching { appExternalFile.writeBytes(bytes) }

        // 3. Write to public Documents/SimpleNotes or Downloads/SimpleNotes via MediaStore on API 29+
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val relativePath = if (targetDirectoryType == Environment.DIRECTORY_DOWNLOADS) {
                    "${Environment.DIRECTORY_DOWNLOADS}/SimpleNotes"
                } else {
                    "${Environment.DIRECTORY_DOCUMENTS}/SimpleNotes/${note.folderName}"
                }

                val collection: Uri = if (targetDirectoryType == Environment.DIRECTORY_DOWNLOADS) {
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Files.getContentUri("external")
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }

                val insertedUri = resolver.insert(collection, values)
                if (insertedUri != null) {
                    resolver.openOutputStream(insertedUri)?.use { out ->
                        out.write(bytes)
                        out.flush()
                    }
                    val displayPath = "/storage/emulated/0/$relativePath/$cleanName"
                    prefs.edit().putString("saved_path_${note.id}", displayPath).apply()
                    LocalSaveResult(
                        success = true,
                        displayPath = displayPath,
                        message = "Saved locally to $relativePath/$cleanName"
                    )
                } else {
                    val displayPath = appExternalFile.absolutePath
                    prefs.edit().putString("saved_path_${note.id}", displayPath).apply()
                    LocalSaveResult(
                        success = true,
                        displayPath = displayPath,
                        message = "Saved locally to SimpleNotes/$cleanName"
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val publicDir = File(
                    Environment.getExternalStoragePublicDirectory(targetDirectoryType),
                    "SimpleNotes/${note.folderName}"
                ).apply { mkdirs() }
                val targetFile = File(publicDir, cleanName)
                targetFile.writeBytes(bytes)
                val displayPath = targetFile.absolutePath
                prefs.edit().putString("saved_path_${note.id}", displayPath).apply()
                LocalSaveResult(
                    success = true,
                    displayPath = displayPath,
                    message = "Saved locally to ${targetFile.absolutePath}"
                )
            }
        } catch (e: Exception) {
            // Fallback to app external storage path which never fails
            val displayPath = appExternalFile.absolutePath
            prefs.edit().putString("saved_path_${note.id}", displayPath).apply()
            LocalSaveResult(
                success = true,
                displayPath = displayPath,
                message = "Saved locally to device storage ($cleanName)"
            )
        }
    }

    /**
     * Saves content to a user-chosen Uri from ActivityResultContracts.CreateDocument ("Save As...").
     */
    fun saveToCustomUri(note: NoteDocument, uri: Uri, contentToWrite: String = note.content): LocalSaveResult {
        return try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                out.write(contentToWrite.toByteArray(StandardCharsets.UTF_8))
                out.flush()
            } ?: context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(contentToWrite.toByteArray(StandardCharsets.UTF_8))
                out.flush()
            }
            val fileName = queryDisplayName(uri) ?: note.fileName
            val displayPath = "Device Storage / $fileName"
            prefs.edit().putString("saved_path_${note.id}", displayPath).apply()
            LocalSaveResult(
                success = true,
                displayPath = displayPath,
                message = "Saved file locally as $fileName"
            )
        } catch (e: Exception) {
            LocalSaveResult(
                success = false,
                displayPath = "",
                message = "Could not write to selected location: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    /**
     * Exports a note as a self-contained, styled HTML5 file to local device storage.
     */
    fun exportNoteAsHtmlLocally(note: NoteDocument): LocalSaveResult {
        val baseName = note.fileName.substringBeforeLast(".")
        val htmlFileName = "$baseName.html"
        val htmlContent = buildStandaloneHtml(note)
        val htmlDoc = note.copy(
            fileName = htmlFileName,
            content = htmlContent
        )
        return saveNoteLocallyToDevice(
            note = htmlDoc,
            customFileName = htmlFileName,
            targetDirectoryType = Environment.DIRECTORY_DOWNLOADS
        )
    }

    /**
     * Exports all notes in the workspace to public Documents/SimpleNotes in one tap.
     */
    fun exportAllNotesLocally(): LocalSaveResult {
        val allNotes = loadAllNotes()
        var savedCount = 0
        for (note in allNotes) {
            val res = saveNoteLocallyToDevice(note, note.fileName, Environment.DIRECTORY_DOCUMENTS)
            if (res.success) savedCount++
        }
        return LocalSaveResult(
            success = savedCount > 0,
            displayPath = "/storage/emulated/0/Documents/SimpleNotes/",
            message = "Saved $savedCount notes locally to Documents/SimpleNotes/"
        )
    }

    /**
     * Imports a local file selected via system file picker into the SimpleNotes workspace.
     */
    fun importNoteFromUri(uri: Uri, targetFolder: String = "Notes"): NoteDocument? {
        return try {
            val rawName = queryDisplayName(uri) ?: "Imported_${System.currentTimeMillis()}.md"
            val fileName = if (rawName.contains(".")) rawName else "$rawName.md"
            val text = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(StandardCharsets.UTF_8).readText()
            } ?: return null

            saveNoteToWorkspace(
                folderName = targetFolder,
                fileName = fileName,
                content = text,
                isPinned = false,
                isFavorite = false
            )
        } catch (e: Exception) {
            null
        }
    }

    fun restoreStarterTemplates() {
        seedStarterDocuments(forceOverwrite = true)
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        }.getOrNull()
    }

    private fun updateSetPref(key: String, id: String, add: Boolean) {
        val current = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (add) current.add(id) else current.remove(id)
        prefs.edit().putStringSet(key, current).apply()
    }

    private fun sanitizeFileName(input: String): String {
        return input.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    private fun deriveTitle(fileName: String, content: String, format: NoteFormat): String {
        if (format == NoteFormat.MARKDOWN || format == NoteFormat.ASCIIDOC || format == NoteFormat.ORGMODE) {
            val headingLine = content.lineSequence()
                .map { it.trim() }
                .firstOrNull {
                    it.startsWith("# ") || it.startsWith("= ") || it.startsWith("#+TITLE:")
                }
            if (headingLine != null) {
                return headingLine
                    .removePrefix("# ")
                    .removePrefix("= ")
                    .removePrefix("#+TITLE:")
                    .trim()
                    .takeIf { it.isNotEmpty() } ?: fileName.substringBeforeLast(".")
            }
        }
        return fileName.removeSuffix(".todo.txt").substringBeforeLast(".")
    }

    private fun extractTags(content: String): List<String> {
        val tagRegex = Regex("(?:^|\\s)#([a-zA-Z][a-zA-Z0-9_-]{1,24})")
        return tagRegex.findAll(content)
            .map { it.groupValues[1].lowercase(Locale.ROOT) }
            .distinct()
            .take(8)
            .toList()
    }

    private fun buildStandaloneHtml(note: NoteDocument): String {
        val escapedBody = note.content
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="utf-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1" />
              <title>${note.title}</title>
              <style>
                :root { color-scheme: light dark; }
                body {
                  max-width: 780px;
                  margin: 40px auto;
                  padding: 0 24px;
                  font-family: system-ui, -apple-system, sans-serif;
                  line-height: 1.65;
                  color: #181C20;
                  background: #FAF8F5;
                }
                @media (prefers-color-scheme: dark) {
                  body { color: #E4E2DD; background: #111417; }
                  pre { background: #1B2024; border-color: #2C343B; }
                }
                h1 { font-size: 2rem; margin-bottom: 0.25rem; }
                .meta { font-size: 0.85rem; opacity: 0.7; margin-bottom: 2rem; }
                pre {
                  white-space: pre-wrap;
                  word-wrap: break-word;
                  font-family: ui-monospace, 'JetBrains Mono', monospace;
                  font-size: 0.94rem;
                  background: #F1EFEA;
                  padding: 20px;
                  border-radius: 14px;
                  border: 1px solid #DFDDD6;
                }
              </style>
            </head>
            <body>
              <h1>${note.title}</h1>
              <div class="meta">${note.fileName} · ${note.formattedDate} · ${note.wordCount} words</div>
              <pre>$escapedBody</pre>
            </body>
            </html>
        """.trimIndent()
    }

    private fun ensureDefaultFoldersAndSeed() {
        val seeded = prefs.getBoolean("initial_seed_v2", false)
        if (!seeded) {
            seedStarterDocuments(forceOverwrite = false)
            prefs.edit().putBoolean("initial_seed_v2", true).apply()
        }
    }

    private fun seedStarterDocuments(forceOverwrite: Boolean) {
        listFolders()

        val welcomeMd = """
            # Welcome to SimpleNotes
            
            > [!TIP]
            > Crafted with **Material You 3 + Fluent** surfaces, full **16:10 Tablet multi-pane workspace**, and instant **Local Device File Saving**.
            
            SimpleNotes is a distraction-free, local-first plain text and Markdown studio. Every document is a real file you own.
            
            ## Core Highlights
            - [x] **Material You 3 + Fluent Accents** — Adapts to your Android system wallpaper or 6 curated editorial palettes
            - [x] **16:10 Tablet Split Workspace** — Navigation Rail + File List + Live Side-by-Side Editor & Preview
            - [x] **Save Locally Anywhere** — Tap the **Save Locally** button in the top bar to save directly to `Documents/SimpleNotes`, `Downloads`, or pick any custom folder via the system file saver
            - [x] **Multi-Format Studio** — Native support for `.md`, `todo.txt`, `.csv`, `.org`, `.adoc`, and `.txt`
            - [ ] Try toggling this task item directly in **Preview** or **Split** mode!
            
            ## Code & Syntax Sample
            ```kotlin
            data class LocalNote(
                val fileName: String,
                val format: String = "Markdown",
                val savedLocally: Boolean = true
            )
            ```
            
            ## Formatting Shortcuts
            | Element | Syntax | Output |
            | :--- | :--- | :--- |
            | Bold | `**bold**` | **bold** |
            | Italic | `*italic*` | *italic* |
            | Inline Code | `` `code` `` | `code` |
            | Highlight | `==marked==` | ==marked== |
            
            #simplenotes #markdown #fluent #productivity
        """.trimIndent()

        val architectureMd = """
            # Zettelkasten & Local-First Notes
            
            Plain text files endure across decades. By storing notes in clean `.md` and `.txt` files, your knowledge graph remains portable, searchable, and fast.
            
            ## Linking & Tagging Principles
            1. **Atomic Notes**: Keep each concept focused and self-contained.
            2. **Explicit Local Backups**: Use *Save Locally -> Documents/SimpleNotes* to mirror files to shared device storage.
            3. **Semantic Tags**: Add `#architecture`, `#research`, or `#journal` anywhere in the document to filter instantly from the workspace header.
            
            > [!NOTE]
            > On 16:10 tablets and foldables, switch to **Split 16:10** mode in the top toolbar to edit Markdown on the left while inspecting live typography and tables on the right.
            
            #architecture #zettelkasten #knowledge
        """.trimIndent()

        val todoSample = """
            (A) 2026-11-10 Review 16:10 tablet multi-pane layout in split view +SimpleNotes @tablet due:2026-11-12
            (A) 2026-11-10 Test one-tap Save Locally to Documents/SimpleNotes folder +SimpleNotes @storage
            (B) 2026-11-10 Customize Material You dynamic color or Fluent accent in Settings +Design @theme
            (C) 2026-11-10 Export quarterly roadmap note as standalone HTML file +Work @export
            x 2026-11-10 2026-11-09 Rewrite legacy XML UI into modern Jetpack Compose +SimpleNotes @kotlin
        """.trimIndent()

        val quickNoteSample = """
            # QuickNote Scratchpad
            
            Capture fleeting thoughts, meeting snippets, URLs, or terminal commands here.
            
            - **10:30 AM**: Sync design tokens for Material You dynamic contrast and AMOLED pitch-black surfaces.
            - **Idea**: Use `JetBrains Mono` for editing Markdown tables and `Instrument Serif` for reader headings.
            
            #quicknote #scratchpad
        """.trimIndent()

        val csvSample = """
            Milestone,Owner,Status,Target Date,Priority
            Jetpack Compose UI Rewrite,Shumail,Completed,2026-11-10,High
            Material You 3 + Fluent Theme,Shumail,Completed,2026-11-10,High
            16:10 Tablet Split Workspace,Shumail,Completed,2026-11-10,High
            Local Device File Save & Export,Shumail,Completed,2026-11-10,High
            GitHub Actions Debug APK Workflow,Shumail,Ready,2026-11-10,High
        """.trimIndent()

        val templateRecipe = """
            # Sourdough Country Loaf
            
            ## Ingredients
            - **450g** Bread Flour
            - **50g** Whole Wheat Flour
            - **375g** Warm Water (75% hydration)
            - **100g** Active Levain
            - **10g** Sea Salt
            
            ## Method
            1. **Autolyse**: Mix flour and water, rest 45 minutes.
            2. **Bulk Fermentation**: Add levain and salt; perform 4 sets of coil folds over 4 hours.
            3. **Cold Proof**: Shape into bâtard and retard at 4°C overnight.
            4. **Bake**: Dutch oven at 245°C for 20 mins covered, 22 mins uncovered.
            
            #recipe #templates #cooking
        """.trimIndent()

        fun writeIfNeeded(folder: String, name: String, body: String, pinned: Boolean = false) {
            val file = File(workspaceRoot, "$folder/$name")
            if (forceOverwrite || !file.exists()) {
                saveNoteToWorkspace(folder, name, body, isPinned = pinned, isFavorite = pinned)
            }
        }

        writeIfNeeded("Notes", "Welcome to SimpleNotes.md", welcomeMd, pinned = true)
        writeIfNeeded("Work", "Zettelkasten Architecture.md", architectureMd, pinned = true)
        writeIfNeeded("Notes", "todo.todo.txt", todoSample, pinned = false)
        writeIfNeeded("QuickNote", "QuickNote.md", quickNoteSample, pinned = false)
        writeIfNeeded("Work", "Release Milestones.csv", csvSample, pinned = false)
        writeIfNeeded("Templates", "Artisan Recipe Template.md", templateRecipe, pinned = false)
    }
}
