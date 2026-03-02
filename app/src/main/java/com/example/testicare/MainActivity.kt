package com.example.testicare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.testicare.ui.theme.TestIcareTheme

data object HomeRoute
data object CallRoute

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      TestIcareTheme {
        AppNav3()
      }
    }
  }
}

@Composable
fun AppNav3(modifier: Modifier = Modifier) {
  val backStack = remember { mutableStateListOf<Any>(HomeRoute) }

  NavDisplay(
    backStack = backStack,
    modifier = modifier.fillMaxSize(),
    onBack = { backStack.removeLastOrNull() },
    entryProvider = { key ->
      when (key) {
        is HomeRoute -> NavEntry(key) {
          HomeScreen(
            onNavigateToCall = { backStack.add(CallRoute) },
            modifier = Modifier.fillMaxSize()
          )
        }

        is CallRoute -> NavEntry(key) {
          CallScreen(modifier = Modifier.fillMaxSize())
        }

        else -> NavEntry(Unit) { Text("Unknown route") }
      }
    }
  )
}

@Composable
fun HomeScreen(
  onNavigateToCall: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold {
    Column(
      modifier = modifier.padding(it).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text("Home")
      Button(
        onClick = onNavigateToCall,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Go to Call Screen")
      }
    }
  }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  TestIcareTheme {
    AppNav3()
  }
}