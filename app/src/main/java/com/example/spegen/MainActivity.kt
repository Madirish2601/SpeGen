package com.example.spegen

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.jsonBody
import java.util.Locale
import com.github.kittinunf.fuel.httpPost
import java.net.URI
import kotlin.jvm.java
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.gson.responseObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Objects.toString


var text: String = ""

const val CLIENT_SECRET = "d65234627cc790cba662f6b3"


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        main()
        setContent {
            Screen()
        }
    }
}

@Composable
fun InputBox() {
    var text by remember { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = {
                newText -> text = newText
            com.example.spegen.text = text
        },
        label = { Text("Image Search") },
        modifier = Modifier
            .padding(vertical = 150.dp)
            .fillMaxWidth()
    )
}

@Composable
fun TextSubmitButton() {
    val tts = rememberTextToSpeech()

    Column(modifier = Modifier.padding(24.dp)) {
        Button(onClick = {
            if (tts.value?.isSpeaking == true) {
                tts.value?.stop()
            } else tts.value?.speak(
                text, TextToSpeech.QUEUE_FLUSH, null, ""
            )
        }
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun rememberTextToSpeech(): MutableState<TextToSpeech?> {
    val context = LocalContext.current
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.US
            }
        }
        tts.value = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
    return tts
}

data class AccessTokenResponse(
    val access_token: String,
    val expires_in: Long
)

suspend fun getAccessToken(): AccessTokenResponse? {
    return withContext(Dispatchers.IO) {
        val params = listOf(
            "secret" to CLIENT_SECRET
        )
        val (_, _, result) = Fuel.post("https://www.opensymbols.org/api/v2/token", params)
            .responseObject<AccessTokenResponse>()

        when (result) {
            is com.github.kittinunf.result.Result.Failure -> {
                val ex = result.getException()
                println("Failed to get access token: ${ex.message}")
                null
            }
            is com.github.kittinunf.result.Result.Success -> {
                val tokenResponse = result.get()
                println("Successfully retrieved access token: ${tokenResponse}")
                tokenResponse
            }
        }
    }
}


suspend fun useApiWithToken(token: String, search: String) {
    withContext(Dispatchers.IO) {
        val params = listOf(
            "q" to search,
            "locale" to "en",
            "safe" to "1"
        )

        val (_, _, result) = Fuel.get("https://www.opensymbols.org/api/v2/symbols", params)
            .responseString()

        when (result) {
            is com.github.kittinunf.result.Result.Failure -> {
                val ex = result.getException()
                println("API call failed: ${ex.message}")
            }
            is com.github.kittinunf.result.Result.Success -> {
                println("API call successful! Response: ${result.get()}")
            }
        }
    }
}


fun main() {
    runBlocking {
        getAccessToken()
        }
}



    @Composable
fun Screen() {
    TextSubmitButton()
    InputBox()
}