import kotlin.reflect.KFunction

interface Task {
    val rla: MDPRLA

    fun start()
    fun getActions(state: State): List<KFunction<State>>
}