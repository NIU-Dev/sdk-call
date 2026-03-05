package com.neo.sdkcall

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import cc.cicare.sdkcall.CiCareSdkCall
import cc.cicare.sdkcall.event.CallEventListener
import cc.cicare.sdkcall.event.CallState
import cc.cicare.sdkcall.event.MessageActionListener

enum class NeoCallState {

  /**
   * Call is outgoing.
   */
  CALLING,

  /**
   * Answering call
   */
  ANSWERING,

  /**
   * Call is incoming and ringing on the callee side.
   */
  RINGING,
  RINGING_OK,
  RECONNECTING,

  /**
   * Call is being connected and signaling/negotiation is in progress.
   */
  CONNECTING,

  /**
   * Call has been answered and media stream is active.
   */
  CONNECTED,

  /**
   * Call has ended either by remote, local, or due to disconnection.
   */
  END,
  /**
   * Call has busy either by remote, local, or due to disconnection.
   */
  BUSY,
  /**
   * Call has refused either by remote, local, or due to disconnection.
   */
  REFUSED,
  /**
   * Call has missed either by remote, local, or due to disconnection.
   */
  MISSED,
  /**
   * Call is timout or no answer from callee
   */
  TIMEOUT
}

fun interface NeoMessageActionListener {
  fun onShowMessagePage()
}

interface NeoCallEventListener {
  fun onCallStateChange(callState: NeoCallState)

  fun onError(code: Int, message: String)
}

object NeoSdkCall {

  fun init(context: Context): NeoSdkCall {
    CiCareSdkCall.init(context)
    return this
  }

  fun setApi(baseUrl: String, token: String) {
    CiCareSdkCall.setAPI(baseUrl = baseUrl, token = token)
  }

  fun setAPI(baseUrl: String, token: String) {
    setApi(baseUrl = baseUrl, token = token)
  }

  fun setRingTone(ringTone: Uri) {
    CiCareSdkCall.setRingTone(ringTone)
  }

  fun setEventListener(eventListener: NeoCallEventListener) {
    CiCareSdkCall.setEventListener(object : CallEventListener {
      override fun onCallStateChange(callState: CallState) {
        eventListener.onCallStateChange(callState.toNeoCallState())
      }

      override fun onError(code: Int, message: String) {
        eventListener.onError(code, message)
      }
    })
  }

  fun checkAndRequestPermissions(activity: Activity): Boolean {
    return CiCareSdkCall.checkAndRequestPermissions(activity)
  }

  fun showIncoming(
    callerId: String,
    callerName: String? = "Green SM Driver",
    callerAvatar: String? = "",
    calleeId: String,
    calleeName: String? = "Green SM Customer",
    calleeAvatar: String? = "",
    checkSum: String,
    metaData: Map<String, String> = emptyMap(),
    messageActionListener: NeoMessageActionListener
  ) {
    CiCareSdkCall.showIncoming(
      callerId = callerId,
      callerName = callerName,
      callerAvatar = callerAvatar,
      calleeId = calleeId,
      calleeName = calleeName,
      calleeAvatar = calleeAvatar,
      checkSum = checkSum,
      metaData = metaData,
      messageActionListener = MessageActionListener {
        messageActionListener.onShowMessagePage()
      }
    )
  }

  fun makeCall(
    activity: ComponentActivity,
    callerId: String,
    callerName: String? = "Caller",
    callerAvatar: String? = "",
    calleeId: String,
    calleeName: String? = "Callee",
    calleeAvatar: String = "",
    checkSum: String,
    metaData: Map<String, String> = emptyMap()
  ) {
    CiCareSdkCall.makeCall(
      activity = activity,
      callerId = callerId,
      callerName = callerName,
      callerAvatar = callerAvatar,
      calleeId = calleeId,
      calleeName = calleeName,
      calleeAvatar = calleeAvatar,
      checkSum = checkSum,
      metaData = metaData
    )
  }

  fun makeCallSip(
    activity: ComponentActivity,
    callerId: String,
    callerName: String? = "Caller",
    callerAvatar: String? = "",
    destination: String,
    destinationName: String,
    destinationAvatar: String,
    checkSum: String,
    metaData: Map<String, String> = emptyMap()
  ) {
    CiCareSdkCall.makeCallSip(
      activity = activity,
      callerId = callerId,
      callerName = callerName,
      callerAvatar = callerAvatar,
      destination = destination,
      destinationName = destinationName,
      destinationAvatar = destinationAvatar,
      checkSum = checkSum,
      metaData = metaData
    )
  }
}

private fun CallState.toNeoCallState(): NeoCallState {
  return when (this) {
    CallState.CALLING -> NeoCallState.CALLING
    CallState.ANSWERING -> NeoCallState.ANSWERING
    CallState.RINGING -> NeoCallState.RINGING
    CallState.RINGING_OK -> NeoCallState.RINGING_OK
    CallState.RECONNECTING -> NeoCallState.RECONNECTING
    CallState.CONNECTING -> NeoCallState.CONNECTING
    CallState.CONNECTED -> NeoCallState.CONNECTED
    CallState.END -> NeoCallState.END
    CallState.BUSY -> NeoCallState.BUSY
    CallState.REFUSED -> NeoCallState.REFUSED
    CallState.MISSED -> NeoCallState.MISSED
    CallState.TIMEOUT -> NeoCallState.TIMEOUT
  }
}