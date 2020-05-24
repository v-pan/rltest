package data

import Action
import Policy
import kotlin.math.pow

data class EpisodeEntry(val state: State, val action: Action, val reward: Double)
typealias Episode = MutableList<EpisodeEntry>

typealias OldEpisode = MutableList<Pair<State, Pair<Action, Double>>>
typealias MutableSAWVMap = MutableMap<Pair<State, Action>, SAWeightedValue>
typealias SAVDoubleMap = Map<Pair<State, Action>, Double>

fun SAVDoubleMap.toPolicy(temperature: Double): Policy {
    val e = 2.718281828459045235360287471352662497757247093699959574966

    val targetPolicy: Policy = mutableMapOf()

    forEach { (stateAction, value) ->
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

fun MutableSAWVMap.asStateActionDoubleMap(): SAVDoubleMap {
    return map { (stateAction, meanValue) ->
        stateAction to meanValue.value
    }.toMap()
}

fun MutableSAWVMap.argMaxOf(state: State): Action {
    var maxValue: Double? = null
    var argMax: Action? = null

    state.getActions().forEach {
        putIfAbsent(state to it, SAWeightedValue(0.0, 0.0))
    }

    filter { it.key.first == state }.forEach { (stateAction, weightedValue) ->
        val (_, action) = stateAction
        if(maxValue != null) {
            if(weightedValue.value > maxValue!!) {
                maxValue = weightedValue.value
                argMax = action
            }
        } else {
            maxValue = weightedValue.value
            argMax = action
        }
    }

    return argMax!!
}

fun OldEpisode.toStateActionWeightedValueMap(discountFactor: Double,
                                             targetPolicy: Policy, basePolicy: Policy,
                                             stateActionValues: MutableSAWVMap = mutableMapOf()
): MutableSAWVMap {

    var returnValue = 0.0
    var weight = 1.0
    val oldStateValues = stateActionValues.toMap()

    reversed().forEachIndexed { index, (state, actionPair) ->
        val (action, reward) = actionPair
        val oldValue = oldStateValues[state to action]?.value ?: 0.0

        // TODO: Use BigDecimal to avoid NaNs?
        returnValue = (discountFactor.pow(index) * returnValue) + reward

        stateActionValues.putIfAbsent(state to action, SAWeightedValue(0.0, 0.0))
        stateActionValues[state to action]!!.totalWeight += weight
        stateActionValues[state to action]!! += (weight * (returnValue - oldValue)) / stateActionValues[state to action]!!.totalWeight

        weight *= targetPolicy[state.value]!![action]!! / basePolicy[state.value]!![action]!!
    }

    return stateActionValues
}