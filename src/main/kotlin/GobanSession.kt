package me.naotiki

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.naotiki.GameResult.*
import java.net.Socket

enum class Stone {
    My,
    Peer
}

enum class GameResult(val text:String) {
    PeerWin("相手の勝ち"),
    MyWin("自分の勝ち"),
    Draw("引き分け")
}

class GobanSession(val socket: Socket) {
    private val inputStream = socket.getInputStream()
    private val outputStream = socket.getOutputStream()
    private val goban = Array(3) {
        Array<Stone?>(3) {
            null
        }
    }

    fun end(): GameResult? {
        inputStream.close()
        outputStream.close()
        socket.close()
        return checkGoban()
    }

    suspend fun peerTurn(): Boolean {
        val data = withContext(Dispatchers.IO) {
            inputStream.readNBytes(10)
        }.decodeToString()
        if (data.first() == 'q') {
            return true
        }
        val y = data[0] - 'a'
        val x = data[1] - '1'
        goban[y][x] = Stone.Peer

        val state = checkGoban()

        when (state) {
            PeerWin -> {
                return true
            }

            Draw -> {
                return true
            }

            else -> {}
        }

        return false
    }

    suspend fun myTurn(): Boolean {

        val data = ByteArray(10)

        val (_, pos) = goban.flatMapIndexed { index: Int, stones: Array<Stone?> ->
            stones.mapIndexed { index2, stone ->
                stone to (index to index2)
            }
        }.filter { (stone, _) ->
            stone == null
        }.random()
        val (y, x) = pos



        goban[y][x] = Stone.My
        data[0] = ('a'.code + y).toByte()
        data[1] = ('1'.code + x).toByte()
        withContext(Dispatchers.IO) {
            outputStream.write(data)
        }

        val state = checkGoban()

        when (state) {
            MyWin -> {
                return true
            }

            Draw -> {
                return true
            }

            else -> {}
        }

        return false
    }

    private fun checkGoban(): GameResult? {

        var peerCount = 0
        var myCount = 0
        //ある行を調べる
        for (i in 0..2) {
            peerCount = 0
            myCount = 0
            for (x in 0..2) {
                if (goban[i][x] == Stone.Peer) {
                    peerCount++
                } else if (goban[i][x] == Stone.My) {
                    myCount++
                }
            }
            if (peerCount == 3) {
                return PeerWin
            }
            if (myCount == 3) {
                return MyWin
            }
        }
        //ある列を調べる
        for (i in 0..2) {
            peerCount = 0
            myCount = 0
            for (y in 0..2) {
                if (goban[y][i] == Stone.Peer) {
                    peerCount++
                } else if (goban[y][i] == Stone.My) {
                    myCount++
                }
            }
            if (peerCount == 3) {
                return PeerWin
            }
            if (myCount == 3) {
                return MyWin
            }
        }
        //斜めを調べる 左上→右下
        peerCount = 0
        myCount = 0
        for (i in 0..2) {
            if (goban[i][i] == Stone.Peer) {
                peerCount++
            }
            if (goban[i][i] == Stone.My) {
                myCount++
            }
        }
        if (peerCount == 3) {
            return PeerWin
        }
        if (myCount == 3) {
            return MyWin
        }

        //斜めを調べる 右上→左下
        peerCount = 0
        myCount = 0
        for (i in 0..2) {
            if (goban[i][2 - i] == Stone.Peer) {
                peerCount++
            }
            if (goban[i][2 - i] == Stone.My) {
                myCount++
            }
        }
        if (peerCount == 3) {
            return PeerWin
        }
        if (myCount == 3) {
            return MyWin
        }

        //全て置かれたか調べる
        if (goban.flatten().all { it != null }) {
            return Draw
        }
        return null
    }
}