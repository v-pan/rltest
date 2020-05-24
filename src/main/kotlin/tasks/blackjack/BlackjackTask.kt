package tasks.blackjack

import MDPRLA
import data.Task

class BlackjackTask : Task {
    override var rla = MDPRLA(0.5, BlackjackState(listOf((1 .. 13).random(), (1 .. 13).random()), 0.0, false), this)
    private var under = false
    private var bust = false
    private var won = false

    fun hit(state: BlackjackState): BlackjackState {
        val newState = BlackjackState(state.cardList.plus((1..13).random()), 0.0, false)
        return if(newState.totalCardValue() > 21) {
            bust = true
            BlackjackState(listOf((1 .. 13).random(), (1 .. 13).random()), -1.0, terminal = true)
        } else {
            newState
        }
    }

    fun stick(state: BlackjackState): BlackjackState {
        return if(state.totalCardValue() > 17) {
            won = true
            BlackjackState(listOf((1 .. 13).random(), (1 .. 13).random()), 1.0, terminal = true)
        } else {
            under = true
            BlackjackState(listOf((1 .. 13).random(), (1 .. 13).random()), -1.0, terminal = true)
        }
    }

    fun start() {
        var winCount = 0
        var bustCount = 0
        var underCount = 0

        val defaultGames = 1000

        println("Play how many games / episodes? (default: $defaultGames)")
        val input = readLine()
        val games = if(input == "") {
            defaultGames
        } else {
            input!!.toInt()
        }

        println("Playing...")

        for(i in 0 until games) {
            var gameOver = false
            while(!gameOver) {
                rla.explore()
                when {
                    bust -> {
                        bust = false
                        bustCount++
                        gameOver = true
                    }
                    under -> {
                        under = false
                        underCount++
                        gameOver = true
                    }
                    won -> {
                        won = false
                        winCount++
                        gameOver = true
                    }
                    else -> {
                        gameOver = false
                    }
                }
            }
        }

        println(bustCount + underCount + winCount)
        println("Finished round 1")
        println("Won $winCount (${(winCount.toDouble() / games.toDouble()) * 100}%), " +
                "bust $bustCount (${(bustCount.toDouble() / games.toDouble()) * 100}%), " +
                "lost $underCount (${(underCount.toDouble() / games.toDouble()) * 100}%) " +
                "of $games games")
        println("Explore / Improve target policy / Exploit: [e/i/x]")
        println("Press any other key to exit")

        var acceptInput = true

        while(acceptInput) when(readLine()!!.toUpperCase()) {
            "E" -> play(false)
//            "I" -> rla.improvePolicy()
            "X" -> play(true)
            else -> acceptInput = false
        }
    }

    private fun play(exploit: Boolean) {
        var winCount = 0
        var bustCount = 0
        var underCount = 0
        val games = 1000

        when(exploit) {
            false -> {
                println("Exploring...")
                for(i in 0 until games) {
                    var gameOver = false
                    while(!gameOver) {
                        rla.explore()
                        when {
                            bust -> { bust = false; bustCount++; gameOver = true }
                            under -> { under = false; underCount++; gameOver = true }
                            won -> { won = false; winCount++; gameOver = true }
                        }
                    }
                }
            }
            true -> {
                println("Exploiting...")
                for(i in 0 until games) {
                    var gameOver = false
                    while(!gameOver) {
                        rla.exploit()
                        when {
                            bust -> { bust = false; bustCount++; gameOver = true }
                            under -> { under = false; underCount++; gameOver = true }
                            won -> { won = false; winCount++; gameOver = true }
                        }
                    }
                }
            }
        }

        println(bustCount + underCount + winCount)
        println("Won $winCount (${(winCount.toDouble() / games.toDouble()) * 100}%), " +
                "bust $bustCount (${(bustCount.toDouble() / games.toDouble()) * 100}%), " +
                "lost $underCount (${(underCount.toDouble() / games.toDouble()) * 100}%) " +
                "of $games games")
    }
}