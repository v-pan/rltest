package data

import Action
import Policy
import kotlin.math.pow

typealias Episode = MutableList<Pair<State, Pair<Action, Double>>>
typealias MutableSAVMeanMap = MutableMap<Pair<State, Action>, SAValue>
typealias SAVDoubleMap = Map<Pair<State, Action>, Double>

fun MutableSAVMeanMap.asStateActionDoubleMap(): SAVDoubleMap {
    return map { (stateAction, meanValue) ->
        stateAction to meanValue.value
    }.toMap()
}

fun Episode.toStateActionMeanValueMap(discountFactor: Double,
                                      targetPolicy: Policy, basePolicy: Policy,
                                      stateActionValues: MutableSAVMeanMap = mutableMapOf()
): MutableSAVMeanMap {

    var returnValue = 0.0
    var weight = 1.0

    reversed().forEachIndexed { index, (state, actionPair) ->
        val (action, reward) = actionPair
        val oldValue = stateActionValues[state to action]?.value ?: 0.0

        returnValue = discountFactor.pow(index) * returnValue + reward

//        stateActionValues[state to action]?.addReturn(returnValue - oldValue)
//            ?: stateActionValues.put(state to action, data.MeanWeightedReturn(returnValue - oldValue))
//        stateActionValues[state to action]!!.weightTotal += weight

        stateActionValues.putIfAbsent(state to action, SAValue(returnValue - oldValue, 0.0))
        stateActionValues[state to action]!!.totalWeight += weight
        stateActionValues[state to action]!! += (weight * (returnValue - oldValue)) / stateActionValues[state to action]!!.totalWeight

        weight *= targetPolicy[state]!![action]!! / basePolicy[state]!![action]!!
    }

    return stateActionValues
}

//fun data.Episode.sampleRatiosOf(targetPolicy: Policy, basePolicy: Policy): MutableList<Double> {
//    println("Calculating importance sampling ratios... (This may take a while)")
//    val relativeProbabilities = relativeProbabilitiesOf(targetPolicy, basePolicy).reversed()
//    val result = mutableListOf<Double>()
//
//    reversed().forEachIndexed { index, _ ->
//        result.add(relativeProbabilities.drop(index).fold(1.0) { acc, d -> acc * d })
//    }
//
//    return result.asReversed()
//}

//private fun data.Episode.relativeProbabilitiesOf(targetPolicy: Policy, basePolicy: Policy): List<Double> {
//    return map { (state, actionPair) ->
//        val (action, _) = actionPair
//
//        if(targetPolicy[state.value]!![action]!! > 0.0) {
//            if(basePolicy[state.value]!![action]!! > 0.0) {
//                (targetPolicy[state.value]!![action]!! / basePolicy[state.value]!![action]!!)
//            } else {
//                throw Error("No coverage between policies!")
//            }
//        } else {
//            0.0
//        }
//    }
//}