package data

data class SAWeightedValue(var value: Double, var totalWeight: Double) {
    operator fun plusAssign(other: Double) {
        value += other
    }
}