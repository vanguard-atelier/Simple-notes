package com.shumail.simplenotes.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.Html
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shumail.simplenotes.model.AppPreferences
import com.shumail.simplenotes.model.EditorViewMode
import com.shumail.simplenotes.model.NoteDocument
import com.shumail.simplenotes.ui.components.RichDocumentPreview
import com.shumail.simplenotes.ui.components.SyntaxHighlightTransformation
import com.shumail.simplenotes.ui.theme.JetBrainsMonoFamily
import com.shumail.simplenotes.ui.theme.editorFontFamilyFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentEditorPane(
    note: NoteDocument,
    preferences: AppPreferences,
    viewMode: EditorViewMode,
    isExpandedTablet: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    isFindBarVisible: Boolean,
    findQuery: String,
    replaceQuery: String,
    isSaveLocallySheetOpen: Boolean,
    folders: List<String>,
    onBack: (() -> Unit)?,
    onContentChange: (String) -> Unit,
    onViewModeChange: (EditorViewMode) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleFindBar: () -> Unit,
    onFindQueryChange: (String) -> Unit,
    onReplaceQueryChange: (String) -> Unit,
    onReplace: (Boolean) -> Unit,
    onTogglePin: () -> Unit,
    onOpenSaveLocally: () -> Unit,
    onDismissSaveLocally: () -> Unit,
    onSaveToDeviceDocuments: (String) -> Unit,
    onSaveToDeviceDownloads: (String) -> Unit,
    onSaveAsSystemPicker: (String) -> Unit,
    onExportHtmlLocally: () -> Unit,
    onRenameOrMove: (String, String) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onToggleTaskLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var textFieldValue by remember(note.id) {
        mutableStateOf(TextFieldValue(text = note.content, selection = TextRange(note.content.length)))
    }

    LaunchedEffect(note.content) {
        if (note.content != textFieldValue.text) {
            val safeCursor = textFieldValue.selection.end.coerceIn(0, note.content.length)
            textFieldValue = TextFieldValue(text = note.content, selection = TextRange(safeCursor))
        }
    }

    var showMoreMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val codeBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val syntaxTransformation = remember(primaryColor, secondaryColor, tertiaryColor, codeBg, mutedColor) {
        SyntaxHighlightTransformation(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            tertiaryColor = tertiaryColor,
            codeBackground = codeBg,
            mutedColor = mutedColor
        )
    }

    fun insertSnippet(prefix: String, suffix: String = "", placeholder: String = "") {
        val currentText = textFieldValue.text
        val start = textFieldValue.selection.start.coerceIn(0, currentText.length)
        val end = textFieldValue.selection.end.coerceIn(0, currentText.length)
        val selected = if (start != end) currentText.substring(minOf(start, end), maxOf(start, end)) else placeholder
        val replacement = "$prefix$selected$suffix"
        val newText = currentText.replaceRange(minOf(start, end), maxOf(start, end), replacement)
        val newCursor = minOf(start, end) + prefix.length + selected.length
        textFieldValue = TextFieldValue(newText, TextRange(newCursor))
        onContentChange(newText)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
    ) {
        // 1. Top Fluent Command Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (onBack != null) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("editor_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back to notes"
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showRenameDialog = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = note.format.shortBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${note.folderName} / ${note.fileName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Mode Segmented Pill (Edit | Split | Preview)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                val modes = if (isExpandedTablet) {
                                    listOf(EditorViewMode.EDIT, EditorViewMode.SPLIT, EditorViewMode.PREVIEW)
                                } else {
                                    listOf(EditorViewMode.EDIT, EditorViewMode.PREVIEW)
                                }
                                modes.forEach { mode ->
                                    val selected = viewMode == mode
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceContainerHighest
                                            )
                                            .clickable { onViewModeChange(mode) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (mode == EditorViewMode.SPLIT && !isExpandedTablet) "Split" else mode.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Undo / Redo on wide or compact
                        IconButton(
                            onClick = onUndo,
                            enabled = canUndo,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Undo,
                                contentDescription = "Undo",
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        if (isExpandedTablet) {
                            IconButton(
                                onClick = onRedo,
                                enabled = canRedo,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Redo,
                                    contentDescription = "Redo",
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        // Prominent Save Locally Button
                        FilledTonalButton(
                            onClick = onOpenSaveLocally,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("save_locally_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = "Save file locally",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMoreMenu = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("editor_more_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription = "More document actions"
                                )
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Save Locally to Device...") },
                                    leadingIcon = { Icon(Icons.Outlined.Download, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        onOpenSaveLocally()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Find & Replace") },
                                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        onToggleFindBar()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (note.isPinned) "Unpin Note" else "Pin to Top") },
                                    leadingIcon = {
                                        Icon(
                                            if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onTogglePin()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rename or Move Folder") },
                                    leadingIcon = { Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showRenameDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Duplicate Note") },
                                    leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        onDuplicate()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share Document Text") },
                                    leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, note.title)
                                            putExtra(Intent.EXTRA_TEXT, note.content)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share ${note.fileName}"))
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Delete Note", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.DeleteOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }

                // Collapsible Find & Replace Bar
                AnimatedVisibility(visible = isFindBarVisible) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = findQuery,
                                onValueChange = onFindQueryChange,
                                placeholder = { Text("Find in note...", style = MaterialTheme.typography.bodySmall) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = replaceQuery,
                                onValueChange = onReplaceQueryChange,
                                placeholder = { Text("Replace...", style = MaterialTheme.typography.bodySmall) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { onReplace(false) }) {
                                Text("Replace")
                            }
                            TextButton(onClick = { onReplace(true) }) {
                                Text("All")
                            }
                            IconButton(onClick = onToggleFindBar) {
                                Icon(Icons.Outlined.Close, contentDescription = "Close find bar")
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            }
        }

        // 2. Main Editor / 16:10 Split / Preview Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (viewMode) {
                EditorViewMode.EDIT -> {
                    RawSyntaxEditorSurface(
                        textFieldValue = textFieldValue,
                        onTextFieldValueChange = {
                            textFieldValue = it
                            onContentChange(it.text)
                        },
                        preferences = preferences,
                        syntaxTransformation = syntaxTransformation,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                EditorViewMode.PREVIEW -> {
                    RichDocumentPreview(
                        content = textFieldValue.text,
                        format = note.format,
                        fontSizeSp = preferences.fontSizeSp,
                        onToggleTaskLine = onToggleTaskLine,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                EditorViewMode.SPLIT -> {
                    // 16:10 Tablet & Landscape Side-by-Side Split View
                    Row(modifier = Modifier.fillMaxSize()) {
                        RawSyntaxEditorSurface(
                            textFieldValue = textFieldValue,
                            onTextFieldValueChange = {
                                textFieldValue = it
                                onContentChange(it.text)
                            },
                            preferences = preferences,
                            syntaxTransformation = syntaxTransformation,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LIVE RENDERED PREVIEW",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${note.wordCount} words · ${note.readingTimeMinutes} min read",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            RichDocumentPreview(
                                content = textFieldValue.text,
                                format = note.format,
                                fontSizeSp = preferences.fontSizeSp,
                                onToggleTaskLine = onToggleTaskLine,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // 3. Bottom Formatting Action Bar & Fluent Status Footer
        if (viewMode != EditorViewMode.PREVIEW) {
            FormattingActionToolbar(
                onHeading1 = { insertSnippet("\n# ", "", "Heading 1") },
                onHeading2 = { insertSnippet("\n## ", "", "Heading 2") },
                onBold = { insertSnippet("**", "**", "bold") },
                onItalic = { insertSnippet("*", "*", "italic") },
                onStrike = { insertSnippet("~~", "~~", "text") },
                onTask = { insertSnippet("\n- [ ] ", "", "Task item") },
                onBullet = { insertSnippet("\n- ", "", "List item") },
                onQuote = { insertSnippet("\n> ", "", "Quote") },
                onCodeBlock = { insertSnippet("\n```kotlin\n", "\n```\n", "// code") },
                onTable = {
                    insertSnippet(
                        "\n| Column A | Column B | Status |\n| :--- | :--- | :--- |\n| Item 1 | Value | Done |\n"
                    )
                },
                onLink = { insertSnippet("[", "](https://)", "Link title") },
                onTimestamp = {
                    val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                    insertSnippet(now)
                }
            )
        }

        // Fluent Status Footer
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${note.wordCount} words",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${note.charCount} chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${note.lineCount} lines",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOpenSaveLocally() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = if (note.localSavedPath != null) "Saved locally on device" else "Workspace auto-saved · Tap to save locally",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // Save Locally Dialog
    if (isSaveLocallySheetOpen) {
        SaveLocallyDialog(
            note = note,
            onDismiss = onDismissSaveLocally,
            onSaveToDocuments = onSaveToDeviceDocuments,
            onSaveToDownloads = onSaveToDeviceDownloads,
            onSaveAsSystemPicker = onSaveAsSystemPicker,
            onExportHtml = onExportHtmlLocally
        )
    }

    // Rename or Move Dialog
    if (showRenameDialog) {
        RenameNoteDialog(
            note = note,
            folders = folders,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName, newFolder ->
                showRenameDialog = false
                onRenameOrMove(newName, newFolder)
            }
        )
    }
}

@Composable
private fun RawSyntaxEditorSurface(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    preferences: AppPreferences,
    syntaxTransformation: SyntaxHighlightTransformation,
    modifier: Modifier = Modifier
) {
    val verticalScroll = rememberScrollState()
    val lineCount = remember(textFieldValue.text) {
        if (textFieldValue.text.isEmpty()) 1 else textFieldValue.text.count { it == '\n' } + 1
    }
    val fontFamily = editorFontFamilyFor(preferences.editorFont)
    val lineHeightSp = (preferences.fontSizeSp * 1.55f).sp

    Row(
        modifier = modifier
            .verticalScroll(verticalScroll)
            .padding(vertical = 14.dp)
    ) {
        if (preferences.showLineNumbers) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .padding(start = 12.dp, end = 10.dp)
                    .width(32.dp)
            ) {
                for (lineNum in 1..lineCount) {
                    Text(
                        text = lineNum.toString(),
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = (preferences.fontSizeSp - 2).coerceAtLeast(10).sp,
                            lineHeight = lineHeightSp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                    )
                }
            }
            VerticalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                modifier = Modifier.padding(end = 12.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(18.dp))
        }

        BasicTextField(
            value = textFieldValue,
            onValueChange = onTextFieldValueChange,
            textStyle = TextStyle(
                fontFamily = fontFamily,
                fontSize = preferences.fontSizeSp.sp,
                lineHeight = lineHeightSp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            visualTransformation = syntaxTransformation,
            modifier = Modifier
                .weight(1f)
                .padding(end = 18.dp)
                .fillMaxHeight()
                .testTag("note_editor_text_field"),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = "Start writing in Markdown, todo.txt, or plain text...",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = preferences.fontSizeSp.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun FormattingActionToolbar(
    onHeading1: () -> Unit,
    onHeading2: () -> Unit,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onStrike: () -> Unit,
    onTask: () -> Unit,
    onBullet: () -> Unit,
    onQuote: () -> Unit,
    onCodeBlock: () -> Unit,
    onTable: () -> Unit,
    onLink: () -> Unit,
    onTimestamp: () -> Unit
) {
    val scroll = rememberScrollState()
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormatActionChip(icon = Icons.Outlined.Title, label = "H1", onClick = onHeading1)
                FormatActionChip(icon = Icons.Outlined.Title, label = "H2", onClick = onHeading2)
                FormatActionIcon(icon = Icons.Outlined.FormatBold, contentDescription = "Bold", onClick = onBold)
                FormatActionIcon(icon = Icons.Outlined.FormatItalic, contentDescription = "Italic", onClick = onItalic)
                FormatActionIcon(
                    icon = Icons.Outlined.FormatStrikethrough,
                    contentDescription = "Strikethrough",
                    onClick = onStrike
                )
                FormatActionIcon(icon = Icons.Outlined.CheckBox, contentDescription = "Task Checkbox", onClick = onTask)
                FormatActionIcon(
                    icon = Icons.Outlined.FormatListBulleted,
                    contentDescription = "Bullet List",
                    onClick = onBullet
                )
                FormatActionIcon(icon = Icons.Outlined.FormatQuote, contentDescription = "Quote", onClick = onQuote)
                FormatActionIcon(icon = Icons.Outlined.Code, contentDescription = "Code Block", onClick = onCodeBlock)
                FormatActionIcon(icon = Icons.Outlined.TableChart, contentDescription = "Table", onClick = onTable)
                FormatActionIcon(icon = Icons.Outlined.Link, contentDescription = "Insert Link", onClick = onLink)
                FormatActionIcon(
                    icon = Icons.Outlined.Schedule,
                    contentDescription = "Insert Date/Time",
                    onClick = onTimestamp
                )
            }
        }
    }
}

@Composable
private fun FormatActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FormatActionIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SaveLocallyDialog(
    note: NoteDocument,
    onDismiss: () -> Unit,
    onSaveToDocuments: (String) -> Unit,
    onSaveToDownloads: (String) -> Unit,
    onSaveAsSystemPicker: (String) -> Unit,
    onExportHtml: () -> Unit
) {
    var fileNameInput by remember(note.fileName) { mutableStateOf(note.fileName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Save File Locally",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Write directly to device storage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fileNameInput,
                    onValueChange = { fileNameInput = it },
                    label = { Text("File Name & Extension") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("local_save_filename_input")
                )

                if (note.localSavedPath != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Last saved: ${note.localSavedPath}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Option 1: Direct save to Documents/SimpleNotes
                Button(
                    onClick = { onSaveToDocuments(fileNameInput) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_to_documents_button")
                ) {
                    Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Documents/SimpleNotes")
                }

                // Option 2: Direct save to Downloads/SimpleNotes
                FilledTonalButton(
                    onClick = { onSaveToDownloads(fileNameInput) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_to_downloads_button")
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Device Downloads")
                }

                // Option 3: Pick custom folder via Android System File Picker
                OutlinedButton(
                    onClick = { onSaveAsSystemPicker(fileNameInput) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_as_picker_button")
                ) {
                    Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save File As... (Pick Folder)")
                }

                // Option 4: Export as standalone HTML
                OutlinedButton(
                    onClick = onExportHtml,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_html_button")
                ) {
                    Icon(Icons.Outlined.Html, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export as Standalone HTML (.html)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun RenameNoteDialog(
    note: NoteDocument,
    folders: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var nameInput by remember(note.fileName) { mutableStateOf(note.fileName) }
    var selectedFolder by remember(note.folderName) { mutableStateOf(note.folderName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename or Move File") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Folder",
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
                        val isSelected = folder == selectedFolder
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedFolder = folder }
                        ) {
                            Text(
                                text = folder,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nameInput, selectedFolder) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
