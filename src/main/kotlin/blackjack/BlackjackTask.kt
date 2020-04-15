package blackjack

import MDPRLA
import Task

class BlackjackTask : Task {
    override var rla = MDPRLA(0.5, BlackjackState(listOf((1 .. 13).random(), (1 .. 13).random()), 0.0), this)
    private var lost = false
    private var won = false

    fun hit(state: BlackjackState): BlackjackState {
        val newState = BlackjackState(state.cardList.plus((1..13).random()), 0.0)
        return if(newState.totalCardValue() > 21) {
            lost = true
            BlackjackState(listOf((1 .. 13).random(), (1 .. 13).random()), -1.0)
        } else {
            newState
        }
    }

    fun stick(state: BlackjackState): BlackjackState {
        return if(state.totalCardValue() > 17) {
            won = true
            BlackjackState(listOf((1 .. 13).random(), (1 .. 13).random()), 1.0)
        } else {
            lost = true
            BlackjackState(listOf((1 .. 13).random(), (1 .. 13).random()), -1.0)
        }
    }

    override fun start() {
        var winCount = 0

        println("Play how many games? (default: 10000)")
        val input = readLine()
        val games = if(input == "") {
            10000
        } else {
            input!!.toInt()
        }

        println("Playing...")

        for(i in 0 until games) {
            rla.act()
            if(lost) {
              lost = false
            } else if (won) {
              winCount++
            }
        }
//        test()
//        rla.improvePolicy()
        println("Finished round 1, test result? y/n")
        if(readLine() == "y") {
            var keepTesting = true
            while(keepTesting) {
                test()
                println("Test again, or train? y/t/n")
                val ans = readLine()
                keepTesting = (ans == "y")
                if(ans == "t") {
                    rla.improvePolicy()
                    keepTesting = true
                }
            }
        } else {
            return
        }
    }

    private fun test() {
        var winCount = 0
        val games = 10000

        println("Testing...")
        for(i in 0 until games) {
            rla.act()
            if(lost) {
                lost = false
            } else if (won) {
                won = false
                winCount++
            }
        }
        println("Won $winCount (${(winCount.toDouble() / games.toDouble()) * 100}%), lost ${(games - winCount)} of $games games")
    }
}