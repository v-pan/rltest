import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KFunction

class MDPRLA(
    private val discountFactor: Double,
    private var state: State,
    private var actions: ArrayList<KFunction<State>> = state.getActions()
) {

    private val policy: HashMap<State, MutableMap<KFunction<State>, Double>> = HashMap()

    val stateValues = mutableMapOf<State, Double>()
    private val stateActionGraph = mutableMapOf<StateActionNode, Int>()

    private fun timeStep(action: KFunction<State>?) {
        if (action != null) {
            val nextState = action.call(state)
            val node = StateActionNode(state, action, nextState)
            val totalOccurrences = stateActionGraph.values.sum()


            stateActionGraph.putIfAbsent(node, 0)
            stateActionGraph[node] = stateActionGraph[node]!! + 1

            if(totalOccurrences != 0) {
                if (stateValues[nextState] != null) {
                    println("(${(stateActionGraph[node]!!.toDouble() / totalOccurrences.toDouble())}) * (${state.reward} + ($discountFactor * ${stateValues[nextState]}))")
                    stateValues[state] = (stateActionGraph[node]!!.toDouble() / totalOccurrences.toDouble()) * state.reward + (discountFactor * stateValues[nextState]!!)
                } else {
                    println("(${(stateActionGraph[node]!!.toDouble() / totalOccurrences.toDouble())}) * (${state.reward} + ($discountFactor * ${stateValues[nextState]}))")
                    stateValues[state] = (stateActionGraph[node]!!.toDouble() / totalOccurrences.toDouble()) * (state.reward + (discountFactor * nextState.reward)) // policy[state]!![action]!! *
                }
            }

            state = nextState
            actions = nextState.getActions()
        }
    }

    fun chooseNextAction() {
        val random = Random()
        var r = random.nextDouble()

        if (policy[state] == null) {
            policy[state] = mutableMapOf()
            val normalProbability = 1 / actions.size.toDouble()
            actions.forEach {
                policy[state]!![it] = normalProbability
            }
        }

        for (pair in policy[state]!!) {
            r -= pair.value
            if (r <= 0) {
                timeStep(pair.key)
                break
            }
        }
    }

    fun stateActionValues(): MutableMap<StateActionNode, Double> {
        val totalOccurrences = stateActionGraph.values.sum()

        return stateActionGraph.map { (stateActionNode, occurrences) ->
            if(stateValues[stateActionNode.nextState] == null) {
//                stateActionNode to 0.0
                throw KotlinNullPointerException()
            } else {
                stateActionNode to (occurrences.toDouble() / totalOccurrences.toDouble()) * stateValues[stateActionNode.nextState]!!
            }
        }.toMap().toMutableMap()
    }

}