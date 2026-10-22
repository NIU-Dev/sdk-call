package com.neo.lib_call.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neo.lib_call.api.CallSdk
import com.neo.lib_call.core.TimerManager
import com.neo.lib_call.model.CallRequest
import com.neo.lib_call.model.CallState
import com.neo.lib_call.model.ReleaseExtensionState
import com.neo.lib_call.model.SpeakerOut
import com.neo.lib_call.util.Colors
import com.neo.lib_call.util.IntentKeys
import com.neo.lib_call.util.MetadataConverter
import java.io.Serializable

internal class CallActivity : ComponentActivity() {
  private val viewModel: CallViewModel by viewModels {
    CallViewModel.Factory(
      parseRequest(intent),
      TimerManager(),
      intent.getBooleanExtra(IntentKeys.EXTRA_INCOMING_CALL, false),
    )
  }

  private var callIsConnected = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val state by viewModel.uiState.collectAsStateWithLifecycle()
      val showLoading = state.callState == CallState.Initializing ||
        state.releaseExtensionState == ReleaseExtensionState.Loading
      val permissions = remember { requiredPermissions() }
      val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
      ) { grantResults ->
        val allGranted = permissions.all { grantResults[it] == true }
        if (allGranted) {
          if (viewModel.isIncomingCall) viewModel.answerIncomingCall() else viewModel.beginCall()
        } else {
          viewModel.setFatalError("Permission audio tidak lengkap. Aplikasi akan ditutup.")
          viewModel.releaseExtention()
        }
      }

      LaunchedEffect(Unit) {
        val alreadyGranted = permissions.all { permission ->
          ContextCompat.checkSelfPermission(
            this@CallActivity,
            permission
          ) == PackageManager.PERMISSION_GRANTED
        }
        if (alreadyGranted) {
          if (viewModel.isIncomingCall) viewModel.answerIncomingCall() else viewModel.beginCall()
        } else {
          permissionLauncher.launch(permissions)
        }
      }

      when (state.callState) {
        CallState.Connected -> {
          callIsConnected = true
        }

        else -> Unit
      }

      LaunchedEffect(state.releaseExtensionState) {
        if (state.releaseExtensionState in listOf(
            ReleaseExtensionState.Success,
            ReleaseExtensionState.Failed
          )
        ) {
          finish()
        }
      }

      if (showLoading) {
        Loading("Loading..") { }
      }

      SetSystemBarAppearance(true)

      MaterialTheme {
        CallScreen(
          state = state,
          onEndCallClick = {
            viewModel.endCall()
            val data = Intent().apply {
              putExtra(CallSdk.IS_CALL_CONNECT, callIsConnected)
              if (state.callDetailResult.isNotBlank()) {
                putExtra(CallSdk.CALL_DETAIL_RESULT, state.callDetailResult)
              }
            }
            setResult(RESULT_OK, data)
            viewModel.releaseExtention()
          },
          isMicMuted = state.isMicMuted,
          speakerOutput = state.speakerOutput,
          onMuteClick = { viewModel.toggleMute() },
          onSpeakerOutputSelected = { output -> viewModel.selectSpeakerOutput(output) },
          onNumpadClick = { digit -> viewModel.sendDtmf(digit) },
          onError = {
            viewModel.setFatalError("")
            viewModel.releaseExtention()
          }
        )
      }
    }
  }

  companion object {
    fun createIntent(context: Context, request: CallRequest): Intent {
      return Intent(context, CallActivity::class.java).apply {
        putExtra(IntentKeys.EXTRA_DESTINATION_NUMBER, request.destinationNumber)
        putExtra(IntentKeys.EXTRA_DESTINATION_NAME, request.destinationName)
        putExtra(IntentKeys.EXTRA_CONTACT_IMAGE, request.contactImage)
        putExtra(IntentKeys.EXTRA_METADATA, MetadataConverter.toHashMap(request.metadata))
        putExtra(IntentKeys.EXTRA_INCOMING_CALL, false)
      }
    }

    fun createIncomingIntent(context: Context, request: CallRequest): Intent {
      return createIntent(context, request).apply {
        putExtra(IntentKeys.EXTRA_INCOMING_CALL, true)
      }
    }

    private fun parseRequest(intent: Intent): CallRequest {
      val destinationNumber =
        requireNotNull(intent.getStringExtra(IntentKeys.EXTRA_DESTINATION_NUMBER)) {
          "Missing destination number for CallActivity."
        }
      val destinationName = intent.getStringExtra(IntentKeys.EXTRA_DESTINATION_NAME)

      return CallRequest(
        destinationNumber = destinationNumber,
        destinationName = destinationName,
        contactImage = intent.getStringExtra(IntentKeys.EXTRA_CONTACT_IMAGE),
        metadata = MetadataConverter.fromSerializable(
          serializableExtra(
            intent,
            IntentKeys.EXTRA_METADATA
          )
        ),
      )
    }

    private fun serializableExtra(intent: Intent, key: String): Serializable? {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getSerializableExtra(key, HashMap::class.java)
      } else {
        @Suppress("DEPRECATION")
        intent.getSerializableExtra(key)
      }
    }

    private fun requiredPermissions(): Array<String> {
      // Linphone voice call only needs microphone access for the outgoing audio stream.
      return arrayOf(
        Manifest.permission.RECORD_AUDIO,
      )
    }
  }
}


@Composable
private fun Loading(message: String, onDismissRequest: () -> Unit) {
  Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(24.dp)
          .background(Color.White, RoundedCornerShape(12.dp))
          .padding(24.dp)
      ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Colors.Orange500)
        Text(message, Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)
      }
    }
  }
}


@Preview(showSystemUi = true)
@Composable
private fun PrevLoading() {
  MaterialTheme() {
    Loading("Loading..") { }
  }

}

@Preview
@Composable
private fun Prev() {
  MaterialTheme {
    CallScreen(
      isMicMuted = false,
      speakerOutput = SpeakerOut.LoadSpeaker,
      state = CallUiState(
        destinationNumber = "08123123",
        destinationName = "Ira Adi",
        contactImage = "cumi",
        metadata = mapOf(),
        callState = CallState.Connected,
        callStateMessage = "call end",
        fatalError = "error",
        timeCall = "12:12"
      ),
      onMuteClick = {},
      onSpeakerOutputSelected = {},
      onNumpadClick = {},
      onEndCallClick = {},
      onError = {}
    )
  }
}
