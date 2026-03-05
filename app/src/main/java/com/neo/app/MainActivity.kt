package com.neo.app

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neo.app.ui.theme.TestIcareTheme

private const val HOME_ROUTE = "home"
private const val CALL_ROUTE = "call"

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      TestIcareTheme {
        AppNav()
      }
    }
  }
}

@Composable
fun AppNav(modifier: Modifier = Modifier) {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = HOME_ROUTE,
    modifier = modifier.fillMaxSize()
  ) {
    composable(HOME_ROUTE) {
      HomeScreen(
        onNavigateToCall = { navController.navigate(CALL_ROUTE) },
        modifier = Modifier.fillMaxSize()
      )
    }

    composable(CALL_ROUTE) {
      CallScreen(modifier = Modifier.fillMaxSize())
    }
  }
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
    AppNav()
  }
}