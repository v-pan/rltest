import data.*
import java.util.*
import kotlin.reflect.KFunction

typealias StateValue = Any
typealias Action = KFunction<State>
typealias Policy = MutableMap<StateValue, MutableMap<Action, Double>>

class MDPRLA(
    private val discountFactor: Double,
    private var state: State,
    private val task: Task,
    private var actions: List<Action> = state.getActions()
) {

    private var behaviourPolicy: Policy = mutableMapOf()
    private var targetPolicy: Policy? = null
    private val experiences: Episode = mutableListOf()
    private var stateActionValueMap: MutableSAWVMap = mutableMapOf()

    fun explore() = timeStep(chooseAction(state, behaviourPolicy))
    fun exploit() = when(targetPolicy){
        null -> {
            println("No target policy has been trained.")
        }
        else -> {
            timeStep(chooseAction(state, targetPolicy!!))
        }
    }

    fun improvePolicy(temperature: Double = 1.0) {
        stateActionValueMap = experiences.toStateActionWeightedValueMap(
            discountFactor,
            targetPolicy = targetPolicy ?: behaviourPolicy,
            basePolicy = behaviourPolicy,
            stateActionValues = stateActionValueMap
        )
        targetPolicy = stateActionValueMap.asStateActionDoubleMap().toPolicy(temperature)

        println("New target policy:\n ${targetPolicy!!.map { (stateValue, actionPair) -> stateValue to actionPair.map { (action, value) -> action to value }.joinToString { (action, value) -> "${action.name}=$value" } }.joinToString { (stateValue, actionString) -> "$stateValue={$actionString}" }}")
    }

    private fun timeStep(action: Action?) {
        if(action != null) {
            val nextState = action.call(task, state)

            experiences.add(state to (action to nextState.reward))

            state = nextState
            actions = state.getActions()
        }
    }

    private fun chooseAction(state: State, policy: Policy): Action? {
        return if(policy[state.value] == null) {
            val total = actions.size
            policy[state.value] = actions.map {
                it to (1.0 / total.toDouble())
            }.toMap().toMutableMap()

            actions.random()
        } else {
            var r = Random().nextDouble()
            var action: Action? = null

            for (pair in policy[state.value]!!) {
                r -= pair.value
                if(r <= 0) {
                    action = pair.key
                    break
                }
            }

            action
        }
    }
}