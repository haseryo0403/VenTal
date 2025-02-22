package kimsy.rr.vental.ui.CommonComposable

import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun MaxLengthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    modifier: Modifier = Modifier,
    placeHolder: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(), // デフォルトのTextFieldColors
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = value))
    }
    val currentText = textFieldValueState.text.take(maxLength)
    val textFieldValue = if (value != currentText) {
        textFieldValueState.copy(text = value)
    } else {
        textFieldValueState
    }
    val onTextFieldValueChange = { text: TextFieldValue ->
        val isComposing = text.composition != null
        val nextText = text.text.take(maxLength)
        if (value != nextText) {
            onValueChange(nextText)
        }
        textFieldValueState = if (!isComposing) {
            text.copy(text = nextText)
        } else {
            text
        }
    }
    TextField(
        value = textFieldValue,
        onValueChange = onTextFieldValueChange,
        modifier = modifier,
        placeholder = placeHolder,
        colors = colors,
        trailingIcon = trailingIcon
    )
}