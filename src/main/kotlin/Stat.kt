package me.naotiki

import java.time.Clock
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

class Stat(private val port:Int) {
    private var aliveClientCount = AtomicInteger(0)
    private var totalClientCount = AtomicInteger(0)
    private val countEachResult = GameResult.entries.associateWith {
        AtomicInteger(0)
    }.toMutableMap()

    fun startSession() {
        aliveClientCount.incrementAndGet()
        totalClientCount.incrementAndGet()
    }

    fun endSession(result: GameResult?) {
        aliveClientCount.decrementAndGet()
        countEachResult[result]?.incrementAndGet()
    }

    fun generateStatString(): String {
        return buildString {
            append(
                """
                3moku Server by Naotiki [${LocalDateTime.now()}]
                Server Listening on port $port
                - Clients
                    - Alive: ${aliveClientCount.get()}
                    - Total: ${totalClientCount.get()}
                - Results
${
                    countEachResult.toList().joinToString("\n") {
                        "                    - ${it.first.text}: ${it.second.get()}"
                    }
                }
            """.trimIndent()
            )
        }
    }
}