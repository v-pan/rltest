import recyclingRobot.RecyclerState
import java.util.ArrayList
import kotlin.reflect.KFunction

interface State {
    val task: Task
    var value: Any
    val reward: Double

    fun getActions(): ArrayList<KFunction<State>>
}
