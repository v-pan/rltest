import java.lang.Error
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.reflect.KFunction

class MDPRLA(
    private val discountFactor: Double,
    private var state: State,
    private var actions: ArrayList<KFunction<State>> = state.getActions()
) {

    private val policy: HashMap<Any, ArrayList<Pair<KFunction<State>, Double>>> = HashMap()
    private val experiences: HashMap<State, ArrayList<Pair<KFunction<State>, Double>>> = HashMap()
    private var rewardValues = ArrayList<Double>()

    private fun timeStep(action: KFunction<State>?) {
        if(action != null) {
            val nextState = action.call(state)

            experiences[state]?.add(Pair(action, nextState.reward)) ?: experiences.put(state, arrayListOf(Pair(action, nextState.reward)))

            state = nextState
            actions = state.getActions()

            rewardValues.add(nextState.reward)
        }
    }

    fun chooseNextAction() {
        val random = Random()
        var r = random.nextDouble()

        if(policy[state.value] == null) {
            policy[state.value] = arrayListOf()
            val normalProbability = 1 / actions.size.toDouble()
            actions.forEach {
                policy[state.value]!!.add(Pair(it, normalProbability))
            }
        }

        for (pair in policy[state.value]!!) {
            r -= pair.second
            if(r <= 0) {
                timeStep(pair.first)
                break
            }
        }
    }

    fun nextPolicyNormalised() {
        experiences.keys.forEach { state ->
            println("Generating for $state, ${experiences[state]}...")
            generateNormalisedPolicy(state)
        }
    }

    fun nextPolicyGreedy() {
        experiences.keys.forEach { state ->
            println("Generating for $state...")
            generateGreedyPolicy(state)
        }
    }

    private fun generateNormalisedPolicy(targetState: State = state) {
        val stateValues = estimateStateActionValues()

        var lowest = 0.0
        var newProbabilities: List<Double> = stateValues[targetState]?.map { (_, value) ->
            if(value < 0) {
                if(value <= lowest) {
                    lowest = value
                }
            }
            value
        } ?: throw Error("No value for state!")

        lowest = lowest.absoluteValue
        newProbabilities = newProbabilities.map {
            it + lowest
        }

        val sum = newProbabilities.sum()
        if(sum != 0.0) {
            newProbabilities = newProbabilities.map {
                it / sum
            }
        }

        val newPolicy = if(policy[targetState.value]?.size == newProbabilities.size) {
            policy[targetState.value]?.mapIndexed { index, pair ->
                Pair(pair.first, newProbabilities[index])
            } ?: throw Error("No policy entry for state!")

        } else {
            policy[targetState.value] ?: throw Error("No policy entry for state!")
        }

        println("New policy for $targetState:\n $newPolicy")
        policy[targetState.value] = ArrayList(newPolicy)
    }

    private fun generateGreedyPolicy(targetState: State = state) {
        val stateValues = estimateStateActionValues()

        var highestReturn: Pair<KFunction<State>, Double>? = null
        stateValues[targetState]?.forEach { actionPair ->
            if(highestReturn == null) {
                highestReturn = actionPair
            } else {
                if(highestReturn!!.second < actionPair.second) {
                    highestReturn = actionPair
                }
            }
        } ?: throw Error("No value for state!")

        println("Optimising (greedily) for ${highestReturn!!.first.name}...")

        val newPolicy = policy[targetState.value]?.map { pair ->
            if(pair.first == highestReturn!!.first) {
                Pair(pair.first, 1.0)
            } else {
                Pair(pair.first, 0.0)
            }
        } ?: throw Error("No policy entry for state!")

        println("New policy for $targetState:\n $newPolicy")
        policy[targetState.value] = ArrayList(newPolicy)
    }

//    fun printReturns() {
//        println(calculateReturns(rewardValues).joinToString())
//        rewardValues = ArrayList<Double>()
//    }

    fun printReturn() {
        println("Done! \n${calculateReturn(rewardValues)}")

        rewardValues = ArrayList<Double>()
    }

    fun printStateValues() {
        println(experiences.values.size)
        println("State value estimations: ${estimateStateValues()}")
    }

    fun printStateActionValues() {
        println("State/Action value estimations: ${estimateStateActionValues()}")
    }

    private fun estimateStateValues(): ArrayList<Pair<State, Double>> {
        val pairs = ArrayList<Pair<State, Double>>()

        experiences.forEach { (state, actionPairs) ->
            val rewardArray = DoubleArray(actionPairs.size)

            actionPairs.forEachIndexed { index, (_, reward) ->
                rewardArray[index] = reward
            }

            pairs.add(Pair(state, calculateReturn(rewardArray.asList())))
        }

        return pairs
    }

    private fun estimateStateActionValues(): HashMap<State, ArrayList<Pair<KFunction<State>, Double>>> {
        val result = HashMap<State, ArrayList<Pair<KFunction<State>, Double>>>()

        experiences.forEach { (state, pairs) ->
            val actionRewards = HashMap<KFunction<State>, java.util.ArrayList<Double>>()

            pairs.forEach { (action, reward) ->
                actionRewards[action]?.add(reward) ?: actionRewards.put(action, arrayListOf(reward))
            }

            actionRewards.forEach { (action, rewards) ->
                result[state]?.add(Pair(action, calculateReturn(rewards))) ?: result.put(state, arrayListOf(Pair(action, calculateReturn(rewards))))
            }
        }

        return result
    }

    private fun calculateReturn(rewards: List<Double>): Double { // TODO: Run in threads? Seems to slow down computation
        var returnValue = 0.0

        if(rewards.size > 1) {
            for(t in 1 until rewards.size) {
                returnValue += (discountFactor.pow(t-1) * rewards[t])
            }
        }

        return returnValue
    }

    private fun calculateReturns(rewards: List<Double>): DoubleArray {
        return if(rewards.isEmpty()) {
                println("No rewards!")
                DoubleArray(0)
                //0.0
            } else {
                println("Calculating returns...")

                val returns = DoubleArray(rewards.size)

                for(t in 0 until returns.size){ // TODO: Attempt to thread all returns
                    returns[t] = calculateReturn(rewards.drop(t))
                }

                println("Done!")
                returns
            }
    }
}