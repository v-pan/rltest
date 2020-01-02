!!! Warning: This is a WIP branch, and is likely to not even compile !!!

# rltest

Attempt at implementing a reinforcement learning algorithm in Kotlin using methods described in [Reinforcement Learning: An Introduction](http://incompleteideas.net/book/RLbook2018.pdf)

### Usage
To run the algorithm, first implement a `Task`, and initialise an `MDPRLA` within that task. Implement a `State` subclass which will store any possible state within your task. Then invoke the `start()` function of your `Task`, which should call timesteps and request policy improvements, as the task demands.

Actions written for a task should always be `public`, and accept a `State` or subclass of `State` to avoid runtime errors.

### Example Task: Recycler Robot
An example `RecyclerState.kt` and `RecyclerTask.kt` are included in this project which demonstrate implementing a given task. This task comes from Example 3.3 in the above book.

The MDPRLA instance can be found within the `RecyclerTask.kt`, as:

```kotlin
override val rla = MDPRLA(discountFactor = 0.9, state = RecyclerState(2, 0.0, this))
```

where a discount factor and initial state are provided.

The `start()` implementation is run in the tabular case, prompting user input between a given number of timesteps. Important to note are the public functions `chooseNextAction()` and `greedyNextPolicy()` which make the `MDPRLA` choose an action based on current policy, and greedily select a new policy, respectively

```kotlin
override fun start() {
        while (true) {
            val steps = readLine()!!.toInt()

            for(i in 0 until steps) {
                rla.chooseNextAction()
            }

            rla.greedyNextPolicy()
            
            // ...
        }
}
```

While this example simulates an environment through random number generation, actions can theoretically extend to perform any function, such as control a real robot.
