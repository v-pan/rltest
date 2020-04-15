package data

import MDPRLA

interface Task {
//    val actionTable: HashMap<Any, List<KFunction<State>>> // TODO: Replace with value -> action map? Create Value interface?
    val rla: MDPRLA

    fun start()
}