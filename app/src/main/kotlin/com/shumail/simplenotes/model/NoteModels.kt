package com.shumail.simplenotes.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NoteFormat(
    val extension: String,
    val label: String,
    val shortBadge: String,
    val mimeType: String
) {
    MARKDOWN(".md", "Markdown", "MD", "text/markdown"),
    PLAINTEXT(".txt", "Plain Text", "TXT", "text/plain"),
    TODOTXT(".todo.txt", "todo.txt", "TODO", "text/plain"),
    CSV(".csv", "CSV Table", "CSV", "text/csv"),
    ORGMODE(".org", "Org-Mode", "ORG", "text/plain"),
    ASCIIDOC(".adoc", "AsciiDoc", "ADOC", "text/plain");

    companion object {
        fun fromFileName(name: String): NoteFormat {
            val lower = name.lowercase(Locale.ROOT)
            return when {
                lower.endsWith(".todo.txt") || lower == "todo.txt" -> TODOTXT
                lower.endsWith(".md") || lower.endsWith(".markdown") -> MARKDOWN
                lower.endsWith(".csv") -> CSV
                lower.endsWith(".org") -> ORGMODE
                lower.endsWith(".adoc") || lower.endsWith(".asciidoc") -> ASCIIDOC
                else -> PLAINTEXT
            }
        }
    }
}

data class NoteDocument(
    val id: String,
    val title: String,
    val fileName: String,
    val folderName: String = "Notes",
    val content: String,
    val format: NoteFormat = NoteFormat.MARKDOWN,
    val lastModified: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val localSavedPath: String? = null
) {
    val wordCount: Int
        get() = content.trim().takeIf { it.isNotEmpty() }?.split(Regex("\\s+"))?.size ?: 0

    val charCount: Int
        get() = content.length

    val lineCount: Int
        get() = if (content.isEmpty()) 1 else content.count { it == '\n' } + 1

    val readingTimeMinutes: Int
        get() = (wordCount / 200).coerceAtLeast(1)

    val previewSnippet: String
        get() = content
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("---") && !it.startsWith("# ") }
            .joinToString(" ")
            .replace(Regex("[#*`_~>\\[\\]]"), "")
            .take(160)
            .ifBlank { "Empty document" }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }
}

data class TodoItem(
    val lineIndex: Int,
    val isCompleted: Boolean,
    val priority: Char? = null,
    val completionDate: String? = null,
    val creationDate: String? = null,
    val description: String,
    val projects: List<String> = emptyList(),
    val contexts: List<String> = emptyList(),
    val dueDate: String? = null,
    val rawLine: String
) {
    fun toTodoTxtLine(): String {
        val parts = mutableListOf<String>()
        if (isCompleted) {
            parts.add("x")
            if (!completionDate.isNullOrBlank()) {
                parts.add(completionDate)
            }
        } else if (priority != null && priority in 'A'..'Z') {
            parts.add("($priority)")
        }
        if (!creationDate.isNullOrBlank()) {
            parts.add(creationDate)
        }
        parts.add(description.trim())
        projects.forEach { proj ->
            val clean = proj.removePrefix("+")
            if (!description.contains("+$clean")) parts.add("+$clean")
        }
        contexts.forEach { ctx ->
            val clean = ctx.removePrefix("@")
            if (!description.contains("@$clean")) parts.add("@$clean")
        }
        if (!dueDate.isNullOrBlank() && !description.contains("due:$dueDate")) {
            parts.add("due:$dueDate")
        }
        return parts.joinToString(" ")
    }

    companion object {
        private val PRIORITY_REGEX = Regex("^\\(([A-Z])\\)\\s+(.*)")
        private val COMPLETED_REGEX = Regex("^x\\s+(?:(\\d{4}-\\d{2}-\\d{2})\\s+)?(.*)")
        private val DATE_PREFIX_REGEX = Regex("^(\\d{4}-\\d{2}-\\d{2})\\s+(.*)")
        private val PROJECT_REGEX = Regex("(?:^|\\s)\\+([\\w-]+)")
        private val CONTEXT_REGEX = Regex("(?:^|\\s)@([\\w-]+)")
        private val DUE_REGEX = Regex("(?:^|\\s)due:(\\d{4}-\\d{2}-\\d{2})")

        fun parseLine(index: Int, rawLine: String): TodoItem? {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) return null

            var remaining = trimmed
            var isCompleted = false
            var priority: Char? = null
            var completionDate: String? = null
            var creationDate: String? = null

            val completedMatch = COMPLETED_REGEX.find(remaining)
            if (completedMatch != null) {
                isCompleted = true
                completionDate = completedMatch.groupValues[1].ifBlank { null }
                remaining = completedMatch.groupValues[2]
            } else {
                val prioMatch = PRIORITY_REGEX.find(remaining)
                if (prioMatch != null) {
                    priority = prioMatch.groupValues[1].firstOrNull()
                    remaining = prioMatch.groupValues[2]
                }
            }

            val dateMatch = DATE_PREFIX_REGEX.find(remaining)
            if (dateMatch != null) {
                creationDate = dateMatch.groupValues[1]
                remaining = dateMatch.groupValues[2]
            }

            val projects = PROJECT_REGEX.findAll(remaining).map { it.groupValues[1] }.distinct().toList()
            val contexts = CONTEXT_REGEX.findAll(remaining).map { it.groupValues[1] }.distinct().toList()
            val dueDate = DUE_REGEX.find(remaining)?.groupValues?.get(1)

            val cleanDescription = remaining
                .replace(DUE_REGEX, "")
                .trim()
                .ifBlank { trimmed }

            return TodoItem(
                lineIndex = index,
                isCompleted = isCompleted,
                priority = priority,
                completionDate = completionDate,
                creationDate = creationDate,
                description = cleanDescription,
                projects = projects,
                contexts = contexts,
                dueDate = dueDate,
                rawLine = rawLine
            )
        }

        fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}

enum class AccentPalette(
    val title: String,
    val subtitle: String,
    val previewHex: Long
) {
    MATERIAL_YOU("Material You", "Dynamic system wallpaper accent", 0xFF7AD0B3),
    FLUENT_SAGE("Obsidian Sage", "Editorial forest & warm parchment", 0xFF2E6F57),
    FLUENT_COBALT("Fluent Cobalt", "Architectural sapphire & slate", 0xFF2563EB),
    TERRACOTTA("Warm Terracotta", "Earthy clay & sunlit limestone", 0xFFC2593F),
    NORD_FROST("Nordic Frost", "Arctic slate & glacier cyan", 0xFF4C8DAE),
    AMBER_INK("Amber Ink", "Archival gold & deep espresso", 0xFFB87D14),
    ROSE_QUARTZ("Rose Quartz", "Botanical plum & warm blush", 0xFF9E4770)
}

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("AMOLED")
}

enum class EditorFont(val label: String) {
    JETBRAINS_MONO("JetBrains Mono"),
    PLUS_JAKARTA_SANS("Plus Jakarta Sans"),
    INSTRUMENT_SERIF("Instrument Serif")
}

enum class EditorViewMode(val label: String) {
    EDIT("Edit"),
    SPLIT("Split 16:10"),
    PREVIEW("Preview")
}

enum class SortOrder(val label: String) {
    MODIFIED_DESC("Recently Modified"),
    TITLE_ASC("Title (A–Z)"),
    FORMAT("Format"),
    WORD_COUNT("Word Count")
}

enum class AppSection(val label: String) {
    NOTES("Notes"),
    TODO("To-Do"),
    QUICKNOTE("QuickNote"),
    FILES("Local Files"),
    SETTINGS("Settings")
}

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentPalette: AccentPalette = AccentPalette.MATERIAL_YOU,
    val editorFont: EditorFont = EditorFont.JETBRAINS_MONO,
    val fontSizeSp: Int = 15,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = true,
    val autoSave: Boolean = true,
    val defaultFolder: String = "Notes",
    val defaultFormat: NoteFormat = NoteFormat.MARKDOWN,
    val sortOrder: SortOrder = SortOrder.MODIFIED_DESC
)

data class LocalSaveResult(
    val success: Boolean,
    val displayPath: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
