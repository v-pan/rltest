import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow
import kotlin.reflect.KFunction
import kotlinx.coroutines.*

class MDPRLA(
    private val discountFactor: Double,
    private var state: State,
    private var actions: ArrayList<KFunction<State>> = state.getActions()
) {

    private val policy: HashMap<Any, ArrayList<Pair<KFunction<State>, Double>>> = HashMap()
    private val experiences: HashMap<State, ArrayList<ActionValuePair>> = HashMap()
    private val rewardValues = ArrayList<Double>()

    private fun timeStep(action: KFunction<State>?) {
//        println("Running time step...")

        if(action != null) {
            state = action.call(state)

            if(experiences[state] == null){
                experiences[state] = arrayListOf(ActionValuePair(action, state.reward))
            } else {
                experiences[state]!!.add(ActionValuePair(action, state.reward))
            }
        }
        rewardValues.add(state.reward)

        actions = state.getActions()

//        println("New state: $state")
//        println("Current actions: $actions")
//        println("Experiences: $experiences")
//        println("Rewards: $rewardValues")
    }

    fun chooseNextAction() {
        val random = Random()
        var r = random.nextDouble()

        if(policy[state.value] == null) {
            policy[state.value] = arrayListOf()
            val normalProbability = 1 / actions.size.toDouble()
            println("Normal Prob: $normalProbability")
            actions.forEach {
                policy[state.value]!!.add(Pair(it, normalProbability))
            }
            println("Created new entry for state value: ${state.value}")
        }

        for (pair in policy[state.value]!!) {
            r -= pair.second
            if(r <= 0) {
//                println("Running: ${pair.first}")
                timeStep(pair.first)
                break
            }
        }
    }

    fun getReturns() {
        println(calculateReturns(rewardValues)[0].toString())
    }

    fun getReturn() {
        println("Done! \n${calculateReturn(rewardValues)}")
    }

    private fun calculateReturn(rewards: List<Double>): Double { // TODO: Run in threads? Seems to slow down computation
        var returnValue = rewards[0]

        if(rewards.size > 1) {
            for(t in 1 until rewards.size) {
//                    println("returnValue: $returnValue, dc: $discountFactor, t: $t, reward: ${rewards[t]}, power: ${discountFactor.pow(t)}, dc * reward: ${discountFactor.pow(t) * rewards[t]}")
                    returnValue += (discountFactor.pow(t) * rewards[t])
            }

//            coroutineScope {
//                (1 until rewards.size).map {
//                    async(Dispatchers.Default) {
////                        println("returnValue: $returnValue, dc: $discountFactor, t: $it, reward: ${rewards[it]}, power: ${discountFactor.pow(it)}, dc * reward: ${discountFactor.pow(it) * rewards[it]}")
//                        returnValue += discountFactor.pow(it) * rewards[it]
//                    }
//                }
//            }.awaitAll()

            return returnValue
        } else {
            return rewards[0]
        }
    }

    private fun calculateReturns(rewards: List<Double>): DoubleArray {
        return if(rewards.isEmpty()) {
                println("No rewards!")
                DoubleArray(0)
                //0.0
            } else {
                println("Calculating returns...")

                val returns = DoubleArray(rewards.size)

                for(t in 0 until returns.size){
                    returns[t] = calculateReturn(rewards.drop(t))

//                    println("G[$t] = $curReturn")
                }
                println("Done!")

                returns
            }
    }
}