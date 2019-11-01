import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow
import kotlin.reflect.KFunction

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

    private fun calculateReturn(rewards: List<Double>): Double { // TODO: Run in threads?
        var returnValue = rewards[0]

        return if(rewards.size > 1) {
            for(t in 1 until rewards.size) {
//                println("returnValue: $returnValue, dc: $dc, t: ${t}, reward: ${rewards[t]}, power: ${dc.pow(t)}, dc * reward: ${dc.pow(t) * rewards[t]}")
                returnValue += (discountFactor.pow(t) * rewards[t])
            }

            returnValue
        } else {
            rewards[0]
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
                    val curReturn = calculateReturn(rewards.drop(t))
                    returns[t] = curReturn

//                    println("G[$t] = $curReturn")
                }

                println("Done!")

                returns
            }
    }
}