package recyclingRobot

import State
import Task
import java.util.ArrayList
import kotlin.reflect.KFunction

class RecyclerState(energyLevel: Int, override val reward: Double, override val task: RecyclerTask) : State() {
    override var value: Any = energyLevel

    override fun getActions(): ArrayList<KFunction<State>> {
        return task.actionTable[value as Int]!!
    }

    override fun toString(): String {
        return "Energy level: $value"
    }
}