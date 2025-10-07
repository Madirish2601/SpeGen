package com.example.spegen

import android.os.AsyncTask
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
import com.github.kittinunf.fuel.core.extensions.jsonBody
import java.util.Locale


var text: String = ""

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Screen()
            main()
        }
    }
}

@Composable
fun InputBox() {
    var text by remember { mutableStateOf("") }
    TextField(
        value = text, // The current text displayed in the input box
        onValueChange = {
                newText -> text = newText
                com.example.spegen.text = text
        }, // Callback when text changes
        label = { Text("Image Search") }, // Optional label for the input box
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
            } // End Button
        } // End for
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
    // Add other fields if needed
)

fun main() {
    val url = "https://opensymbols.org/api/v2/token/"
    val jsonPayload = "d65234627cc790cba662f6b3"

    Fuel.post(url)
        .jsonBody(jsonPayload)
        .response { request, response, result ->
            when (result) {
                is com.github.kittinunf.result.Result.Success -> {
                    val data = String(response.data)
                    println("Request: $request")
                    println("Response: $response")
                    println("Success: $data")
                }
                is com.github.kittinunf.result.Result.Failure -> {
                    val ex = result.getException()
                    println("Request: $request")
                    println("Response: $response")
                    println("Error: ${ex.message}")
                }
            }
        }
}

@Composable
fun Screen() {
    TextSubmitButton()
    InputBox()
}