package recyclingRobot

import MDPRLA
import data.Task
import kotlin.random.Random

class RecyclerTask : Task {
    // For simulating environment dynamics
    private val random = Random

    override val rla = MDPRLA(discountFactor = 0.5, state = RecyclerState(2, 0.0), task = this)
    private var searchCount = 0

    fun searchAction(state: RecyclerState): RecyclerState {
        return if (searchCount == 2) {
            searchCount = 0
            val newLevel = state.value - 1

            if (newLevel > 0) {
                RecyclerState(newLevel, 2.0) // Battery reduced
            } else {
                RecyclerState(2, -3.0) // Ran out of battery
            }
        } else {
            searchCount++
            RecyclerState(state.value, 2.0) // Battery not reduced
        }
    }

    fun waitAction(state: RecyclerState): RecyclerState {
//        println("Waiting...\n Battery level ${state.value}, reward: 1.0\n")
        return RecyclerState(state.value, 1.0)
    }

    fun rechargeAction(state: RecyclerState): RecyclerState {
//        println("Recharging...\n Battery level: 2.0, reward: 0.0\n")
        searchCount = 0
        return RecyclerState(2, 0.0)
    }

    override fun start() {
        while (true) {
            println("Run for how many steps? (Default: 10000)")
            val stepsInput = readLine()
            val steps = if(stepsInput == "") {
                10000
            } else {
                stepsInput!!.toInt()
            }
            println("Running...")
            for(i in 0 until steps) {
                rla.act()
            }
//            rla.nextPolicyNormalised()

//            rla.printReturn()

            println("Keep running? Y/N")
            val continueInput = readLine()!!.toUpperCase()
            if(continueInput == "Y" || continueInput == "") {
                continue
            } else {
                break
            }
        }
    }
}