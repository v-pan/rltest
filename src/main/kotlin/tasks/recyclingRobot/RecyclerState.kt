package tasks.recyclingRobot

import Action
import data.State

class RecyclerState(energyLevel: Int, override val reward: Double, override val terminal: Boolean = false) : State() {
    override val value = energyLevel

    override fun getActions(): List<Action> {
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