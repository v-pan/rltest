import data.*
import java.util.*
import kotlin.reflect.KFunction

typealias StateValue = Any
typealias Action = KFunction<State>
typealias Policy = MutableMap<StateValue, MutableMap<Action, Double>>

class MDPRLA(
    private val discountFactor: Double,
    private var state: State,
    private val task: Task
) {

    private var actions: List<Action> = state.getActions()

    var behaviourPolicy: Policy = mutableMapOf()
    var targetPolicy: Policy = mutableMapOf()

    private var episode: Episode = mutableListOf()
    private val episodes: MutableList<Episode> = mutableListOf()

    var stateActionValueMap: MutableSAWVMap = mutableMapOf()

    fun explore() = timeStep(chooseAction(state, behaviourPolicy))
    fun exploit() = when(targetPolicy){
        null -> {
            println("No target policy has been trained.")
        }
        else -> {
            timeStep(chooseAction(state, targetPolicy!!))
        }
    }

    fun printTrainingResults() {
        println("SAWVMap:\n " +
                stateActionValueMap.map { (stateAction, weightedValue) -> stateAction to weightedValue.value }.joinToString { (stateAction, value) -> "${stateAction.first.value}: ${stateAction.second.name}=$value" })
        println("Target policy:\n " +
                targetPolicy.map { (stateValue, actionPair) -> stateValue to actionPair.map { (action, value) -> action to value }.joinToString { (action, value) -> "${action.name}=$value" } }.joinToString { (stateValue, actionString) -> "$stateValue={$actionString}" })
    }

//    fun improvePolicy(temperature: Double = 1.0) {
//        stateActionValueMap = episode.toStateActionWeightedValueMap(
//            discountFactor,
//            targetPolicy = targetPolicy ?: behaviourPolicy,
//            basePolicy = behaviourPolicy,
//            stateActionValues = stateActionValueMap
//        )
//        targetPolicy = stateActionValueMap.asStateActionDoubleMap().toPolicy(temperature)
//
//        println("New target policy:\n " +
//                targetPolicy!!.map { (stateValue, actionPair) -> stateValue to actionPair.map { (action, value) -> action to value }.joinToString { (action, value) -> "${action.name}=$value" } }.joinToString { (stateValue, actionString) -> "$stateValue={$actionString}" }
//        )
//    }

    private fun timeStep(action: Action?) {
        if(action != null) {
            val nextState = action.call(task, state)

//            episode.add(state to (action to nextState.reward))
            episode.add(EpisodeEntry(state, action, nextState.reward))

            if(nextState.terminal) {
                var returnValue = 0.0
                var weight = 1.0

                for(t in (episode.size - 1) downTo 0) {
                    returnValue = (discountFactor * returnValue) + episode[t].reward

                    stateActionValueMap.putIfAbsent(state to action, SAWeightedValue(0.0, 0.0))
                    stateActionValueMap[state to action]!!.totalWeight += weight

                    stateActionValueMap[state to action]!! +=
                        (weight * (returnValue - stateActionValueMap[state to action]!!.value)) / stateActionValueMap[state to action]!!.totalWeight

                    val argMax = stateActionValueMap.argMaxOf(state)

                    targetPolicy[state.value] = state.getActions().map {
                        if(it == argMax) {
                            it to 1.0
                        } else {
                            it to 0.0
                        }
                    }.toMap().toMutableMap()

                    if(action != argMax) {
                        break
                    }

                    weight *= 1 / behaviourPolicy[state.value]!![action]!!
                }
//                println("SAWVMap:\n " +
//                        stateActionValueMap.map { (stateAction, weightedValue) -> stateAction to weightedValue.value }.joinToString { (stateAction, value) -> "${stateAction.first.value}: ${stateAction.second.name}=$value" })
//                println("New target policy:\n " +
//                    targetPolicy.map { (stateValue, actionPair) -> stateValue to actionPair.map { (action, value) -> action to value }.joinToString { (action, value) -> "${action.name}=$value" } }.joinToString { (stateValue, actionString) -> "$stateValue={$actionString}" })
//                println()

                episodes.add(episode)
                episode = mutableListOf()
            }

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