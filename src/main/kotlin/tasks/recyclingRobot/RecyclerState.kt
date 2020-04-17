package tasks.recyclingRobot

import data.State
import kotlin.reflect.KFunction

class RecyclerState(energyLevel: Int, override val reward: Double) : State() {
    override val value = energyLevel

    override fun getActions(): List<KFunction<State>> {
        return when(value) {
            0 -> listOf()
            1 -> listOf(RecyclerTask::searchAction, RecyclerTask::waitAction, RecyclerTask::rechargeAction)
            2 -> listOf(RecyclerTask::searchAction, RecyclerTask::waitAction)
            else -> throw Error("No actions for state!")
        }
    }

    override fun toString(): String {
        return "Energy level: $value"
    }
}