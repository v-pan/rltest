package data

data class StateActionWeightedValue(var value: Double, var totalWeight: Double) {
    operator fun plusAssign(other: Double) {
        value += other
    }
}