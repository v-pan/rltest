package data

data class SAValue(var value: Double, var totalWeight: Double) {
    operator fun plusAssign(other: Double) {
        value += other
    }
}