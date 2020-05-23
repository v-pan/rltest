package data

import Action

abstract class State {
    abstract val value: Any
    abstract val reward: Double
    abstract val terminal: Boolean

    abstract fun getActions(): List<Action>
    override fun equals(other: Any?): Boolean {
        return if(other is State) {
            value == other.value
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
