package com.example.spegen

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import androidx.compose.foundation.layout.fillMaxSize
import coil3.request.ImageRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextField
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.fromColorLong
import androidx.lifecycle.MutableLiveData
import kotlin.math.absoluteValue


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
var empty = false

// Screen height and width variables as determined by GetScreenDimensions()
var screenHeight = 0.dp
var screenWidth = 0.dp

// Ensures that the function that manages image display is always ran through changing this value when OpenSymbolsButton is clicked
var alternate = false

// See above, only this one is for SymbolsButtonExec
var alternate_button = false

// Amount of images that should be displayed on screen when calling for images
var display_images = 8

// Is the device in landscape?
var isLandscape = false

var image_names = mutableListOf("")

var image_urls = mutableListOf("")

var image_number = 0

var maxItems = display_images

val paddingDividend = 50

var static_row_height = 0.dp

var button_boxes_width = 0.dp

val home = menutemplate(1, "Menu", 1, listOf("My"), listOf(2), listOf("i", "see", "dog", "moose", "1", "2", "3", "4", "5", "6", "shidfuhosd"), listOf(1,1,1,1,1,1,1,1,1,1,1))

val my = menutemplate(2, "My", 1, listOf("I"), listOf(3), listOf("i", "me", "mine", "eye", "1", "2", "10", "4", "5", "6", "1938"), listOf(1,1,1,1,1,1,1,1,1,1,1))

var MenuList = listOf<menutemplate>(home, my)

var box_size = 100.dp

var box_padding = 20.dp

var menu_height = (screenHeight - static_row_height - static_row_height - static_row_height - static_row_height)

var menu_width = screenWidth - (button_boxes_width * 2)

var selected_symbols = mutableStateListOf<String>()

var tts: MutableState<TextToSpeech?> = mutableStateOf(null)

var wordfinder_display = mutableIntStateOf(0)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Screen()
        }
    }
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
fun rememberTextToSpeech(): MutableState<TextToSpeech?> {
    // Handles TTS and its properties
    val context = LocalContext.current
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.US
            }
        }

        tts.value = textToSpeech

        onDispose {
            textToSpeech.stop()
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


suspend fun useApiWithToken(token: String?, search: String) {
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
                var symbolstring = (result.get()).replace("[", "").replace("]", "").split("},")[0]
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


// Function that creates the static row of always accessible words at the bottom of the screen for easy access with for loop that allows for customization through variables
@Composable
fun Static_Row_Needs() {
    tts = rememberTextToSpeech()
    val static_terms: MutableList<String> = mutableListOf("Yes", "No", "Food", "Water", "I need my parent", "I use a talker to communicate")
    var text_color = Color.Black // Set as var to be able to be customized by user later
    var text_alignment = Alignment.Center // Set as var to be able to be customized by user later
    var box_color = Color.White // Set as var to be able to be customized by user later
    var border_size = 2.dp // Set as var to be able to be customized by user later
    var border_color = Color.Black // Set as var to be able to be customized by user later
    var width = (screenWidth/static_terms.size.dp).dp // Determine width of boxes by dividing screen width by total number of boxes which is equal to number of needed terms
    static_row_height = (screenHeight.value*((70.dp/screenHeight).dp).value).dp // Fraction determined by base value of 70.dp then converted to fraction and applied to screen height to (hopefully) make box height scale with screen height
    var y_offset = (screenHeight-static_row_height) // Determines Y offset by subtracting height from the total screen width
    var x_offset = (0).dp // Determines X offset. Not needed since the first box starts at the left edge of the screen.
    for (i in 0 until static_terms.size) // For loop to create modular number of boxes. Starts at zero due to X offset calculations and ends at the number of terms minus 1 since it starts at zero
        Column() {
            val text = static_terms[i]
            Box(
                // FIX Y OFFSET
                modifier = Modifier
                    .offset((x_offset+(width*i)), y_offset)
                    .width(width)
                    .height(static_row_height)
                    .background(color = box_color)
                    .border(border = BorderStroke(border_size, border_color))
                    .clickable(onClick = {
                        if (tts.value?.isSpeaking == true) {
                            tts.value?.stop()
                        } else tts.value?.speak(
                            text, TextToSpeech.QUEUE_FLUSH, null, ""
                        )
                    })
            ) {
                Text(text = static_terms[i], color = text_color, modifier = Modifier.align(text_alignment))
            }
        }
}

@Composable
fun InputBox(modifier: Modifier) {
    tts = rememberTextToSpeech()
    Row {
        LazyRow(
            modifier = modifier.width(screenWidth - (button_boxes_width * 2))
                .height(button_boxes_width * 2)
                .background(Color.White)
                .border(width = 4.dp, color = Color.Black, shape = RoundedCornerShape(0.dp))
                .clickable(onClick = {
                    var speech = selected_symbols.joinToString(separator = " ")
                    if (tts.value?.isSpeaking == true) {
                        tts.value?.stop()
                    } else tts.value?.speak(
                        speech, TextToSpeech.QUEUE_FLUSH, null, ""
                    )
                }
                )
        ) {
            items(selected_symbols.size) { item ->
                runBlocking {
                    useApiWithToken(accesstoken, selected_symbols[item])
                }
                InputBox_Symbol(name)
            }
        }
    }
}


@Composable
fun InputBox_Symbol(Name: String) {
    val name = Name.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase()
        else it.toString() }
    var height_dp = 16
    var width_dp = height_dp*3.0625
    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image_url)
                .build(),
            "Picture of $Name",
            modifier = Modifier
                .background(Color.White)
                .padding(box_padding)
                .scale(1f)
                .size(box_size)
        )
        Text(text = name, color = Color.Black, modifier = Modifier.padding(1.dp).height(height_dp.dp).width(width_dp.dp).align(Alignment.BottomCenter), textAlign = TextAlign.Center)
    }
}

@Composable
fun Symbol(Name: String, Vertical_Stretch: Dp, tts_type: Int) {
    tts = rememberTextToSpeech()
    val name = Name.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase()
        else it.toString() }
    var height_dp = 16
    var width_dp = height_dp*3.0625
    var switchmenu by remember { mutableStateOf(false) }
    var switchmenu1 by remember { mutableStateOf(false) }
    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image_url)
                .build(),
            "Picture of $Name",
            modifier = Modifier
                .height(box_size+Vertical_Stretch+(box_padding*3))
                .background(Color.White)
                .border(width = 4.dp, color = Color.Black, shape = RoundedCornerShape(40.dp))
                .padding(box_padding)
                .scale(1f)
                .width(box_size)
                .clickable(onClick = {
                    if (tts_type == 0) {
                        if (tts.value?.isSpeaking == true) {
                            tts.value?.stop()
                        } else tts.value?.speak(
                            (name), TextToSpeech.QUEUE_FLUSH, null, ""
                        )
                    }
                    if (tts_type == 1) {
                        selected_symbols += name
                    }
                    if (tts_type == 2) {
                        if (tts.value?.isSpeaking == true) {
                            tts.value?.stop()
                        } else tts.value?.speak(
                            (name), TextToSpeech.QUEUE_FLUSH, null, ""
                        )
                        selected_symbols += name
                    }
                    if (switchmenu == false) {
                        switchmenu = !switchmenu
                    } else {
                        switchmenu = !switchmenu
                        switchmenu1 = !switchmenu1
                    }
                })
        )
        Text(text = name, color = Color.Black, modifier = Modifier.padding(1.dp).height(height_dp.dp).width(width_dp.dp).align(Alignment.BottomCenter), textAlign = TextAlign.Center)
    }
}

@Composable
fun Folder(Name: String, LinkedMenu: Int, Vertical_Stretch: Dp) {
    val name = Name.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase()
        else it.toString() }
    var height_dp = 16
    var width_dp = height_dp*3.0625
    var switchmenu by remember { mutableStateOf(false) }
    var switchmenu1 by remember { mutableStateOf(false) }
    Box() {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image_url)
                .build(),
            "Picture of $name",
            modifier = Modifier
                .height(box_size+Vertical_Stretch+(box_padding*3))
                .background(Color.White)
                .border(width = 4.dp, color = Color.Black, shape = RoundedCornerShape(40.dp))
                .padding(box_padding)
                .scale(1f)
                .width(box_size)
                .clickable(onClick = {
                    if (switchmenu == false) {
                        switchmenu = !switchmenu
                    } else {
                        switchmenu = !switchmenu
                        switchmenu1 = !switchmenu1
                    }
                })
        )
        Text(
            text = name,
            color = Color.Black,
            modifier = Modifier.padding(1.dp).height(height_dp.dp).width(width_dp.dp)
                .align(Alignment.BottomCenter),
            textAlign = TextAlign.Center
        )
        if (switchmenu or switchmenu1) {
            MenuParser(MenuFinder(LinkedMenu))
        }
    }
}

data class menutemplate(
    val id: Int, // ID of the current menu
    val title: String, // Title of the current menu
    val parentId: Int?, // ID of the parent menu
    val folders: List<String>, // List of folder names to be used with the API function useAPIWithToken
    val pointers: List<Int>, // Pointers to be used in MenuFinder to find the corresponding menu for a folder to link to
    val symbols: List<String>, // List of symbol names to be used with the API function useAPIWithToken
    val tts: List<Int> // 0 is for appending to the input box without instantly playing, 1 is for instantly playing in tts engine without appending to input box, 2 is for both appending to text box and playing in tts engine instantly
)

fun MenuFinder(menu_id: Int?): menutemplate {
    if (menu_id !is Int) {
        return home
    }
    for (i in 0 until MenuList.size) {
        if (MenuList[i].id == menu_id) {
            return MenuList[i]
        }
    }
    return home
}

@Composable
fun MenuParser(menutemplate: menutemplate, modifier: Modifier = Modifier) {
    var totalitems = ((screenWidth - (button_boxes_width * 2))/(box_size + (box_padding*2)))*((screenHeight-(static_row_height*2))/box_size)
    var total_box_size = box_size+(box_padding*2)
    val vertical_stretch = ((menu_height)-((((menu_height)/(total_box_size)).toInt())*total_box_size))
    runBlocking {
        getAccessToken()
    }
    FlowRow(modifier = modifier.fillMaxWidth().fillMaxHeight(), horizontalArrangement = Arrangement.SpaceBetween) {
        var itemsdisplayed = 0
        for (i in 0 until menutemplate.folders.size) {
            runBlocking {
                useApiWithToken(accesstoken, menutemplate.folders[i])
            }
            if (id != 0) {
                Folder(name, menutemplate.pointers[i], vertical_stretch)
                itemsdisplayed += 1
            }
        }
        for (i in 0 until menutemplate.symbols.size) {
            runBlocking {
                useApiWithToken(accesstoken, menutemplate.symbols[i])
            }
            if (id != 0) {
                Symbol(name, vertical_stretch, menutemplate.tts[i])
                itemsdisplayed += 1
            }
        }
        for (i in 0 until totalitems.toInt()-(itemsdisplayed)) {
            Box(modifier = Modifier
                .background(Color.White)
                .border(width = 4.dp, color = Color.Black, shape = RoundedCornerShape(40.dp))
                .padding(box_padding)
                .scale(1f)
                .height(box_size+vertical_stretch+box_padding)
                .width(box_size))
        }
    }
}

@Composable
fun Menu(modifier: Modifier) {
    menu_height = (screenHeight - static_row_height - static_row_height - static_row_height - static_row_height)
    menu_width = screenWidth - (button_boxes_width * 2)
    Column(
        modifier = Modifier.alpha(1f)
    ) {
        Column(
            modifier = modifier
                .width(menu_width)
                .height(menu_height)
                .offset(x = 0.dp, y = (static_row_height*2))
        ) {
            MenuParser(MenuFinder(1))
        }
    }
}

@Composable
fun MenuRow(modifier: Modifier) {
    val menu_terms: MutableList<String> = mutableListOf("Home", "Temp", "Temp2", "Temp3", "Temp4", "Temp5")
    val linked_menus: MutableList<Int?> = mutableListOf(0,null,null,null,null,null)
    var text_color = Color.Black // Set as var to be able to be customized by user later
    var box_color = Color.White // Set as var to be able to be customized by user later
    var border_size = 2.dp // Set as var to be able to be customized by user later
    var border_color = Color.Black // Set as var to be able to be customized by user later
    var width = (screenWidth/menu_terms.size.dp).dp // Determine width of boxes by dividing screen width by total number of boxes which is equal to number of needed terms
    static_row_height = (screenHeight.value*((70.dp/screenHeight).dp).value).dp // Fraction determined by base value of 70.dp then converted to fraction and applied to screen height to (hopefully) make box height scale with screen height
    var y_offset = (screenHeight-static_row_height-static_row_height) // Determines Y offset by subtracting height from the total screen width
    var x_offset = (0).dp // Determines X offset. Not needed since the first box starts at the left edge of the screen.
    var switchmenu by remember { mutableStateOf(false) }
    var switchmenu1 by remember { mutableStateOf(false) }
    for (i in 0 until menu_terms.size) // For loop to create modular number of boxes. Starts at zero due to X offset calculations and ends at the number of terms minus 1 since it starts at zero
        Column() {
            Box(
                // FIX Y OFFSET
                modifier = modifier
                    .offset((x_offset+(width*i)), y_offset)
                    .width(width)
                    .height(static_row_height)
                    .background(color = box_color)
                    .border(border = BorderStroke(border_size, border_color))
                    .clickable(onClick = {
                        if (switchmenu == false) {
                            switchmenu = !switchmenu
                        }
                        else {
                            switchmenu = !switchmenu
                            switchmenu1 = !switchmenu1
                        }
                    })
            ) {
                Text(
                    text = menu_terms[i],
                    color = text_color,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            if (switchmenu or switchmenu1) {
                MenuParser(MenuFinder(linked_menus[i]), Modifier.offset(0.dp,-150.dp))
            }
        }
}

// Could make an image override function that lets the user use their own images in place of the default ones
fun ImageOverride() {
}

@Composable
fun WordFinder() {
    val a = remember {mutableIntStateOf(1)}
    var search_row_display = remember {mutableIntStateOf(0)}
    Box(modifier = Modifier.fillMaxSize().background(Color(red = 230, green = 227, blue = 227, alpha = 100))) {
        Box(
            modifier = Modifier
                .offset(x = (screenWidth.value*0.1).dp, y = ((screenHeight.value - static_row_height.value)*0.1).dp)
                .border(width = 4.dp, color = Color.Black, shape = RoundedCornerShape(40.dp))
                .clip(RoundedCornerShape(40.dp))
                .height(((screenHeight.value - static_row_height.value) * (0.8)).dp)
                .width((screenWidth.value * 0.8).dp).background(Color.White)
                .padding(vertical = 20.dp)
        ) {
            var text by remember { mutableStateOf("") }
            TextField(
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                value = text,
                onValueChange = { newText ->
                    text = newText
                },
                label = { Text("Image Search") },
                modifier = Modifier.offset((((screenWidth.value * 0.8).dp/2)-(screenWidth.value*0.1).dp))
            )
            Button(
                modifier = Modifier.offset((((screenWidth.value * 0.8).dp/4)-(screenWidth.value*0.1).dp)),
                onClick = {
                    wordfinder_display.intValue = 0
                }
            ) {
                Text(text = "Close", textAlign = TextAlign.Center)
            }
            Button(
                modifier = Modifier.offset(x = ((((screenWidth.value * 0.8)/4)+((((screenWidth.value * 0.8)/2))))).dp),
                onClick = {
                    // Calculate where item is in menus and how to navigate to it. Could use something like bubble sort.
                }
            ) {
                Text(text = "Search", textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun Buttonboxes() {
    val a = remember {mutableIntStateOf(0)}
    println(button_boxes_width)
    val x_offset = ((screenWidth - button_boxes_width - 70.dp).value).dp
    val y_offset = 0.dp
    button_boxes_width = 70.dp
    var switchmenu by remember { mutableStateOf(false) }
    var switchmenu1 by remember { mutableStateOf(false) }
    if (wordfinder_display.value == a.value) {
        //TOP RIGHT
        Column() {
            Box(
                modifier = Modifier
                    .offset(x_offset, y_offset)
                    .size(button_boxes_width)
                    .background(color = Color.White)
                    .border(border = BorderStroke(2.dp, Color.Black))
                    .clickable(onClick = {
                    })
            ) {
                Text(text = "Settings", color = Color.Black, modifier = Modifier.align(Alignment.Center))
            }
        }
        //BOTTOM RIGHT
        Column() {
            Box(
                modifier = Modifier
                    .offset(x_offset, y_offset+140.dp)
                    .size(button_boxes_width)
                    .background(color = Color.White)
                    .border(border = BorderStroke(2.dp, Color.Black))
                    .clickable(onClick = {
                        if (tts.value?.isSpeaking == true) {
                            tts.value?.stop()
                        }
                    })
            ) {
                Text(text = "Stop", color = Color.Black, modifier = Modifier.align(Alignment.Center))
            }
        }
        //TOP LEFT
        Column() {
            Box(
                modifier = Modifier
                    .offset(x_offset-70.dp)
                    .size(button_boxes_width)
                    .background(color = Color.White)
                    .border(border = BorderStroke(2.dp, Color.Black))
                    .clickable(onClick = {
                    })
            ) {
                Text(text = "Keyboard", color = Color.Black, modifier = Modifier.align(Alignment.Center))
            }}
        //MIDDLE LEFT
        Column() {
            Box(
                modifier = Modifier
                    .offset(x_offset-70.dp, y_offset+140.dp)
                    .size(button_boxes_width)
                    .background(color = Color.White)
                    .border(border = BorderStroke(2.dp, Color.Black))
                    .clickable(onClick = {
                        selected_symbols.clear()
                    })
            ) {
                Text(text = "Clear", color = Color.Black, modifier = Modifier.align(Alignment.Center))
            }

        }
        //BOTTOM LEFT
        Column() {
            Box(
                modifier = Modifier
                    .offset(x_offset - 70.dp, y_offset + 70.dp)
                    .size(button_boxes_width)
                    .background(color = Color.White)
                    .border(border = BorderStroke(2.dp, Color.Black))
                    .clickable(onClick = {
                        if (selected_symbols.size >= 1) {
                            selected_symbols.removeAt(selected_symbols.lastIndex)
                        }
                    })
            ) {
                Text(text = "Delete", color = Color.Black, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
    //MIDDLE RIGHT
    Column() {
        Box(
            modifier = Modifier
                .offset(x_offset, y_offset+70.dp)
                .size(button_boxes_width)
                .background(color = Color.White)
                .border(border = BorderStroke(2.dp, Color.Black))
                .clickable(onClick = {
                    wordfinder_display.intValue = 1
                    })
        ) {
            Text(text = "Search", color = Color.Black, modifier = Modifier.align(Alignment.Center))
        }
    }
}



@Composable
fun Screen() {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        tts = rememberTextToSpeech()
        val a = remember { mutableIntStateOf(0) }
        GetScreenDimensions()
        Static_Row_Needs()
        if (wordfinder_display.intValue != a.intValue) {
            WordFinder()
        } else {
            Buttonboxes()
            MenuRow(Modifier)
            InputBox(Modifier)
            Menu(Modifier)
        }
    }
}