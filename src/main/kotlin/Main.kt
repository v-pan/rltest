import blackjack.BlackjackTask
import recyclingRobot.RecyclerTask

fun main() {
    val blackjack = BlackjackTask()
    blackjack.run{
        start()
        rla.improvePolicy(0.5)
    }
}