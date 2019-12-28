import kotlin.reflect.KFunction

data class StateActionNode(val state: State, val action: KFunction<State>, val nextState: State) {
    override fun equals(other: Any?): Boolean {
        return if(other is StateActionNode) {
            state == other.state && action === other.action && nextState == nextState
        } else {

            false
        }
    }

    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + action.hashCode()
        result = 31 * result + nextState.hashCode()
        return result
    }

    override fun toString(): String {
        return "StateActionNode(state=$state, action=${action.name}, nextState=$nextState)"
    }
}