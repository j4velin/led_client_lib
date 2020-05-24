package de.j4velin.ledclient.lib

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private val okHttp = OkHttpClient()
private val mediaType = "application/json; charset=utf-8".toMediaType()

val TAG = "LedClient.Lib"

class LedController(private val serverUrl: String) {
    fun trigger(effect: LedEffect) {
        Thread {

            Log.i(TAG, "Payload: ${effect.toJSON()}")

            val body = effect.toJSON().toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(serverUrl + "/effect/" + effect.name)
                .post(body)
                .build()

            okHttp.newCall(request).execute().use { response ->
                if (!response.isSuccessful) Log.e(TAG, "Exception triggering effect: $response")
            }
        }.start()
    }
}