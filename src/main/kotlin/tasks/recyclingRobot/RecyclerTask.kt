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
    override val rla = MDPRLA(discountFactor = 0.9, state = RecyclerState(2, 0.0), task = this)
    private var searchCount = 0
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
                RecyclerState(newLevel, 3.0) // Battery reduced
            } else {
                result = RecyclerResult.OutOfBattery
                RecyclerState(2, -5.0) // Ran out of battery
            }
        } else {
            searchCount++
            RecyclerState(state.value, 3.0) // Battery not reduced
        }
    }

    fun waitAction(state: RecyclerState): RecyclerState {
        when (state.value) {
            2 -> result = RecyclerResult.HighWait
            1 -> result = RecyclerResult.LowWait
        }
        return RecyclerState(state.value, 1.0)
    }

    fun rechargeAction(state: RecyclerState): RecyclerState {
        when (state.value) {
            1 -> result = RecyclerResult.LowRecharge
        }
        searchCount = 0
        return RecyclerState(2, 0.0)
    }

    override fun start() {
        println("Run for how many steps? (Default: 1000)")
        val stepsInput = readLine()
        val steps = if(stepsInput == "") {
            1000
        } else {
            stepsInput!!.toInt()
        }
        println("Running...")
        runTask(false, steps)

        println("Explore / Improve target policy / Exploit: [e/i/x]")
        println("Press any other key to exit")

        var acceptInput = true

        while(acceptInput) when(readLine()!!.toUpperCase()) {
            "E" -> runTask(false)
            "I" -> rla.improvePolicy()
            "X" -> runTask(true)
            else -> acceptInput = false
        }
    }

    private fun runTask(exploit: Boolean, steps: Int = 1000) {
        var highSearchCount = 0
        var highWaitCount = 0
        var lowSearchCount = 0
        var lowWaitCount = 0
        var outOfBatteryCount = 0
        var highRechargeCount = 0
        var lowRechargeCount = 0

        for(i in 0 until steps) {
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
        }

        println("Result:")
        println("On high battery level:\n " +
                "searched $highSearchCount (${(highSearchCount.toDouble() / steps.toDouble()) * 100}%) times, " +
                "waited $highWaitCount times")
        println("Spent ${((highSearchCount + highWaitCount).toDouble() / steps.toDouble()) * 100}% of the time in high battery state")
        println("On low battery level:\n " +
                "searched $lowSearchCount (${(lowSearchCount.toDouble() / steps.toDouble()) * 100}%) times, " +
                "waited $lowWaitCount times, " +
                "recharged $lowRechargeCount times")
        println("Spent ${((lowRechargeCount + lowSearchCount + lowWaitCount).toDouble() / steps.toDouble()) * 100}% of the time in low battery state")
        println("Ran out of battery $outOfBatteryCount times")
    }
}