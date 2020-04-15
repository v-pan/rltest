package data

import Action
import Policy
import kotlin.math.pow

typealias Episode = MutableList<Pair<State, Pair<Action, Double>>>
typealias MutableSAWVMap = MutableMap<Pair<State, Action>, SAWeightedValue>
typealias SAVDoubleMap = Map<Pair<State, Action>, Double>

fun MutableSAWVMap.asStateActionDoubleMap(): SAVDoubleMap {
    return map { (stateAction, meanValue) ->
        stateAction to meanValue.value
    }.toMap()
}

fun Episode.toStateActionWeightedValueMap(discountFactor: Double,
                                          targetPolicy: Policy, basePolicy: Policy,
                                          stateActionValues: MutableSAWVMap = mutableMapOf()
): MutableSAWVMap {

    var returnValue = 0.0
    var weight = 1.0

    reversed().forEachIndexed { index, (state, actionPair) ->
        val (action, reward) = actionPair
        val oldValue = stateActionValues[state to action]?.value ?: 0.0

        returnValue = discountFactor.pow(index) * returnValue + reward

        stateActionValues.putIfAbsent(state to action, SAWeightedValue(returnValue - oldValue, 0.0))
        stateActionValues[state to action]!!.totalWeight += weight
        stateActionValues[state to action]!! += (weight * (returnValue - oldValue)) / stateActionValues[state to action]!!.totalWeight

        weight *= targetPolicy[state]!![action]!! / basePolicy[state]!![action]!!
    }

    return stateActionValues
}