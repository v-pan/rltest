import kotlin.reflect.KFunction

data class ActionValuePair(private val action: KFunction<State>, private val reward: Double)