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

        return if(p < 0.3) {
            RecyclerState(state.value as Int, 2.0, this) // Battery not reduced
        } else {
            val newLevel = state.value as Int - 1
            if(newLevel > 0) {
                RecyclerState(newLevel, 2.0, this) // Battery reduced
            } else {
                RecyclerState(2, -3.0, this) // Ran out of battery
            }
        }
    }

    fun waitAction(state: RecyclerState): RecyclerState {
        return RecyclerState(state.value as Int, 1.0, this)
    }

    fun rechargeAction(state: RecyclerState): RecyclerState {
        return RecyclerState(2, 0.0, this)
    }

    override fun start() {
        println("Run for how many steps?")
        val steps = readLine()!!.toInt()
        println("Running...")
        for(i in 0 until steps) {
            rla.chooseNextAction()
        }
        println("Getting return...")
        rla.printReturn()
        rla.printReturns()
//        println(rla.calculateReturns(listOf(2.0, 2.0, 2.0, 2.0)).joinToString())
    }
}