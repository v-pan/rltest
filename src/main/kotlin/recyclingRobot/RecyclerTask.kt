package recyclingRobot

import Task
import State
import MDPRLA
import kotlin.random.Random
import kotlin.reflect.KFunction

class RecyclerTask : Task {
    private val actionTable: HashMap<Int, List<KFunction<State>>> = hashMapOf(
        Pair(0, listOf()),
        Pair(1, listOf<KFunction<State>>(::searchAction, ::waitAction, ::rechargeAction)),
        Pair(2, listOf<KFunction<State>>(::searchAction, ::waitAction))
    )

    private val random = Random     // For simulating environment dynamics

    override val rla = MDPRLA(discountFactor = 0.9, state = RecyclerState(2, 0.0, this))

    override fun getActions(state: State): List<KFunction<State>> {
        return actionTable[state.value] ?: throw IndexOutOfBoundsException()
    }

    fun searchAction(state: RecyclerState): RecyclerState {
        val p = random.nextDouble()

        return if(p < 0.3) {
//            println("Battery level not reduced ${state.value}, reward: 2.0\n")
            RecyclerState(state.value as Int, 2.0, this)
        } else {
            val newLevel = state.value as Int - 1
            if(newLevel > 0) {
//                println("Battery level reduced ${state.value as Int - 1}, reward: 2.0\n")
                RecyclerState(newLevel, 2.0, this)
            } else {
//                println("Battery level ran out, reward: -3.0\n")
                RecyclerState(2, -3.0, this)
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
            println("Run for how many steps? (Default: 1000000)")
            val stepsInput = readLine()
            val steps = if(stepsInput == "") {
                1000000
            } else {
                stepsInput!!.toInt()
            }
            println("Running...")
            for(i in 0 until steps) {
                rla.chooseNextAction()
            }

            println(rla.stateValues)
            println(rla.stateActionValues())

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