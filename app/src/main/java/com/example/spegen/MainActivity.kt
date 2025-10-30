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
import java.util.Locale
import com.github.kittinunf.fuel.gson.responseObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.JsonIgnoreUnknownKeys


var text: String = ""

const val CLIENT_SECRET = "d65234627cc790cba662f6b3"

var accesstoken = ""
var expires_in = 0

var id = 0
var symbol_key = ""
var name = ""
var locale = ""
var license = ""
var license_url = ""
var author = ""
var author_url = ""
var source_url: String? = ""
var skins = false
var repo_key = ""
var hc = false
var extension = ""
var image_url = ""
var search_string: String? = ""
var unsafe_result = false
var _href = ""
var details_url = ""


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
fun OpenSymbolsButton() {
    val token =
    Column(modifier = Modifier.padding(30.dp)) {
        val tts = rememberTextToSpeech()
        Button(onClick = {
            runBlocking {
                getAccessToken()
                useApiWithToken(accesstoken, text)
            }
            println(name)
        }
        ) {
            Text("Search")
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

@Serializable
@JsonIgnoreUnknownKeys
data class ApiSymbolResponse(
    val id: Int,
    val symbol_key: String,
    val name: String,
    val locale: String,
    val license: String,
    val license_url: String,
    val author: String,
    val author_url: String,
    val source_url: String? = null,
    val skins: Boolean? = false,
    val repo_key: String,
    val hc: Boolean? = false,
    val extension: String,
    val image_url: String,
    val search_string: String? = null,
    val unsafe_result: Boolean,
    val _href: String,
    val details_url: String
)


suspend fun getAccessToken(): AccessTokenResponse? {
    return withContext(Dispatchers.IO) {
        val params = listOf(
            "secret" to CLIENT_SECRET
        )
        val (_, _, result) = Fuel.post("https://www.opensymbols.org/api/v2/token", params)
            .responseObject<AccessTokenResponse>()

        when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                println("Failed to get access token: ${ex.message}")
                null
            }

            is Result.Success -> {
                val tokenResponse = result.get()
                accesstoken = tokenResponse.access_token
                tokenResponse
            }
        }
    }
}


suspend fun useApiWithToken(token: String?, search: String) {
    withContext(Dispatchers.IO) {
        val params = listOf(
            "q" to search,
            "locale" to "en",
            "safe" to "0",
            "access_token" to token
        )

        val (_, _, result) = Fuel.get("https://www.opensymbols.org/api/v2/symbols", params)
            .responseString()

        when (result) {
            is com.github.kittinunf.result.Result.Failure -> {
                val ex = result.getException()
                println("API call failed: ${ex.message}")
            }
            is com.github.kittinunf.result.Result.Success -> {
                println("Result.get: ${params}")
                var symbolstring = (result.get()).replace("[", "").replace("]", "").split("},")[0]
                if (symbolstring.length > 1) {
                    symbolstring += "}"
                }
                print("Symbol String: $symbolstring")
                if (symbolstring.count{ char -> char in "}" } > 0) {
                    symbolstring = symbolstring.dropLast((symbolstring.count { char -> char in "}" })-1)
                    print("Symbol String Drop: $symbolstring")
                    val symbol = Json.decodeFromString<ApiSymbolResponse>(symbolstring)
                    id = symbol.id
                    symbol_key = symbol.symbol_key
                    name = symbol.name
                    locale = symbol.locale
                    license = symbol.license
                    license_url = symbol.license_url
                    author = symbol.author
                    author_url = symbol.author_url
                    source_url = symbol.source_url
                    skins = symbol.skins == false
                    repo_key = symbol.repo_key
                    hc = symbol.hc == false
                    extension = symbol.extension
                    image_url = symbol.image_url
                    search_string = symbol.search_string
                    unsafe_result = symbol.unsafe_result
                    _href = symbol._href
                    details_url = symbol.details_url
                }
                else {
                    println("Result not found.")
                    """Note: Tell the user that there were no results for the search; thats why the result.get was empty"""
                }
            }
        }
    }
}

private fun String.count(predicate: String) {}

@Composable
fun Screen() {
    TextSubmitButton()
    InputBox()
    OpenSymbolsButton()
}