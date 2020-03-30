import java.lang.Error
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.reflect.KFunction

typealias StateValue = Any

class MDPRLA(
    private val discountFactor: Double,
    var state: State,
    private val task: Task,
    private var actions: List<KFunction<State>> = state.getActions(),
    val policy: MutableMap<StateValue, MutableMap<KFunction<State>, Double>> = mutableMapOf()
) {

    private val experiences: MutableList<Pair<StateValue, Pair<KFunction<State>, Double>>> = mutableListOf() // Chronological list of experiences, and rewards
    private var rewardValues = mutableListOf<Double>()

    private fun timeStep(action: KFunction<State>?) {
        if(action != null) {
            val nextState = action.call(task, state)

            experiences.add(state.value to (action to nextState.reward))

            state = nextState
            actions = state.getActions()

            rewardValues.add(nextState.reward)
        }
    }

    fun chooseAction(stateValue: StateValue = state.value): KFunction<State>? {
        if(stateValue is State) {
            throw Error("State passed to chooseAction")
        }

        var r = Random().nextDouble()

        if(policy[stateValue] == null) {
            policy[state.value] = mutableMapOf()
            val normalProbability = 1 / actions.size.toDouble()
            actions.forEach {
                policy[state.value]!![it] = normalProbability
            }
        }

        for (pair in policy[stateValue]!!) {
            r -= pair.value
            if(r <= 0) {
                return pair.key
            }
        }
        return null
    }

    fun act() = timeStep(chooseAction(this.state.value))

    fun mapStateToValue(episode: List<Pair<StateValue, Pair<KFunction<State>, Double>>> = experiences): Map<StateValue, Double> {
        var returnValue = 0.0
        val returns: MutableMap<StateValue, MutableList<Double>> = mutableMapOf()

        return episode.reversed().forEachIndexed { index, (stateValue, actionPair) ->
            val (action, reward) = actionPair
            returnValue = discountFactor.pow(index) * returnValue + reward

            returns[stateValue]?.add(returnValue) ?: returns.put(stateValue, mutableListOf(returnValue))
        }.let { returns.map { (stateValue, returns) -> stateValue to returns.average() }.toMap() }
    }
}