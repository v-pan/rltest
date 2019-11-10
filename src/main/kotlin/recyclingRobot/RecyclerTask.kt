package recyclingRobot

import Task
import State
import MDPRLA
import kotlin.random.Random
import kotlin.reflect.KFunction

class RecyclerTask : Task {
    override val actionTable: HashMap<Any, ArrayList<KFunction<State>>> = hashMapOf(
        Pair<Int, ArrayList<KFunction<State>>>(0, ArrayList()),
        Pair<Int, ArrayList<KFunction<State>>>(1, arrayListOf(::searchAction, ::waitAction, ::rechargeAction)),
        Pair<Int, ArrayList<KFunction<State>>>(2, arrayListOf(::searchAction, ::waitAction))
    )

    // For simulating environment dynamics
    private val random = Random

    override val rla = MDPRLA(0.5, RecyclerState(2, 0.0, this))

    fun searchAction(state: RecyclerState): RecyclerState {
        val p = random.nextDouble()

//        println("Searching...")

        return if(p < 0.3) {
//            println("Battery level not reduced ${state.value}, reward: 2.0\n")

            RecyclerState(state.value as Int, 2.0, this) // Battery not reduced
        } else {
            val newLevel = state.value as Int - 1
            if(newLevel > 0) {
//                println("Battery level reduced ${state.value as Int - 1}, reward: 2.0\n")

                RecyclerState(newLevel, 2.0, this) // Battery reduced
            } else {
//                println("Battery level ran out, reward: -3.0\n")

                RecyclerState(2, -3.0, this) // Ran out of battery
            }
        }
    }

    fun waitAction(state: RecyclerState): RecyclerState {
//        println("Waiting...\n Battery level ${state.value}, reward: 1.0\n")
        return RecyclerState(state.value as Int, 1.0, this)
    }

    fun rechargeAction(state: RecyclerState): RecyclerState {
//        println("Recharging...\n Battery level: 2.0, reward: 0.0\n")
        return RecyclerState(2, 0.0, this)
    }

    override fun start() {
        while (true) {
            println("Run for how many steps? (Default: 1000)")
            val stepsInput = readLine()
            val steps = if(stepsInput == "") {
                1000
            } else {
                stepsInput!!.toInt()
            }
            println("Running...")
            for(i in 0 until steps) {
                rla.chooseNextAction()
            }

            rla.printStateValues()
            rla.printStateActionValues()
//            println(rla.calculateReturns(listOf(2.0, 2.0, 2.0, 2.0)).joinToString())

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