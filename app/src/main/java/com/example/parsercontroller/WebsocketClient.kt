package com.example.parsercontroller

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*




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

class WebsocketClient(ctx: Context,messages: SnapshotStateList<ConsoleMsg>) {
    val server_ip  = "ws://83.147.245.51:8760"
    val ctx = ctx
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


   suspend fun StartTask(task_name:String,testMode: Boolean)   {
        this.ImportModules()
        val ns = NotificationService(this.ctx)

        Log.i("StartTask",task_name.toString())
        client.webSocket(server_ip){
            try {
                sendSerialized(Task("start",testMode,false,task_name))

                var res = incoming.receive()
                var resultText = (res as Frame.Text).readText()
                Log.i("info",resultText)

                if (resultText == "started"){
                    messages.add(ConsoleMsg("[$task_name] start","good"))
                } else {
                    messages.add(ConsoleMsg("[$task_name] $resultText","bad"))
                }


                res = incoming.receive()
                resultText = (res as Frame.Text).readText()
                val jsRes = Json.parseToJsonElement(resultText).jsonObject
                Log.i("info",resultText)
                ns.PushNotification(task_name+" "+jsRes["data"])

                messages.add(ConsoleMsg("[$task_name]  "+jsRes["data"],"good"))

                client.close()


            } catch (e: Exception){
                Log.e("ERR",e.toString())
                messages.add(ConsoleMsg(e.toString(),"bad"))
            }
        }
   }



}