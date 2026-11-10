package com.shumail.simplenotes.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shumail.simplenotes.model.TodoItem
import com.shumail.simplenotes.ui.theme.InstrumentSerifFamily
import com.shumail.simplenotes.ui.theme.JetBrainsMonoFamily

@Composable
fun TodoTxtScreen(
    todoItems: List<TodoItem>,
    rawTodoContent: String,
    onAddTodo: (String, Char?, String?, String?, String?) -> Unit,
    onToggleTodo: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    onUpdateRawContent: (String) -> Unit,
    onSaveTodoLocally: () -> Unit,
    modifier: Modifier = Modifier
) {
    var taskText by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf<Char?>('A') }
    var projectInput by remember { mutableStateOf("SimpleNotes") }
    var contextInput by remember { mutableStateOf("today") }
    var selectedProjectFilter by remember { mutableStateOf<String?>(null) }
    var showRawEditor by remember { mutableStateOf(false) }

    val allProjects = remember(todoItems) {
        todoItems.flatMap { it.projects }.distinct().sorted()
    }
    val filteredItems = remember(todoItems, selectedProjectFilter) {
        todoItems
            .filter { item ->
                selectedProjectFilter == null || item.projects.any { it.equals(selectedProjectFilter, ignoreCase = true) }
            }
            .sortedWith(
                compareBy<TodoItem> { it.isCompleted }
                    .thenBy { it.priority ?: 'Z' }
            )
    }

    val completedCount = todoItems.count { it.isCompleted }
    val totalCount = todoItems.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Row with Save Locally & Raw Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "todo.txt Tasks",
                    style = MaterialTheme.typography.headlineLarge.copy(fontFamily = InstrumentSerifFamily),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$completedCount of $totalCount completed · Plaintext portable format",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { showRawEditor = !showRawEditor },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (showRawEditor) Icons.Outlined.ViewList else Icons.Outlined.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showRawEditor) "Cards" else "Raw .txt")
                }

                Button(
                    onClick = onSaveTodoLocally,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_todo_locally_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Locally")
                }
            }
        }

        // Progress Card
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                // Quick-Add Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = taskText,
                        onValueChange = { taskText = it },
                        placeholder = { Text("Add a new task (e.g., Ship 16:10 tablet layout)...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("todo_input_field")
                    )
                    Button(
                        onClick = {
                            if (taskText.isNotBlank()) {
                                onAddTodo(taskText, selectedPriority, projectInput, contextInput, null)
                                taskText = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("todo_add_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add task")
                    }
                }

                // Priority & Tag selectors for new task
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Priority:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    listOf('A', 'B', 'C', null).forEach { prio ->
                        val active = selectedPriority == prio
                        Surface(
                            color = if (active) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPriority = prio }
                        ) {
                            Text(
                                text = prio?.let { "($it)" } ?: "None",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+$projectInput  @$contextInput",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (showRawEditor) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            ) {
                OutlinedTextField(
                    value = rawTodoContent,
                    onValueChange = onUpdateRawContent,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
        } else {
            // Project Filter Chips
            if (allProjects.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedProjectFilter == null,
                        onClick = { selectedProjectFilter = null },
                        label = { Text("All Tasks (${todoItems.size})") }
                    )
                    allProjects.forEach { proj ->
                        FilterChip(
                            selected = selectedProjectFilter == proj,
                            onClick = {
                                selectedProjectFilter = if (selectedProjectFilter == proj) null else proj
                            },
                            label = { Text("+$proj") }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredItems, key = { "${it.lineIndex}_${it.rawLine}" }) { item ->
                    TodoItemCard(
                        item = item,
                        onToggle = { onToggleTodo(item) },
                        onDelete = { onDeleteTodo(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodoItemCard(
    item: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val prioColor = when (item.priority) {
        'A' -> MaterialTheme.colorScheme.error
        'B' -> MaterialTheme.colorScheme.primary
        'C' -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        color = if (item.isCompleted) MaterialTheme.colorScheme.surfaceContainerLow
        else MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (item.isCompleted) "Completed" else "Mark complete",
                tint = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            if (item.priority != null && !item.isCompleted) {
                Surface(
                    color = prioColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "(${item.priority})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = prioColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                if (item.projects.isNotEmpty() || item.contexts.isNotEmpty() || item.dueDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.projects.forEach { p ->
                            Text(
                                text = "+$p",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item.contexts.forEach { c ->
                            Text(
                                text = "@$c",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        item.dueDate?.let { d ->
                            Text(
                                text = "due:$d",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
