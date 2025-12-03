package com.example.spegen

import android.content.res.Configuration
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import coil3.compose.AsyncImage
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import coil3.request.ImageRequest
import kotlin.math.floor
import android.content.res.Resources
import android.service.autofill.Validators.or
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.spegen.lazy_images_exceeding
import java.security.KeyStore
import kotlin.math.abs
import kotlin.math.floor


// Text box text variable
var text: String = ""

// Shared secret which is used for calling for an access token
const val CLIENT_SECRET = "d65234627cc790cba662f6b3"

// Access token that is used for calling to the API
var accesstoken = ""

// These are all of the variables that update whenever an image is called based off of its properties to allow for global use.
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

// Variable that will update if the image is not found or is empty. Used in the LoadImages function.
var empty = true

// Screen height and width variables as determined by GetScreenDimensions()
var screenHeight = 0.dp
var screenWidth = 0.dp

// Ensures that the function that manages image display is always ran through changing this value when OpenSymbolsButton is clicked
var alternate = false

// See above, only this one is for SymbolsButtonExec
var alternate_button = false

// Amount of images that should be displayed on screen when calling for images
var display_images = 6

// Is the device in landscape?
var isLandscape = false

var image_names = mutableListOf("")

var image_urls = mutableListOf("")

var lazy_images_exceeding = false

var image_number = 0


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
    // Text box that automatically updates the text variable to whatever the user types
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
fun GetScreenDimensions() {
    // Function that gets the dimensions of the screen for later use in UI scaling
    var configuration = LocalConfiguration.current
    screenWidth = configuration.screenWidthDp.dp
    screenHeight = configuration.screenHeightDp.dp
    configuration = LocalConfiguration.current
    isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}


@Composable
fun GridDisplay() {
    LazyHorizontalGrid (
        rows = GridCells.Adaptive((200).dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        contentPadding = PaddingValues(1.dp),
        userScrollEnabled = true,

        modifier = Modifier.fillMaxSize().offset(x=(abs((floor(((screenWidth)/100).toString().substringBefore(".").toInt().toDouble()) * 100))*-1).dp, y = 8.dp)

    ) {
        var image_display_number = 0
        image_number = 0
        items(display_images) { item ->
            Loadimages(image_display_number)
            image_display_number += 1
            image_number += 1
        }
    }

}

@Composable
fun Loadimages(image_num: Int) {
    // Displays images and controls their position and size. If no images are found it will display an image not found (See drawables in resource folder)
    if (empty) {
        if (!isLandscape) {
            Image(
                painter = painterResource(id = R.drawable.image_not_found_error),
                modifier = Modifier
                    .requiredSize(
                        width = (1.27226463104 * screenWidth),
                        height = (1.17508813161 * screenHeight)
                    )
                    .offset(
                        x = (-(0.63613231552 * screenWidth)),
                        y = (0.02350176263 * screenHeight)
                    ),
                contentDescription = "Image not found",
            )
        }

        else {
            Image(
                painter = painterResource(id = R.drawable.image_not_found_error),
                modifier = Modifier
                    .requiredSize(
                        width = (1.27226463104 * screenWidth),
                        height = (0.8 * screenHeight)
                    )
                    .offset(
                        x = (-(0.2 * screenWidth)),
                        y = (0.02350176263 * screenHeight)
                    ),
                contentDescription = "Image not found"
            )
        }
    }

    else {
        val tts = rememberTextToSpeech()
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image_urls[image_number])
                .build(),
            "Picture of ${image_names[0]}",
            modifier = Modifier
                .padding(screenWidth / 8)
                .size(width = screenWidth / 3, height = screenHeight / 8)
                .clickable(onClick = {
                    if (tts.value?.isSpeaking == true) {
                        tts.value?.stop()
                    } else tts.value?.speak(
                        (image_names[image_num]), TextToSpeech.QUEUE_FLUSH, null, ""
                    )
                })
        )
    }
}

@Composable
fun TextSubmitButton() {
    // Button that converts the value in the text box to TTS.
    val tts = rememberTextToSpeech()
    Column(modifier = Modifier
        .padding(24.dp)
        .offset(x = 260.dp, 10.dp)) {
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
fun SymbolsButtonExec() {
    // Oversees calling to the OpenSymbols API by calling several sub-functions
    var image_num by remember { mutableIntStateOf(0) }
    var image_int by remember { mutableIntStateOf(0) }
    image_names.clear()
    image_urls.clear()
    for (i in 1..display_images) {
        image_num = i
        image_int = i-1
        var displayImages by remember { mutableIntStateOf(1) }
        runBlocking {
            alternate = !alternate
            displayImages += 1
            getAccessToken()
            useApiWithToken(accesstoken, text, image_int)
        }

        image_names += name

        image_urls += image_url

        if (empty) {
            Loadimages(0)
            return
        }

        if (displayImages > 1 && alternate) {
            GridDisplay()
        }

        if (displayImages > 1 && !alternate) {
            GridDisplay()
        }
    }
}

@Composable
fun OpenSymbolsButton() {
    // Button that will execute SymbolsButtonExec; used for initiating interaction with OpenSymbols API
    var displayImages by remember { mutableIntStateOf(1) }
    Column(modifier = Modifier
        .padding(30.dp)
        .offset(x = 260.dp, y = 60.dp)) {
        Button(onClick = {
            displayImages += 1
            alternate_button = !alternate_button
        }) {
            Text("Search")
        }
        if (displayImages > 1 && alternate_button) {
            SymbolsButtonExec()
        }

        if (displayImages > 1 && !alternate_button) {
            SymbolsButtonExec()
        }
    }
}

@Composable
fun rememberTextToSpeech(): MutableState<TextToSpeech?> {
    // Handles TTS and its properties
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
    // Data class for getAccessToken to allow to parse the response data
    val access_token: String,
    val expires_in: Long
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ApiSymbolResponse(
    // Data class for useApiWithToken to allow to parse the response data
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
    // Gets a new access token using the shared secret
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


suspend fun useApiWithToken(token: String?, search: String, image_iteration: Int) {
    // Uses access token to get image data

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
            is Result.Failure -> {
                val ex = result.getException()
                println("API call failed: ${ex.message}")
            }
            is Result.Success -> {
                var symbolstring = (result.get()).replace("[", "").replace("]", "").split("},")[image_iteration]
                if (symbolstring.length > 1) {
                    symbolstring += "}"
                }
                if (symbolstring.count{ char -> char in "}" } > 0) {
                    symbolstring = symbolstring.dropLast((symbolstring.count { char -> char in "}" })-1)
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
                    empty = false
                }

                else {
                    id = 0
                    symbol_key = ""
                    name = ""
                    locale = ""
                    license = ""
                    license_url = ""
                    author = ""
                    author_url = ""
                    source_url = ""
                    skins = false
                    repo_key = ""
                    hc = false
                    extension = ""
                    image_url = ""
                    search_string = ""
                    unsafe_result = false
                    _href = ""
                    details_url = ""
                    empty = true
                }
            }
        }
    }
}


@Composable
fun Screen() {
    // Function that is called on application creation to display and call all of the other visible composable functions
    GetScreenDimensions()
    TextSubmitButton()
    InputBox()
    OpenSymbolsButton()
}