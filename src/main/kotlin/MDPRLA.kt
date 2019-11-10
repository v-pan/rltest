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

    fun printReturns() {
        println(calculateReturns(rewardValues).joinToString())
    }

    fun printReturn() {
        println("Done! \n${calculateReturn(rewardValues)}")
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