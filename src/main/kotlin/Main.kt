package me.naotiki

import kotlinx.coroutines.*
import java.net.ServerSocket


suspend fun main(vararg args: String) {
    val port = args.singleOrNull()?.toIntOrNull() ?: 22413
    val stat = Stat(port)
    runCatching {
        val serverSocket = ServerSocket(port)
        println("Server Listening on port $port")
        coroutineScope {
            launch {
                while (true) {
                    readln()
                    println(stat.generateStatString())
                }
            }
            withContext(Dispatchers.IO) {
                while (true) {
                    val client = serverSocket.accept()
                    launch {
                        client.use {
                            val gobanSession = GobanSession(it)
                            stat.startSession()
                            println(stat.generateStatString())
                            while (true) {
                                if (gobanSession.peerTurn()) {
                                    break
                                }
                                yield()
                                if (gobanSession.myTurn()) {
                                    break
                                }
                                yield()
                            }
                            val result = gobanSession.end()

                            stat.endSession(result)
                            println(stat.generateStatString())
                        }
                    }
                }
            }
        }
    }

}