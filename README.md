# rltest

Attempt at implementing a reinforcement learning algorithm in Kotlin using methods described in [Reinforcement Learning: An Introduction](http://incompleteideas.net/book/RLbook2018.pdf).

## Usage
To run the algorithm, example tasks have been provided in the `tasks` package. These can be run by calling their `start()` method.

These example tasks will ask if you wish to explore or exploit the environment. Exploring will use normalised policy, picking actions at random to attempt to explore possible state transitions. Exploiting will act according to the target policy, if a target policy has been improved. Improving the target policy will thus change the algorithm's behaviour when exploiting.

You may also choose to implement your own task. See [#Custom Implementation](#custom-implementation) for details.

## Example tasks
These example tasks can be found in the `tasks` package, within their respective subdirectories.

### Recycler Robot
An example `RecyclerState.kt` and `RecyclerTask.kt` are included in this project which demonstrate implementing a given task. This task comes from Example 3.3 in [the above book](http://incompleteideas.net/book/RLbook2018.pdf).

While this example simulates a simple environment, actions can theoretically extend to perform any function, such as control a real robot.

The MDPRLA instance can be found within the `RecyclerTask.kt`, as:

```kotlin
override val rla = MDPRLA(discountFactor = 0.9, state = RecyclerState(2, 0.0), task = this)
```

where a discount factor and initial state are provided.

The `start()` function prompts user input between a number of timesteps by calling `runTask()`. Their (truncated) implementations can be seen below:

```kotlin
fun start() {

        // ...
        var acceptInput = true

        while(acceptInput) when(readLine()!!.toUpperCase()) {
            "E" -> runTask(false)
            "I" -> rla.improvePolicy()
            "X" -> runTask(true)
            else -> acceptInput = false
        }
}
```

```kotlin
private fun runTask(exploit: Boolean, steps: Int = 1000) {
        // ...

        for(i in 0 until steps) {
            when(exploit) {
                true -> {
                    rla.exploit()
                    // ...
                }
                false -> {
                    rla.explore()
                    // ...
                }
            }
        }

        println("Result:")
        // ...
}
```

### Blackjack
Another example `BlackjackTask.kt` and `BlackjackState.kt` are included, which demonstrate different ways of handling state and state transitions to the 'Recycler Robot' task.

It is otherwise implemented the same as the 'Recycler Robot', and so its implementation has been left out for brevity. For details, simply read the code.

## Custom implementation
When writing your own task, it must be a subclass of the `Task` interface.

- While the interface only specifies that each task hold its own `rla` instance, it is also required to implement all actions as methods of the task.

 - You must also create a subclass of the `State` class for your task. It should hold a `value` and `reward`, representing the value of your state and the associated reward with that value.

    It is important to consider the state and how it is valued as seperate, as this can greatly reduce your total state space. This is particularly important given the limitations of this reinforcement learning implementation.

As an example of how this might be done (from `BlackjackState.kt`):
```kotlin
class BlackjackState(val cardList: List<Int>, override val reward: Double) : State() {
    override val value = totalCardValue(cardList)

    // ...
}
```

where `cardList` represents the current hand, and `totalCardValue(cardList: List<Int>)` returns an Int value for the hand. Consequently, many different hands may return the same value[\*]().

 - Methods of the task may be passed to the `MDPRLA` instance through the `getActions()` method the associated `State`, whereupon they will be treated as actions. Actions are passed as a list to the `MDPRLA` using reflection.

As an example of how this might be done (from `RecyclerState.kt`):
```kotlin
override fun getActions(): List<Action> {
        return when(value) {
            0 -> listOf()
            1 -> listOf(RecyclerTask::searchAction, RecyclerTask::waitAction, RecyclerTask::rechargeAction)
            2 -> listOf(RecyclerTask::searchAction, RecyclerTask::waitAction)
            else -> throw Error("No actions for state!")
        }
}
```

 - Actions represent state transitions, taking a state, executing the action that is being taken by the algorithm, and returning the next state and reward.

    It is important that actions written for a task always be `public`, and accept a `State` or subclass of `State` to avoid runtime errors.

 - Finally a task should call the `explore()`, `exploit()` and `improvePolicy(temperature: Double)` methods of the initialised `rla` when appropriate as described in [#Usage](#usage).

    `improvePolicy(temperature: Double)` takes a parameter `temperature` which controls how close to deterministic the policy is. The closer it is to 0, the more deterministic it becomes. As the temperature approaches infinity, the policy becomes increasingly normalised. By default the temperature is 1. In the future, this temperature will hopefully be controlled by the `MDPRLA` to provide a better balance between exploration and exploitation than is currently available.

## Footnotes

### Reducing state space

> Consequently, many different hands may return the same value

While this approach helps to keep memory usage down, it comes at the potential cost of performance. When deciding on how to represent a state's value, the impact of reducing a policy's complexity should be considered to inform the final decision.

For instance, in the `BlackjackState` example used, the algorithm performs well at keeping its total hand value above 17, which it is penalised for sticking beneath, but still goes bust as often as before policy improvement. This is most likely because it cannot recognise any difference between hands with or without aces. A more effective representation may inform the algorithm of its total card value, as well as how many aces it has, to allow the algorithm to make more complex strategies and possibly improving its performance.
