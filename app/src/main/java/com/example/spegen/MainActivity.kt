package com.example.spegen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.spegen.ui.theme.SpeGenTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.onGloballyPositioned
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleInputTextBox()
        }
    }
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
fun SimpleInputTextBox() {
    // State to hold the current value of the text field
    var text by remember { mutableStateOf("") }

    Column() {
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
}
