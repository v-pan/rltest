package data

import kotlin.reflect.KFunction

abstract class State {
    abstract val value: Any
    abstract val reward: Double

    abstract fun getActions(): List<KFunction<State>>
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
