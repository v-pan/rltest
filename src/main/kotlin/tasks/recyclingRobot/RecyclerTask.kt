package tasks.recyclingRobot

import MDPRLA
import data.Task
import kotlin.random.Random

enum class RecyclerResult(value: Int) {
    HighSearch(0),
    HighWait(1),
    LowSearch(2),
    LowWait(3),
    OutOfBattery(4),
    LowRecharge(5)
}

class RecyclerTask : Task {
    override val rla = MDPRLA(discountFactor = 0.5, state = RecyclerState(2, 0.0), task = this)
    private var searchCount = 0
    private var terminal = false
    private lateinit var result: RecyclerResult

    fun searchAction(state: RecyclerState): RecyclerState {
        when (state.value) {
            2 -> result = RecyclerResult.HighSearch
            1 -> result = RecyclerResult.LowSearch
        }

        return if (searchCount == 5) {
            searchCount = 0
            val newLevel = state.value - 1

            if (newLevel > 0) {
                RecyclerState(newLevel, 3.0, terminal) // Battery reduced
            } else {
                result = RecyclerResult.OutOfBattery
                RecyclerState(1, -3.0, terminal) // Ran out of battery
            }
        } else {
            searchCount++
            RecyclerState(state.value, 3.0, terminal) // Battery not reduced
        }
    }

    fun waitAction(state: RecyclerState): RecyclerState {
        when (state.value) {
            2 -> result = RecyclerResult.HighWait
            1 -> result = RecyclerResult.LowWait
        }
        return RecyclerState(state.value, 1.0, terminal)
    }

    fun rechargeAction(state: RecyclerState): RecyclerState {
        when (state.value) {
            1 -> result = RecyclerResult.LowRecharge
        }
        searchCount = 0
        return RecyclerState(2, 0.0, terminal)
    }

    fun start() {
        println("Steps per episode? (Default: 100)")
        val stepsInput = readLine()
        val steps = if(stepsInput == "") {
            100
        } else {
            stepsInput!!.toInt()
        }

        println("Run for how many episodes? (Default: 100)")
        val episodesInput = readLine()
        val episodes = if(episodesInput == "") {
            100
        } else {
            episodesInput!!.toInt()
        }

        println("Running...")
        for(i in 0 until episodes) {
            runTask(false, steps)
        }

        rla.printTrainingResults()
        println("Explore / Improve target policy / Exploit: [e/i/x]")
        println("Press any other key to exit")

        var acceptInput = true

        while(acceptInput) {
            when(readLine()!!.toUpperCase()) {
                "E" -> runTask(false)
//            "I" -> rla.improvePolicy()
                "X" -> runTask(true)
                else -> acceptInput = false
            }
            rla.printTrainingResults()
        }
    }

    private fun runTask(exploit: Boolean, steps: Int = 100) {
        var highSearchCount = 0
        var highWaitCount = 0
        var lowSearchCount = 0
        var lowWaitCount = 0
        var outOfBatteryCount = 0
        var lowRechargeCount = 0

        for(i in 0 until steps) {
            if(i == steps-1) {
                terminal = true
            }
            when(exploit) {
                true -> {
                    rla.exploit()
                    when(result) {
                        RecyclerResult.HighSearch -> highSearchCount++
                        RecyclerResult.HighWait -> highWaitCount++
                        RecyclerResult.LowSearch -> lowSearchCount++
                        RecyclerResult.LowWait -> lowWaitCount++
                        RecyclerResult.OutOfBattery -> outOfBatteryCount++
                        RecyclerResult.LowRecharge -> lowRechargeCount++
                    }
                }
                false -> {
                    rla.explore()
                    when(result) {
                        RecyclerResult.HighSearch -> highSearchCount++
                        RecyclerResult.HighWait -> highWaitCount++
                        RecyclerResult.LowSearch -> lowSearchCount++
                        RecyclerResult.LowWait -> lowWaitCount++
                        RecyclerResult.OutOfBattery -> outOfBatteryCount++
                        RecyclerResult.LowRecharge -> lowRechargeCount++
                    }
                }
            }
            terminal = false
        }

        println("Result:")
        println("On high battery level:\n " +
                "searched $highSearchCount (${(highSearchCount.toDouble() / (highSearchCount + highWaitCount).toDouble()) * 100}%) times, " +
                "waited $highWaitCount times")
        println("Spent ${((highSearchCount + highWaitCount).toDouble() / steps.toDouble() * 100)}% of the time in high battery state")
        println("On low battery level:\n " +
                "searched $lowSearchCount (${(lowSearchCount.toDouble() / (lowRechargeCount + lowSearchCount + lowWaitCount).toDouble()) * 100}%) times, " +
                "waited $lowWaitCount times, " +
                "recharged $lowRechargeCount times")
        println("Spent ${((lowRechargeCount + lowSearchCount + lowWaitCount).toDouble() / steps.toDouble()) * 100}% of the time in low battery state")
        println("Ran out of battery $outOfBatteryCount times")
    }
}