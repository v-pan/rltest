package data

import Action
import Policy
import kotlin.math.pow

typealias Episode = MutableList<Pair<State, Pair<Action, Double>>>
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

fun Episode.toStateActionWeightedValueMap(discountFactor: Double,
                                          targetPolicy: Policy, basePolicy: Policy,
                                          stateActionValues: MutableSAWVMap = mutableMapOf()
): MutableSAWVMap {

    var returnValue = 0.0
    var weight = 1.0
    val oldStateValues = stateActionValues.toMap()

    reversed().forEachIndexed { index, (state, actionPair) ->
        val (action, reward) = actionPair
        val oldValue = oldStateValues[state to action]?.value ?: 0.0

        returnValue = discountFactor.pow(index) * returnValue + reward

//        if(targetPolicy !== basePolicy) {
        stateActionValues.putIfAbsent(state to action, SAWeightedValue(returnValue - oldValue, 0.0))
        stateActionValues[state to action]!!.totalWeight += weight
        stateActionValues[state to action]!! += (weight * (returnValue - oldValue)) / stateActionValues[state to action]!!.totalWeight

        weight *= targetPolicy[state.value]!![action]!! / basePolicy[state.value]!![action]!!
//        } else {
//            stateActionValues.putIfAbsent(state to action, SAWeightedValue(returnValue - oldValue, 1.0))
//            stateActionValues[state to action]!!.totalWeight = 1.0
//            stateActionValues[state to action]!! += (returnValue - oldValue)
//        }
    }

    return stateActionValues
}