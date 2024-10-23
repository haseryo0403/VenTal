package kimsy.rr.vental.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRegistScreen(

){
    var userName by remember{ mutableStateOf("")}
    var gender by remember{ mutableStateOf("")}
    var yearOfBirth by remember{ mutableStateOf("")}
    var selfIntroduction by remember{ mutableStateOf("")}

    Column (

    ) {

        Text("ユーザーネームを入力してください")
        OutlinedTextField(
            value = userName,
            onValueChange = {userName = it},
            label = {Text("ニックネームOK")},
        )

    }
}