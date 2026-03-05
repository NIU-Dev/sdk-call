package com.neo.app

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.neo.sdkcall.NeoSdkCall

@Composable
fun CallScreen(modifier: Modifier = Modifier) {
  var name by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var callStatus by remember { mutableStateOf("Ready") }
  val context = LocalContext.current
  val currentActivity = context.findComponentActivity()

  LaunchedEffect(Unit) {
    currentActivity?.let {
      PermissionUtil.checkAndRequestPermissions(context, it)
    }
  }

//  DisposableEffect(Unit) {
//    NeoSdkCall.setEventListener(object : NeoCallEventListener {
//      override fun onCallStateChange(callState: CallState) {
//        coroutineScope.launch {
//          callStatus = "State: $callState"
//        }
//      }
//
//      override fun onError(code: Int, message: String) {
//        coroutineScope.launch {
//          callStatus = "Error $code: $message"
//        }
//      }
//    })
//
//    onDispose {}
//  }


  Scaffold {
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(it)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "Call Screen",
        style = MaterialTheme.typography.headlineSmall
      )

      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Your Name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )

      OutlinedTextField(
        value = phoneNumber,
        onValueChange = { phoneNumber = it },
        label = { Text("Your Phone Number") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )

      Button(
        onClick = {
          if (currentActivity == null) {
            callStatus = "Error: Activity tidak tersedia"
            return@Button
          }

          if (!PermissionUtil.checkAndRequestPermissions(context, currentActivity)) {
            callStatus = "Permission belum diberikan"
            return@Button
          }

          val callerName = name.ifBlank { "Unknown" }
          NeoSdkCall.makeCallSip(
            activity = currentActivity,
            callerId = phoneNumber.ifBlank { "08123123" },
            callerName = callerName,
            callerAvatar = "",
            checkSum = "dummy-checksum",
            metaData = emptyMap(),
            destination = "1500738",
            destinationName = "destination-1500738",
            destinationAvatar = "",
          )

          callStatus = "Caller $callerName ($phoneNumber)"
        },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Call")
      }

      Text(
        text = callStatus,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }

}

private tailrec fun Context.findComponentActivity(): ComponentActivity? {
  return when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
  }
}
