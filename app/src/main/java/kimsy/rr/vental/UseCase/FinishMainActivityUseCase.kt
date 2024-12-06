package kimsy.rr.vental.UseCase

import android.app.Activity
import android.content.Context
import android.content.Intent
import kimsy.rr.vental.MainActivity
import javax.inject.Inject

class FinishMainActivityUseCase @Inject constructor() {
    fun execute(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        (context as Activity).finish()
    }
}