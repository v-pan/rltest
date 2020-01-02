import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KFunction

class MDPRLA(
    private val discountFactor: Double,
    private var state: State,
    private var actions: List<KFunction<State>> = state.getActions(),
    private val estimationThreshold: Double = 0.1
) {

    private val policy: MutableMap<State, MutableMap<KFunction<State>, Double>> = HashMap()

    val stateValues = mutableMapOf<State, Double>()
    private lateinit var oldStateValues: Map<State, Double>
    private val stateActionExperiences = mutableMapOf<StateActionNode, Int>()

    private fun timeStep(action: KFunction<State>?) {
        if (action != null) {
            val nextState = action.call(state)
            val node = StateActionNode(state, action, nextState)
            val totalOccurrences = stateActionExperiences.values.sum()

            stateActionExperiences.putIfAbsent(node, 0)
            stateActionExperiences[node] = stateActionExperiences[node]!! + 1

            if (totalOccurrences != 0) {
                if (stateValues[nextState] != null) {
//                    if (stateValues[state] != null) {
//                        oldStateValues[state] = stateValues[state]!!
//                    }

                    stateValues[state] =
                        (stateActionExperiences[node]!!.toDouble() / totalOccurrences.toDouble()) * state.reward + (discountFactor * stateValues[nextState]!!)
                } else {
//                    if (stateValues[state] != null) {
//                        oldStateValues[state] = stateValues[state]!!
//                    }

                    stateValues[state] =
                        (stateActionExperiences[node]!!.toDouble() / totalOccurrences.toDouble()) * (state.reward + (discountFactor * nextState.reward))
                }
            } else {
                if (stateValues[nextState] != null) {
//                    oldStateValues[state] = stateValues[state]!!

                    stateValues[state] =
                        (state.reward + (discountFactor * stateValues[nextState]!!))
                } else {
//                    if (stateValues[state] != null) {
//                        oldStateValues[state] = stateValues[state]!!
//                    }

                    stateValues[state] =
                        (state.reward + (discountFactor * nextState.reward))
                }
            }

            state = nextState
            actions = nextState.getActions()
        }
    }

    fun chooseNextAction() {
        val random = Random()
        var r = random.nextDouble()

        if (policy[state] == null) {        // Initially choose a policy with equal probabilities for the previously unseen state
            policy[state] = mutableMapOf()
            val normalProbability = 1 / actions.size.toDouble()
            actions.forEach {
                policy[state]!![it] = normalProbability
            }
        }

        for (pair in policy[state]!!) {     // Use probabilities from the map for the given state
            r -= pair.value
            if (r <= 0) {
                timeStep(pair.key)
                break
            }
        }
    }

    fun stateActionValues(): Map<StateActionNode, Double> {
        val totalOccurrences = stateActionExperiences.values.sum()

        return stateActionExperiences.map { (node, occurrences) ->
            if (stateValues[node.nextState] != null) {
                node to (stateActionExperiences[node]!!.toDouble() / totalOccurrences.toDouble()) * stateValues[node.nextState]!!
//                stateActionNode to stateValues[stateActionNode.nextState]!!
            } else {
                node to 0.0
            }
        }.toMap()
    }
}
