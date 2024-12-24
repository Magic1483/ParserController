package com.example.parsercontroller

import android.Manifest
import android.annotation.SuppressLint

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.util.cio.KtorDefaultPool
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context



@Serializable
data class Task(
    val command: String,
    val only_one:Boolean,
    val force_upload: Boolean,
    val service_task: String)

@Serializable
data class PythonModule(
    val command: String,
    val name: String,
    val data: String
)


enum class STATE {
    OPEN,CLOSE
}

class WebsocketClient(ctx: Context) {
//    val server_ip  = "ws://192.168.100.5:8750"
    val server_ip  = "ws://83.147.245.51:8760"
    val ctx = ctx
    var status = STATE.CLOSE



    val client = HttpClient(CIO){
        install(WebSockets){
            pingIntervalMillis = 20_000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }



    suspend fun ImportModules(){
        val fh = FilesHelper(this.ctx)
        val modules =
            listOf("utils.pyc","OzonCategory.pyc","OzonProduct.pyc","OzonProductHandler.pyc")

        
        client.webSocket(server_ip){
            try {
                for (f in modules){
                    sendSerialized(PythonModule(command = "test_load", name = f.replace(".pyc",""), data = fh.readFromAssets("modules/"+f)))

                    val res = incoming.receive()
                    val resultText = (res as Frame.Text).readText()

//                    if (resultText.contains("err")){
//                        messages.add(ConsoleMsg(resultText,"bad"))
//                    } else {
//                        messages.add(ConsoleMsg(resultText,"good"))
//                    }


                    Log.i("WS test_load",resultText)
                }

            } catch (e: Exception){
                Log.e("WS err",e.toString())
            }
        }
    }

    fun stop(){
        status = STATE.CLOSE
        client.close()
    }

    suspend fun Test(){
        status = STATE.OPEN
        val bgService = BackgroundService.getInstance()
        Log.i("TestWorker",server_ip.toString())
        client.webSocket(server_ip){
            try{
                var try_count = 1
                while (status == STATE.OPEN) {
                    send("test request $try_count")
                    bgService?.updateNotification("test request $try_count")
                    try_count++
                    var res = incoming.receive()
                    var resultText = (res as Frame.Text).readText()

                    Log.i("WebsocketClient",resultText.toString())
                    Messages.AddMsg(resultText,"good")
                    delay(5000)
                }
            } catch (e: Exception){
                Log.e("WebsocketClient",e.toString())
            }


        }
    }

   suspend fun StartTask(task: String,testMode: Boolean)   {
        this.ImportModules()
        val bgService = BackgroundService.getInstance()

        var task_name:String = ""
        when (task){
            "OZON_IMPORT" -> task_name = "import"
            "OZON_UPDATE_STOCK" -> task_name = "update_price"
            "OZON_UPDATE_PRICES" -> task_name = "update_stock"
        }

        Log.i("StartTask",task_name.toString())
        client.webSocket(server_ip){
            try {
                sendSerialized(Task("start",testMode,false,task_name))

                var res = incoming.receive()
                var resultText = (res as Frame.Text).readText()

                if (resultText == "started"){
                    Messages.AddMsg("[$task_name] $resultText","good")
                } else {
                    Messages.AddMsg("[$task_name] $resultText","bad")
                }
                bgService?.updateNotification("[$task_name] $resultText")

                res = incoming.receive()
                resultText = (res as Frame.Text).readText()
                val jsRes = Json.parseToJsonElement(resultText).jsonObject
                Log.i("info",resultText)

                Messages.AddMsg("[$task_name] ${jsRes["data"]}","good")
                bgService?.updateNotification("[$task_name] ${jsRes["data"]}")

//                client.close()


            } catch (e: Exception){
                Log.e("ERR",e.toString())
                Messages.AddMsg(e.toString(),"bad")
            }
        }
   }



}