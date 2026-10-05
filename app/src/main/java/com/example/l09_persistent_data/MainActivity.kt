package com.example.l09_persistent_data

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { PersistentDataApp() } }
    }
}

enum class Demo { MENU, FILES, DATASTORE, UISTATE }

@Composable
fun PersistentDataApp() {
    var demo by remember { mutableStateOf(Demo.MENU) }
    Surface(Modifier.fillMaxSize()) {
        when (demo) {
            Demo.MENU -> DemoMenu { demo = it }
            Demo.FILES -> DemoPage("1. Internal Files and Cache", { demo = Demo.MENU }) { FileDemo() }
            Demo.DATASTORE -> DemoPage("2. Preferences DataStore", { demo = Demo.MENU }) { DataStoreDemo() }
            Demo.UISTATE -> DemoPage("3. ViewModel + UI State", { demo = Demo.MENU }) { UiStateDemo() }
        }
    }
}

@Composable
fun DemoMenu(onOpen: (Demo) -> Unit) {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Seminar 9: Persistent Data", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = { onOpen(Demo.FILES) }) { Text("1. Internal Files + Cache") }
        Button(onClick = { onOpen(Demo.DATASTORE) }) { Text("2. DataStore") }
        Button(onClick = { onOpen(Demo.UISTATE) }) { Text("3. ViewModel + CityUiState") }
    }
}

@Composable
fun DemoPage(title: String, back: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = back) { Text("← Back") }
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
fun FileDemo() {
    val context = LocalContext.current
    var result by remember { mutableStateOf("Nothing saved yet") }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = {
            context.openFileOutput("example.txt", android.content.Context.MODE_PRIVATE).use {
                it.write("Hello COS30017".toByteArray())
            }
            result = "Saved to app-specific storage"
        }) { Text("Save persistent file") }
        Button(onClick = {
            result = runCatching {
                context.openFileInput("example.txt").bufferedReader().use { it.readText() }
            }.getOrElse { "No file yet" }
        }) { Text("Read persistent file") }
        Button(onClick = {
            java.io.File(context.cacheDir, "cities_cache.txt").writeText("Melbourne,Sydney")
            result = "Temporary cache written"
        }) { Text("Write cache file") }
        Text(result)
    }
}

@Composable
fun DataStoreDemo() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val darkMode by darkModeFlow(context).collectAsStateWithLifecycle(initialValue = false)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Saved value: darkMode = $darkMode")
        Switch(checked = darkMode, onCheckedChange = { enabled ->
            scope.launch { saveDarkMode(context, enabled) }
        })
        Text("Close and reopen the app: the setting remains.")
    }
}

@Composable
fun UiStateDemo(vm: CityViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::loadCities) { Text("Load") }
            Button(onClick = vm::showError) { Text("Error") }
        }
        when (val s = state) {
            CityUiState.Loading -> CircularProgressIndicator()
            is CityUiState.Error -> Text(s.message)
            is CityUiState.Success -> LazyColumn {
                items(s.cities, key = { it.id }) { Text("${it.name}, ${it.state}", Modifier.padding(8.dp)) }
            }
        }
    }
}