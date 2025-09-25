package com.example.spegen

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Screen()
        }
    }
}

object input {
    var newText = ""
}

fun InvokeAPI(symbol: String) {
    val url = "https://www.opensymbols.org/search?q=" + symbol

    val stringRequest = StringRequest(
        Request.Method.GET, url,
        Response.Listener { response ->
            // Handle the response data
            println("Works")
        },
        Response.ErrorListener { error ->
            // Handle errors
            println("Doesn't Work")
        })
}

@Composable
fun InputBox() {
    var text by remember { mutableStateOf("") }
    TextField(
        value = text, // The current text displayed in the input box
        onValueChange = {
                newText -> text = newText
            InvokeAPI(newText)
        }, // Callback when text changes
        label = { Text("Image Search") }, // Optional label for the input box
        modifier = Modifier
            .fillMaxWidth() // Make the TextField fill the width
            .padding(50.dp)
    )
}

@Composable
fun TextSubmitButton() {
    Column() {
        Button(
            onClick = { },
            modifier = Modifier.offset(80.dp, 80.dp)
        ) {
            Text(text = "Submit")
        }
    }
}

@Composable
fun Screen() {
    TextSubmitButton()
    InputBox()
}

val sentences = listOf(
    "This works",
    "Test 1 2 3",
    "I am a text to speech engine!",
)


@Composable
fun TextToSpeechScreen() {
    var isSpeaking by remember { mutableStateOf(false) }
    val tts = rememberTextToSpeech()
    if (tts.value?.isSpeaking == true) {
        tts.value?.stop()
        isSpeaking = false
    } else {
        tts.value?.speak(
            input.newText, TextToSpeech.QUEUE_FLUSH, null, ""
                    )
        isSpeaking = true
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
