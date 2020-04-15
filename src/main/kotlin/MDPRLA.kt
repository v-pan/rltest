import data.*
import java.util.*
import kotlin.math.pow
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

    fun act() = timeStep(chooseAction(state, behaviourPolicy))
    fun improvePolicy(temperature: Double = 1.0) {
        targetPolicy = experiences.toStateActionWeightedValueMap(
            discountFactor,
            targetPolicy = targetPolicy ?: behaviourPolicy,
            basePolicy = behaviourPolicy
        ).asStateActionDoubleMap().toPolicy(temperature)

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

    private fun chooseAction(state: State, behaviourPolicy: Policy): Action? {
        return if(behaviourPolicy[state.value] == null) {
            val total = actions.size
            behaviourPolicy[state.value] = actions.map {
                it to (1.0 / total.toDouble())
            }.toMap().toMutableMap()

            actions.random()
        } else {
            var r = Random().nextDouble()
            var action: Action? = null

            for (pair in behaviourPolicy[state.value]!!) {
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