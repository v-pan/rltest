import blackjack.BlackjackTask
import recyclingRobot.RecyclerState
import recyclingRobot.RecyclerTask

fun main() {
    BlackjackTask().run{
        start()
        println(rla.mapStateToValue())
    }
}