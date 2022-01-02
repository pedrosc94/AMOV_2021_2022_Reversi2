package com.amov.reversi

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

const val SERVER_PORT = 9999

class GameOnline : AppCompatActivity() {
    enum class State {
        STARTING,  PLAYING_OTHER, GAME_OVER
    }

    enum class ConnectionState {
        SETTING_PARAMETERS, SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED,
        CONNECTION_ERROR, CONNECTION_ENDED
    }

    private var board = arrayOf(CharArray(8), CharArray(8), CharArray(8), CharArray(8), CharArray(8), CharArray(8), CharArray(8),CharArray(8))
    private var possiblePlays = ArrayList<PossiblePlay>()

    var player1 = Player('B')
    var player2 = Player('W')

    private var dlg: AlertDialog? = null
    val state = MutableLiveData(State.STARTING)
    val connectionState = MutableLiveData(ConnectionState.SETTING_PARAMETERS)
    private var socket: Socket? = null
    private val socketI: InputStream? get() = socket?.getInputStream()
    private val socketO: OutputStream? get() = socket?.getOutputStream()

    private var serverSocket: ServerSocket? = null

    private var threadComm: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_online)

        connectionState.observe(this) { state ->
            setUI()

            if ( state != ConnectionState.SETTING_PARAMETERS && state != ConnectionState.SERVER_CONNECTING && dlg?.isShowing == true) {
                dlg?.dismiss()
                dlg = null
            }

            if (state == ConnectionState.CONNECTION_ERROR) {
                finish()
            }
            if (state == ConnectionState.CONNECTION_ENDED)
                finish()
        }

        when (intent.getIntExtra("mode", 0)) {
            0 -> startAsServer()
            1 -> startAsClient()
        }
    }

    private fun setUI() {
        player1.turnPlaying = true

        for(x in 0..7)
        {
            for(y in 0..7)
            {
                board[x][y] = ' '
            }
        }

        setPeace(3, 4, R.id.Pos34, R.drawable.black_peace, 'B')
        setPeace(4, 3, R.id.Pos43, R.drawable.black_peace, 'B')
        setPeace(4, 4, R.id.Pos33, R.drawable.white_peace, 'W')
        setPeace(3, 3, R.id.Pos44, R.drawable.white_peace, 'W')

        var arrayOfPeacesToFlip23 = ArrayList<String>()
        arrayOfPeacesToFlip23.add("33")
        setPossiblePlay(2, 3, R.id.Pos23, arrayOfPeacesToFlip23)

        var arrayOfPeacesToFlip32 = ArrayList<String>()
        arrayOfPeacesToFlip32.add("33")
        setPossiblePlay(3, 2, R.id.Pos32, arrayOfPeacesToFlip32)

        var arrayOfPeacesToFlip45 = ArrayList<String>()
        arrayOfPeacesToFlip45.add("44")
        setPossiblePlay(4, 5, R.id.Pos45, arrayOfPeacesToFlip45)

        var arrayOfPeacesToFlip54 = ArrayList<String>()
        arrayOfPeacesToFlip54.add("44")
        setPossiblePlay(5, 4, R.id.Pos54,arrayOfPeacesToFlip54)
    }

    private fun setPossiblePlay(positionX: Int, positionY: Int, peaceImageId: Int, arrayOfPeacesToFlip: ArrayList<String>){
        setPeace(positionX, positionY, peaceImageId, R.drawable.possible_play, '*')

        var possiblePlay = PossiblePlay()
        possiblePlay.Pos = positionX.toString() + positionY.toString()
        possiblePlay.arrayToFlip = arrayOfPeacesToFlip
        possiblePlays.add(possiblePlay)
    }

    private fun setPeaceByView(v: View, positionX: Int, positionY: Int, peaceImageId: Int, peaceChar: Char){
        var id = getIdByPositions(v, positionX, positionY)
        setPeace(positionX, positionY, id, peaceImageId, peaceChar)
    }

    private fun setPeace(positionX: Int, positionY: Int, id: Int, peaceImageId: Int, peaceChar: Char){
        var peace: ImageButton = findViewById(id)
        peace.setImageResource(peaceImageId)
        board[positionX][positionY] = peaceChar
    }

    private fun getIdByPositions(v: View, positionX: Int, positionY: Int): Int {
        return v.resources.getIdentifier("Pos$positionX$positionY", "id", packageName)
    }

    //#region Server
    private fun startAsServer() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress // Deprecated in API Level 31. Suggestion NetworkCallback
        val strIPAddress =  String.format("%d.%d.%d.%d",ip and 0xff,(ip shr 8) and 0xff,(ip shr 16) and 0xff,(ip shr 24) and 0xff)

        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)

            layoutParams = params

            setBackgroundColor(Color.rgb(240, 224, 208))

            orientation = LinearLayout.HORIZONTAL

            addView(ProgressBar(context).apply {
                isIndeterminate = true
                val paramsPB = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                paramsPB.gravity = Gravity.CENTER_VERTICAL
                layoutParams = paramsPB
                indeterminateTintList = ColorStateList.valueOf(Color.rgb(10, 255, 10))
            })

            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = paramsTV
                text = "Server IP address: $strIPAddress\nWaiting for a client..."
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }

        dlg = AlertDialog.Builder(this).run {
            setTitle("Server mode")
            setView(ll)
            setOnCancelListener {
                stopServer()
                finish()
            }
            create()
        }

        startServer()

        dlg?.show()
    }

    fun startServer() {
        if (serverSocket != null ||socket != null)
            return

        connectionState.postValue(ConnectionState.SERVER_CONNECTING)

        thread {
            serverSocket = ServerSocket(SERVER_PORT)
            serverSocket?.apply {
                try {
                    startCommunication(serverSocket!!.accept())
                } catch (_: Exception) {
                    connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                } finally {
                    serverSocket?.close()
                    serverSocket = null
                }
            }
        }
    }

    fun stopServer() {
        serverSocket?.close()
        connectionState.postValue(ConnectionState.CONNECTION_ENDED)
        serverSocket = null
    }
    //#endregion

    //#region Client
    private fun startAsClient() {
        val edtBox = EditText(this).apply {
            maxLines = 1
            filters = arrayOf(object : InputFilter {
                override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                    source?.run {
                        var ret = ""
                        forEach {
                            if (it.isDigit() || it == '.')
                                ret += it
                        }
                        return ret
                    }
                    return null
                }

            })
        }

        val dlg = AlertDialog.Builder(this).run {
            setTitle("Client Mode")
            setMessage("What Ip are you trying to connect?")
            setPositiveButton("Connect") { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()

                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    finish()
                } else {
                    startClient(strIP)
                }
            }

            setNegativeButton("Cancelar") { _: DialogInterface, _: Int ->
                finish()
            }

            setCancelable(false)
            setView(edtBox)
            create()
        }
        dlg.show()
    }

    fun startClient(serverIP: String,serverPort: Int = SERVER_PORT) {
        if (socket != null || connectionState.value != ConnectionState.SETTING_PARAMETERS)
            return

        thread {
            connectionState.postValue(ConnectionState.CLIENT_CONNECTING)
            try {
                val newsocket = Socket()
                newsocket.connect(InetSocketAddress(serverIP,serverPort),5000)
                startCommunication(newsocket)
            } catch (_: Exception) {
                connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                stopGame()
            }
        }
    }

    fun sendPositions(view: View, positions: String) {
        if (connectionState.value != ConnectionState.CONNECTION_ESTABLISHED)
            return

        socketO?.run {
            thread {
                try {
                    val printStream = PrintStream(this)
                    printStream.println(positions)
                    printStream.flush()
                } catch (_: Exception) {
                    stopGame()
                }
            }
        }
        state.postValue(State.PLAYING_OTHER)
    }
    //#endregion

    private fun startCommunication(newSocket: Socket) {
        if (threadComm != null)
            return

        socket = newSocket

        threadComm = thread {
            try {
                if (socketI == null)
                    return@thread

                connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
                val bufI = socketI!!.bufferedReader()
                Log.i("ERROR","@2")

                while (state.value != State.GAME_OVER) {
                    val positions = bufI.readLine()

                //makeLogic(view, positions)
                }
            } catch (_: Exception) {
            } finally {
                Log.i("ERROR","@3")

                stopGame()
            }
        }
    }

    fun stopGame() {
        try {
            state.postValue(State.GAME_OVER)
            connectionState.postValue(ConnectionState.CONNECTION_ERROR)
            socket?.close()
            socket = null
            threadComm?.interrupt()
            threadComm = null
        } catch (_: Exception) { }
    }

    fun onPlay(view: View) {
        val positions = view.resources.getResourceName(view.id).replace("com.amov.reversi:id/Pos", "")

        sendPositions(view, positions)
        makeLogic(view, positions)
    }

    fun makeLogic(v: View, positions: String) {
        val positionX = positions.take(1).toInt()
        val positionY = positions.takeLast(1).toInt()

        var player = getCurrentPlayer()

        if(!validMove(positionX, positionY)){
            return
        }

        makeMove(v, positionX, positionY)

        setPlayerTurnPlaying()
        cleanValidPlays(v)
        calculateValidPlays(v)
        updateScore(v)

        if(checkIfGameEnded()){
            showDialogMsg("END",this,this)
        }
    }

    private fun checkIfGameEnded(): Boolean {
        var possiblePlayCounter = 0
        var emptyCellCounter = 0

        for(x in 0..7)
        {
            for(y in 0..7)
            {
                if(board[x][y] == '*')
                {
                    possiblePlayCounter++;
                }

                if(board[x][y] == ' ')
                {
                    emptyCellCounter++
                }
            }
        }

        if(possiblePlayCounter == 0){
            return true
        }

        if(emptyCellCounter == 0){
            return true
        }

        return false
    }

    private fun updateScore(v: View) {
        updateScoreByPlayer(v, "scorePlayer1", 'B')
        updateScoreByPlayer(v, "scorePlayer2", 'W')
    }

    private fun updateScoreByPlayer(v: View, textViewId: String, playerChar: Char) {
        var id = v.resources.getIdentifier(textViewId, "id", packageName)
        var score :TextView = findViewById(id)

        score.text = getNumberOfPeaces(playerChar).toString()
    }

    private fun getNumberOfPeaces(char: Char): Int {
        var numberOfPeaces = 0

        for(x in 0..7)
        {
            for(y in 0..7)
            {
                if(board[x][y] == char)
                {
                    numberOfPeaces++
                }
            }
        }

        return numberOfPeaces
    }

    private fun calculateValidPlays(v: View){
        for(x in 0..7)
        {
            for(y in 0..7)
            {
                if(board[x][y] == ' ')
                {
                    var possiblePlay = PossiblePlay()
                    possiblePlay.Pos = (x.toString() + y.toString())

                    var validTop = calculateTopValidPlay(x, y, possiblePlay)
                    var validRight = calculateRightValidPlay(x, y, possiblePlay)
                    var validBottom = calculateBottomValidPlay(x, y, possiblePlay)
                    var validLeft = calculateLeftValidPlay(x, y, possiblePlay)

                    var validTopRight = calculateTopRightValidPlay(x, y, possiblePlay)
                    var validBottomRight = calculateBottomRightValidPlay(x, y, possiblePlay)
                    var validBottomLeft = calculateBottomLeftValidPlay(x, y, possiblePlay)
                    var validTopLeft = calculateTopLeftValidPlay(x, y, possiblePlay)

                    if(validTop || validRight || validBottom || validLeft || validTopRight || validBottomRight || validBottomLeft || validTopLeft)
                    {
                        setPeace(x, y, getIdByPositions(v, x, y), R.drawable.possible_play,'*')
                    }
                }
            }
        }
    }

    private fun getNotCurrentPlayer(): Player {
        if(player1.turnPlaying){
            return player2
        }

        return player1
    }

    private fun calculateTopValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x-1, y) || checkBoundries(x-2, y)){
            return false
        }

        if(board[x-1][y] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x-1).toString() + (y).toString())

        for(i in x-2 downTo 0)
        {
            if(checkBoundries(i, y) || board[i][y] == ' ' || board[i][y] == '*') {
                return false
            }

            if(board[i][y] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            auxArrayToFlip.add((i).toString() + (y).toString())
        }

        return false
    }

    private fun calculateRightValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x, y + 1) || checkBoundries(x, y + 2)){
            return false
        }

        if(board[x][y+1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x).toString() + (y + 1).toString())

        for(i in y+2 until 7)
        {
            if(checkBoundries(x, i) || board[x][i] == ' '|| board[x][i] == '*') {
                return false
            }

            if(board[x][i] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            auxArrayToFlip.add((x).toString() + (i).toString())
        }

        return false
    }

    private fun calculateBottomValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x + 1, y) || checkBoundries(x + 2, y)){
            return false
        }

        if(board[x+1][y] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x+1).toString() + (y).toString())

        for(i in x+2 until 7)
        {
            if(checkBoundries(i, y) || board[i][y] == ' ' || board[i][y] == '*') {
                return false
            }

            if(board[i][y] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            possiblePlay.arrayToFlip.add((i).toString() + (y).toString())
        }

        return false
    }

    private fun calculateLeftValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x, y-1) || checkBoundries(x, y-2)){
            return false
        }

        if(board[x][y-1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x).toString() + (y - 1).toString())

        for(i in y-2 downTo 0)
        {
            if(checkBoundries(x, i) || board[x][i] == ' ' || board[x][i] == '*') {
                return false
            }

            if(board[x][i] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            auxArrayToFlip.add((x).toString() + (i).toString())
        }

        return false
    }

    private fun calculateBottomRightValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x-1, y-1) || checkBoundries(x-2, y-2)){
            return false
        }

        if(board[x-1][y-1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x-1).toString() + (y - 1).toString())

        var auxY = y - 2

        for(i in x-2 downTo 0)
        {
            if(checkBoundries(i, auxY) || board[i][auxY] == ' ' || board[i][auxY] == '*') {
                return false
            }

            if(board[i][auxY] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            possiblePlay.arrayToFlip.add((i).toString() + (auxY).toString())
            auxY--
        }

        return false
    }

    private fun calculateBottomLeftValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x-1, y+1) || checkBoundries(x-2, y+2)){
            return false
        }

        if(board[x-1][y+1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x - 1).toString() + (y + 1).toString())
        var auxY = y + 2

        for(i in x-2 downTo 0)
        {
            if(checkBoundries(i, auxY) || board[i][auxY] == ' ' || board[i][auxY] == '*') {
                return false
            }

            if(board[i][auxY] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            possiblePlay.arrayToFlip.add((i).toString() + (auxY).toString())
            auxY++
        }

        return false
    }

    private fun calculateTopRightValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x+1, y-1) || checkBoundries(x+2, y-2)){
            return false
        }

        if(board[x+1][y-1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x + 1).toString() + (y - 1).toString())
        var auxY = y -2

        for(i in x+2 until 7)
        {
            if(checkBoundries(i, auxY) || board[i][auxY] == ' ' || board[i][auxY] == '*') {
                return false
            }

            if(board[i][auxY] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            possiblePlay.arrayToFlip.add((i).toString() + (auxY).toString())
            auxY--
        }

        return false
    }

    private fun calculateTopLeftValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x+1, y+1) || checkBoundries(x+2, y+2)){
            return false
        }

        if(board[x+1][y+1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x + 1).toString() + (y + 1).toString())

        var auxY = y +2

        for(i in x+2 until 7)
        {
            if(checkBoundries(i, auxY) || board[i][auxY] == ' ' || board[i][auxY] == '*') {
                return false
            }

            if(board[i][auxY] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            possiblePlay.arrayToFlip.add((i).toString() + (auxY).toString())
            auxY++
        }

        return false
    }

    private fun checkBoundries(x: Int, y: Int): Boolean {
        if((x < 0) || (y < 0) || (x > 7) || (y > 7)){
            return true
        }

        return false
    }

    private fun cleanValidPlays(v: View){
        for(x in 0..7)
        {
            for(y in 0..7)
            {
                if(board[x][y] == '*')
                {
                    setPeaceByView(v, x, y, R.drawable.cell, ' ')
                }
            }
        }

        possiblePlays.clear()
    }

    private fun validMove(positionX: Int, positionY: Int): Boolean {
        if(board[positionX][positionY] != '*'){
            return false
        }

        return true
    }

    private fun setPlayerTurnPlaying() {
        if(player1.turnPlaying) {
            player1.turnPlaying = false

            player2.turnPlaying = true
            findViewById<TextView>(R.id.textViewPlayer2).setBackgroundColor(Color.parseColor("#EC8E00"))
            findViewById<TextView>(R.id.textViewPlayer1).setBackgroundColor(Color.TRANSPARENT)
        } else {
            player1.turnPlaying = true
            player2.turnPlaying = false
            findViewById<TextView>(R.id.textViewPlayer1).setBackgroundColor(Color.parseColor("#EC8E00"))
            findViewById<TextView>(R.id.textViewPlayer2).setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun getCurrentPlayer(): Player {
        if(player1.turnPlaying) return player1

        return player2
    }

    private fun makeMove(v: View, positionX: Int, positionY: Int){
        var peaceImageId = if(player1.turnPlaying) R.drawable.black_peace else R.drawable.white_peace
        var playerChar = if(player1.turnPlaying) player1.char else player2.char

        move(v, positionX, positionY, peaceImageId)
        setPeaceByView(v, positionX, positionY, peaceImageId, playerChar)
    }

    private fun move(v: View, positionX: Int, positionY: Int, peaceImageId: Int){
        var peacesToFlip = getPossiblePlay(positionX, positionY)
        var playerChar = getCurrentPlayer().char

        for(peaceToFlip in peacesToFlip){
            val x = peaceToFlip.take(1).toInt()
            val y = peaceToFlip.takeLast(1).toInt()

            setPeaceByView(v, x, y, peaceImageId, playerChar)
        }
    }

    private fun getPossiblePlay(positionX: Int, positionY: Int): ArrayList<String> {
        var peacesToFlip = ArrayList<String>()

        for(possiblePlay in possiblePlays) {
            if (possiblePlay.Pos == (positionX.toString() + positionY.toString())) {
                peacesToFlip.addAll(possiblePlay.arrayToFlip)
            }
        }

        return peacesToFlip
    }
}