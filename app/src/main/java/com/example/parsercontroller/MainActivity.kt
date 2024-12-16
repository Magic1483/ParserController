package com.example.parsercontroller

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val ServiceOptions = listOf("Add to ozon","Update Stocks","Update Prices")
var messages  = mutableStateListOf<ConsoleMsg>()
//val ws = WebsocketClient()
val REQUEST_CODE: Int = 100

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("TAG","test")
        checkPerm()

        
        
        enableEdgeToEdge()
        setContent {
            Main(this)
        }

    }



    private fun checkPerm() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.POST_NOTIFICATIONS)){
            val builder =  AlertDialog.Builder(this);
            builder.setMessage("This app requires POST_NOTIFICATION PERM")
                .setTitle("Perm requets")
                .setCancelable(false)
                .setPositiveButton("OK",DialogInterface.OnClickListener { dialogInterface, i ->
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_CODE)
                    dialogInterface.dismiss()
                })
                .setNegativeButton("cancel!",DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
            builder.show()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE)
        }
    }


}

class ConsoleMsg(val msg: String, val type: String){

}



@Composable
fun Main(ctx: Context){
    var isTestMode by remember { mutableStateOf(false) }
    
    Column(
        Modifier
            .padding(start = 8.dp, end = 8.dp, top = 30.dp)
            .fillMaxHeight() ) {
        Text(text = "Console", style = MaterialTheme.typography.titleLarge)
        Surface(shape = MaterialTheme.shapes.large, shadowElevation = 4.dp, modifier = Modifier.padding(top = 8.dp)) {
            Column(modifier = Modifier
                .size(400.dp)
                .verticalScroll(rememberScrollState())
                .padding(all = 8.dp))
            {
                messages.forEach {m ->
                    MessageConsole(m)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)){
            Text(text = "TestMode")
            Checkbox(
                checked = isTestMode,
                onCheckedChange = { isTestMode = it}
            )
        }
        Surface (modifier = Modifier.padding(bottom = 40.dp)) {
            Column(modifier = Modifier.align(Alignment.End) ) {
                Text("Options", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                for (el in ServiceOptions){
                    Option(el,ctx, isTestMode)
                }
            }
        }
    }


}




@Composable
fun MessageConsole(msg: ConsoleMsg){
    if (msg.type == "good"){
        messages.plus(Text(text = msg.msg, color = Color(0xFF018786), style = MaterialTheme.typography.bodyLarge))
    } else {
        messages.plus(Text(text = msg.msg, color = Color(0xFFB00020), style = MaterialTheme.typography.bodyLarge))
    }

    Log.i("DEBUG",messages.toString())

}

@Composable
fun Option(text: String ,ctx: Context,testMode: Boolean){
    Button( onClick = { HandleTask(text,ctx,testMode) }, modifier = Modifier.width(210.dp) ) {
        Text(text)
    }
}


//https://needone.app/start-and-suspend-a-coroutine-in-kotlin/
@OptIn(DelicateCoroutinesApi::class)
fun HandleTask(name: String, ctx: Context,testMode: Boolean){

    val ws = WebsocketClient(ctx, messages)
    when (name){
        "Add to ozon"   -> GlobalScope.launch { ws.StartTask("import",testMode) }
        "Update Stocks"  -> GlobalScope.launch { ws.StartTask("update_stock",testMode) }
        "Update Prices"  -> GlobalScope.launch { ws.StartTask("update_price",testMode) }
   }
}

