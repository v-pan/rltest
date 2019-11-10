import kotlin.reflect.KFunction

interface Task {
    val actionTable: HashMap<Any, ArrayList<KFunction<State>>> // TODO: Replace with value -> action map? Create Value interface?
    val rla: MDPRLA

    fun start()
}