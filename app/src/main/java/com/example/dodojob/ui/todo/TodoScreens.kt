package com.example.dodojob.ui.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import io.github.jan.supabase.postgrest.from
import com.example.dodojob.data.supabase.LocalSupabase

@Serializable
data class Todo(val id: Long, val title: String, val is_done: Boolean = false)

@Composable
fun TodoListScreen() {
    val supabase = LocalSupabase.current

    var items by remember { mutableStateOf<List<Todo>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        runCatching {
            val res = supabase.from("todos").select()   // PostgrestResult
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromJsonElement(
                ListSerializer(Todo.serializer()),
                Json.parseToJsonElement(res.data)
            )
        }.onSuccess { items = it }
            .onFailure { error = it.message }
        loading = false
    }

    Box(
        Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> Text("에러: $error")
            items.isEmpty() -> Text("할 일이 없습니다.")
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { todo ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(todo.title)
                        if (todo.is_done) Text("완료됨")
                    }
                }
            }
        }
    }
}



@Composable
fun TodoScreen(navController: androidx.navigation.NavController) = TodoListScreen()

@Composable
fun RealtimeTodoScreen(navController: androidx.navigation.NavController) = TodoListScreen()
