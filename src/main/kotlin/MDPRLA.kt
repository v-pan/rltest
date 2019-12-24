import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.reflect.KFunction

class MDPRLA(
    private val discountFactor: Double,
    private var state: State,
    private var actions: ArrayList<KFunction<State>> = state.getActions()
) {

    private val policy: HashMap<State, MutableMap<KFunction<State>, Double>> = HashMap()

    private val experiences: HashMap<State, MutableMap<KFunction<State>, Double>> = HashMap() // Replace with linked list
    private val linkedExperiences: MutableMap<State, MutableMap<KFunction<State>, MutableSet<State>>> = mutableMapOf()

    val stateValues = mutableMapOf<State, Double>()

    private fun timeStep(action: KFunction<State>?) {
        if(action != null) {
            val nextState = action.call(state)

//            if(linkedExperiences[state] == null) {
//                linkedExperiences[state] = mutableMapOf(action to mutableSetOf())
//            } else if(linkedExperiences[state]?.get(action) == null) {
//                linkedExperiences[state]!![action] = mutableSetOf()
//            }

//            linkedExperiences[state]!![action]!!.add(nextState)

            if(stateValues[nextState] != null) {
                stateValues[state] = state.reward + (discountFactor * stateValues[nextState]!!)
            } else {
                stateValues[state] = policy[state]!![action]!! * (state.reward + (discountFactor * nextState.reward))
            }

            state = nextState
            actions = nextState.getActions()
        }
    }

    fun chooseNextAction() {
        val random = Random()
        var r = random.nextDouble()

        if(policy[state] == null) {
            policy[state] = mutableMapOf()
            val normalProbability = 1 / actions.size.toDouble()
            actions.forEach {
                policy[state]!![it] = normalProbability
            }
        }

        for (pair in policy[state]!!) {
            r -= pair.value
            if(r <= 0) {
                timeStep(pair.key)
                break
            }
        }
    }
}