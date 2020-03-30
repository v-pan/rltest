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
            println("still under")
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
        println("---------------------------------------------------")
        println(rla.state.toString())
        for(i in 0 until games) {
            println("Playing...")
            rla.act()
            if(lost) {
                println("lost!")
                println("---------------------------------------------------")
                lost = false
            } else if (won) {
                println("won!")
                println("---------------------------------------------------")

                won = false
                winCount++
            }
//            rla.nextPolicyNormalised()
            if(i != games-1) {
                println(rla.state.toString())
            }
        }
        println("Won $winCount, lost ${(games - winCount)} of $games games")
        println()
        println("Policy for 20: ${rla.policy[20]}")
        if(rla.policy[20] != null) {
            println(rla.chooseAction(20)?.name)
            println(rla.chooseAction(20)?.name)
            println(rla.chooseAction(20)?.name)
        }
    }

    fun test(){

    }
}