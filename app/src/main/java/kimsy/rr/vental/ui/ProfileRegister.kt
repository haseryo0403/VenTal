package kimsy.rr.vental.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRegisterScreen(

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