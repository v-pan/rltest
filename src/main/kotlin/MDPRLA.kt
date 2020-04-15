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

    private var policy: Policy = mutableMapOf()
    private val experiences: Episode = mutableListOf()

    fun act() = timeStep(chooseAction(state, policy))
    fun improvePolicy(temperature: Double = 1.0) {
        val targetPolicy = createTargetPolicy(experiences, temperature)

        policy = targetPolicy
        println("New policy:\n $targetPolicy")
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

    private fun createTargetPolicy(episode: Episode, temperature: Double): Policy {
        val stateActionValues: SAVDoubleMap =
            episode.toStateActionWeightedValueMap(discountFactor, targetPolicy = policy, basePolicy = policy)
            .asStateActionDoubleMap()

        val targetPolicy: Policy = mutableMapOf()

        // Soft-max
        val e = 2.718281828459045235360287471352662497757247093699959574966
        stateActionValues.forEach { (stateAction, value) ->
            val (state, action) = stateAction

            targetPolicy.putIfAbsent(state.value, mutableMapOf())
            targetPolicy[state.value]!![action] = (value)
        }

        return targetPolicy.map { (state, actionMap) ->
            var total = 0.0
            val finalMap = actionMap.map { (action, value) ->
                val eValue = e.pow(value / temperature)
                total += eValue
                action to eValue
            }.map { (action, eValue) ->
                action to eValue / total
            }

            state to finalMap.toMap().toMutableMap()
        }.toMap().toMutableMap()
    }
}