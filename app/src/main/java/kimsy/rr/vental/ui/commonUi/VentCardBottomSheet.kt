package kimsy.rr.vental.ui.commonUi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kimsy.rr.vental.R
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardShareModel


@Composable
fun VentCardBottomSheet(
    modifier: Modifier,
    ventCard: VentCard,
    currentUserId: String,
    toReportVentCardView: () -> Unit,
    toRequestVentCardDeletionView: () -> Unit,
    hideModal: () -> Unit
){
    Box(
        Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                MaterialTheme.colors.primarySurface
            )
    ){
        Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween){
            if (currentUserId == ventCard.posterId) {
                Row(
                    modifier = modifier
                        .padding(16.dp)
                        .clickable {
                            VentCardShareModel.setDeleteRequestedVentCardToModel(ventCard)
                            hideModal()
                            toRequestVentCardDeletionView()
                        }
                ){
                    Icon(modifier = Modifier.padding(end = 8.dp),
                        painter =  painterResource(id = R.drawable.outline_delete_24),
                        contentDescription = "request debate deletion")
                    Text(text = stringResource(id = R.string.to_do_request_ventCard_deletion), fontSize = 20.sp, color = Color.White)
                }
            } else {
                Row(
                    modifier = modifier
                        .padding(16.dp)
                        .clickable {
                            VentCardShareModel.setReportedVentCardToModel(ventCard)
                            hideModal()
                            toReportVentCardView()
                        }
                ){
                    Icon(modifier = Modifier.padding(end = 8.dp),
                        painter =  painterResource(id = R.drawable.outline_report_problem_24),
                        contentDescription = "report debate")
                    Text(text = stringResource(id = R.string.to_do_report_ventCard), fontSize = 20.sp, color = Color.White)
                }
            }
        }
    }
}

