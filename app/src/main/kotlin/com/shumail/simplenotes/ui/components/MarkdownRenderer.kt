package com.shumail.simplenotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shumail.simplenotes.model.NoteFormat
import com.shumail.simplenotes.ui.theme.InstrumentSerifFamily
import com.shumail.simplenotes.ui.theme.JetBrainsMonoFamily

/**
 * Live syntax highlighting VisualTransformation for the Markdown / Todo.txt / Org-mode editor.
 * Preserves exact 1-to-1 character length with OffsetMapping.Identity.
 */
class SyntaxHighlightTransformation(
    private val primaryColor: Color,
    private val secondaryColor: Color,
    private val tertiaryColor: Color,
    private val codeBackground: Color,
    private val mutedColor: Color
) : VisualTransformation {

    private val headingRegex = Regex("(?m)^(#{1,6}\\s+.*|=+\\s+.*|\\*+\\s+.*)$")
    private val boldRegex = Regex("\\*\\*[^*]+\\*\\*")
    private val italicRegex = Regex("(?<!\\*)\\*[^*\n]+\\*(?!\\*)")
    private val inlineCodeRegex = Regex("`[^`\n]+`")
    private val tagRegex = Regex("(?<=^|\\s)(#[a-zA-Z0-9_-]+|\\+[a-zA-Z0-9_-]+|@[a-zA-Z0-9_-]+)")
    private val priorityRegex = Regex("(?m)^\\([A-Z]\\)")
    private val quoteRegex = Regex("(?m)^>.*$")
    private val linkRegex = Regex("\\[[^\\]\n]+]\\([^)\n]+\\)")

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val styled = buildAnnotatedString {
            append(raw)

            headingRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }
            boldRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor),
                    match.range.first,
                    match.range.last + 1
                )
            }
            italicRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(fontStyle = FontStyle.Italic, color = secondaryColor),
                    match.range.first,
                    match.range.last + 1
                )
            }
            inlineCodeRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(
                        fontFamily = JetBrainsMonoFamily,
                        color = tertiaryColor,
                        background = codeBackground
                    ),
                    match.range.first,
                    match.range.last + 1
                )
            }
            tagRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(color = tertiaryColor, fontWeight = FontWeight.SemiBold),
                    match.range.first,
                    match.range.last + 1
                )
            }
            priorityRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }
            quoteRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(color = mutedColor, fontStyle = FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
            }
            linkRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(color = primaryColor, textDecoration = TextDecoration.Underline),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

private sealed class ParsedBlock {
    data class Heading(val level: Int, val text: String) : ParsedBlock()
    data class Paragraph(val text: String) : ParsedBlock()
    data class TaskItem(val lineIndex: Int, val checked: Boolean, val text: String) : ParsedBlock()
    data class BulletItem(val text: String, val numberPrefix: String? = null) : ParsedBlock()
    data class CodeBlock(val language: String, val code: String) : ParsedBlock()
    data class CalloutBlock(val kind: String, val lines: List<String>) : ParsedBlock()
    data class TableBlock(val headers: List<String>, val rows: List<List<String>>) : ParsedBlock()
    data object Divider : ParsedBlock()
}

@Composable
fun RichDocumentPreview(
    content: String,
    format: NoteFormat,
    fontSizeSp: Int,
    onToggleTaskLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    if (format == NoteFormat.CSV) {
        Column(
            modifier = modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            CsvDataTablePreview(csvContent = content)
        }
        return
    }

    val blocks = remember(content, format) { parseDocumentBlocks(content) }
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val codeBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlightBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is ParsedBlock.Heading -> {
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = InstrumentSerifFamily,
                            fontSize = (fontSizeSp + 14).sp,
                            lineHeight = (fontSizeSp + 20).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        2 -> MaterialTheme.typography.headlineSmall.copy(
                            fontSize = (fontSizeSp + 6).sp,
                            lineHeight = (fontSizeSp + 12).sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        else -> MaterialTheme.typography.titleMedium.copy(
                            fontSize = (fontSizeSp + 2).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(modifier = Modifier.padding(top = if (block.level == 1) 6.dp else 4.dp)) {
                        Text(
                            text = renderInlineMarkup(block.text, primary, tertiary, codeBg, highlightBg),
                            style = style
                        )
                        if (block.level == 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                        }
                    }
                }

                is ParsedBlock.Paragraph -> {
                    Text(
                        text = renderInlineMarkup(block.text, primary, tertiary, codeBg, highlightBg),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.55f).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                is ParsedBlock.TaskItem -> {
                    Surface(
                        color = if (block.checked) {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleTaskLine(block.lineIndex) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (block.checked) {
                                    Icons.Filled.CheckCircle
                                } else {
                                    Icons.Outlined.RadioButtonUnchecked
                                },
                                contentDescription = if (block.checked) "Completed task" else "Incomplete task",
                                tint = if (block.checked) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = renderInlineMarkup(block.text, primary, tertiary, codeBg, highlightBg),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = fontSizeSp.sp,
                                    textDecoration = if (block.checked) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (block.checked) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }

                is ParsedBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = block.numberPrefix ?: "•",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = fontSizeSp.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = renderInlineMarkup(block.text, primary, tertiary, codeBg, highlightBg),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp * 1.5f).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                is ParsedBlock.CodeBlock -> {
                    val clipboard = LocalClipboardManager.current
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = block.language.ifBlank { "code" }.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { clipboard.setText(AnnotatedString(block.code)) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy code",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = block.code,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = (fontSizeSp - 1).coerceAtLeast(12).sp,
                                        lineHeight = (fontSizeSp + 5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                is ParsedBlock.CalloutBlock -> {
                    val isWarning = block.kind.contains("WARN", ignoreCase = true)
                    val isTip = block.kind.contains("TIP", ignoreCase = true)
                    val accent = when {
                        isWarning -> MaterialTheme.colorScheme.error
                        isTip -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                    Surface(
                        color = accent.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = when {
                                    isWarning -> Icons.Outlined.WarningAmber
                                    isTip -> Icons.Outlined.Lightbulb
                                    else -> Icons.Outlined.Info
                                },
                                contentDescription = block.kind,
                                tint = accent,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (block.kind.isNotBlank() && block.kind != "QUOTE") {
                                    Text(
                                        text = block.kind,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = accent
                                    )
                                }
                                block.lines.forEach { line ->
                                    Text(
                                        text = renderInlineMarkup(line, primary, tertiary, codeBg, highlightBg),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = fontSizeSp.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                is ParsedBlock.TableBlock -> {
                    FluentTableComponent(headers = block.headers, rows = block.rows)
                }

                ParsedBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CsvDataTablePreview(csvContent: String) {
    val lines = csvContent.lines().map { it.trim() }.filter { it.isNotEmpty() }
    if (lines.isEmpty()) {
        Text("Empty CSV file", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val headers = lines.first().split(",").map { it.trim() }
    val rows = lines.drop(1).map { line -> line.split(",").map { it.trim() } }
    FluentTableComponent(headers = headers, rows = rows)
}

@Composable
private fun FluentTableComponent(
    headers: List<String>,
    rows: List<List<String>>
) {
    val hScroll = rememberScrollState()
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.horizontalScroll(hScroll)) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
                    .padding(vertical = 10.dp, horizontal = 12.dp)
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .widthIn(min = 120.dp, max = 220.dp)
                            .padding(horizontal = 8.dp)
                    )
                }
            }
            rows.forEachIndexed { rowIdx, row ->
                val bg = if (rowIdx % 2 == 0) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
                }
                Row(
                    modifier = Modifier
                        .background(bg)
                        .padding(vertical = 9.dp, horizontal = 12.dp)
                ) {
                    headers.indices.forEach { colIdx ->
                        val cell = row.getOrNull(colIdx).orEmpty()
                        Text(
                            text = cell,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .widthIn(min = 120.dp, max = 220.dp)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun parseDocumentBlocks(content: String): List<ParsedBlock> {
    val lines = content.lines()
    val result = mutableListOf<ParsedBlock>()
    var i = 0

    while (i < lines.size) {
        val rawLine = lines[i]
        val trimmed = rawLine.trim()

        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // Fenced code block
        if (trimmed.startsWith("```")) {
            val lang = trimmed.removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            i++ // skip closing fence
            result.add(ParsedBlock.CodeBlock(lang, codeLines.joinToString("\n")))
            continue
        }

        // Horizontal rule
        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            result.add(ParsedBlock.Divider)
            i++
            continue
        }

        // Markdown / AsciiDoc / Org heading
        if (trimmed.startsWith("#")) {
            val hashes = trimmed.takeWhile { it == '#' }.length
            if (hashes in 1..6 && trimmed.getOrNull(hashes) == ' ') {
                result.add(ParsedBlock.Heading(hashes, trimmed.substring(hashes + 1).trim()))
                i++
                continue
            }
        }
        if (trimmed.startsWith("= ") || trimmed.startsWith("== ")) {
            val eqs = trimmed.takeWhile { it == '=' }.length
            result.add(ParsedBlock.Heading(eqs.coerceIn(1, 4), trimmed.substring(eqs).trim()))
            i++
            continue
        }
        if (trimmed.startsWith("#+TITLE:")) {
            result.add(ParsedBlock.Heading(1, trimmed.removePrefix("#+TITLE:").trim()))
            i++
            continue
        }

        // Markdown Table
        if (trimmed.startsWith("|") && trimmed.endsWith("|") && i + 1 < lines.size && lines[i + 1].trim().contains("---")) {
            val headers = trimmed.trim('|').split("|").map { it.trim() }
            i += 2 // skip header and separator
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].trim().startsWith("|")) {
                val cells = lines[i].trim().trim('|').split("|").map { it.trim() }
                rows.add(cells)
                i++
            }
            result.add(ParsedBlock.TableBlock(headers, rows))
            continue
        }

        // Quote / Admonition Callout
        if (trimmed.startsWith(">")) {
            val quoteLines = mutableListOf<String>()
            var kind = "QUOTE"
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                val stripped = lines[i].trim().removePrefix(">").trim()
                if (stripped.startsWith("[!") && stripped.contains("]")) {
                    kind = stripped.substringAfter("[!").substringBefore("]").uppercase()
                } else if (stripped.isNotEmpty()) {
                    quoteLines.add(stripped)
                }
                i++
            }
            result.add(ParsedBlock.CalloutBlock(kind, quoteLines))
            continue
        }

        // Task list item
        if (trimmed.startsWith("- [ ] ") || trimmed.startsWith("* [ ] ")) {
            result.add(ParsedBlock.TaskItem(i, false, trimmed.substring(6)))
            i++
            continue
        }
        if (trimmed.startsWith("- [x] ", ignoreCase = true) || trimmed.startsWith("* [x] ", ignoreCase = true)) {
            result.add(ParsedBlock.TaskItem(i, true, trimmed.substring(6)))
            i++
            continue
        }

        // Bullet list item
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            result.add(ParsedBlock.BulletItem(trimmed.substring(2)))
            i++
            continue
        }

        // Numbered list item
        val numMatch = Regex("^(\\d+\\.)\\s+(.*)").find(trimmed)
        if (numMatch != null) {
            result.add(ParsedBlock.BulletItem(numMatch.groupValues[2], numMatch.groupValues[1]))
            i++
            continue
        }

        result.add(ParsedBlock.Paragraph(trimmed))
        i++
    }
    return result
}

private fun renderInlineMarkup(
    input: String,
    primaryColor: Color,
    tertiaryColor: Color,
    codeBg: Color,
    highlightBg: Color
): AnnotatedString = buildAnnotatedString {
    var idx = 0
    while (idx < input.length) {
        when {
            input.startsWith("**", idx) -> {
                val end = input.indexOf("**", idx + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(input.substring(idx + 2, end))
                    pop()
                    idx = end + 2
                } else {
                    append(input[idx])
                    idx++
                }
            }
            input.startsWith("==", idx) -> {
                val end = input.indexOf("==", idx + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(background = highlightBg, fontWeight = FontWeight.Medium))
                    append(" ${input.substring(idx + 2, end)} ")
                    pop()
                    idx = end + 2
                } else {
                    append(input[idx])
                    idx++
                }
            }
            input.startsWith("~~", idx) -> {
                val end = input.indexOf("~~", idx + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    append(input.substring(idx + 2, end))
                    pop()
                    idx = end + 2
                } else {
                    append(input[idx])
                    idx++
                }
            }
            input[idx] == '`' -> {
                val end = input.indexOf('`', idx + 1)
                if (end != -1) {
                    pushStyle(
                        SpanStyle(
                            fontFamily = JetBrainsMonoFamily,
                            color = tertiaryColor,
                            background = codeBg
                        )
                    )
                    append(" ${input.substring(idx + 1, end)} ")
                    pop()
                    idx = end + 1
                } else {
                    append(input[idx])
                    idx++
                }
            }
            input[idx] == '*' -> {
                val end = input.indexOf('*', idx + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(input.substring(idx + 1, end))
                    pop()
                    idx = end + 1
                } else {
                    append(input[idx])
                    idx++
                }
            }
            input[idx] == '#' && (idx == 0 || input[idx - 1].isWhitespace()) -> {
                val tagEnd = input.indexOfFirst(idx + 1) { it.isWhitespace() }
                val actualEnd = if (tagEnd == -1) input.length else tagEnd
                if (actualEnd > idx + 1) {
                    pushStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold))
                    append(input.substring(idx, actualEnd))
                    pop()
                    idx = actualEnd
                } else {
                    append(input[idx])
                    idx++
                }
            }
            else -> {
                append(input[idx])
                idx++
            }
        }
    }
}

private inline fun String.indexOfFirst(startIndex: Int, predicate: (Char) -> Boolean): Int {
    for (i in startIndex until length) {
        if (predicate(this[i])) return i
    }
    return -1
}
