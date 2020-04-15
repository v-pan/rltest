package blackjack

import data.State
import kotlin.reflect.KFunction

/*
We're keeping track of cards as Int, but it's not representative of their values in game

For example, values of 1 in the list are an ace, which can be 1 or 11 in game
    or Kings are 13 in the list, despite holding a value of 10 in game
 */

class BlackjackState(val cardList: List<Int>, override val reward: Double) : State() {
    override val value = totalCardValue(cardList)

    override fun getActions(): List<KFunction<State>> {
        return listOf(BlackjackTask::hit, BlackjackTask::stick)
    }

    fun totalCardValue(cards: List<Int> = cardList): Int {
        val aces = cards.count { it == 1 }
        var result = 0
        for(i in 0..aces) {
            result = cards.fold(0) { value, card ->
                when {
                    card == 1 -> {
                        if(value + 11 > 21) {
                            value + 1
                        } else {
                            value + 11
                        }
                    }
                    card > 10 -> {
                        value + 10
                    }
                    else -> {
                        value + card
                    }
                }
            }
        }
        if(result == 0) {
            throw Error("Card value wrong")
        }
        return result
    }

    override fun toString(): String {
        return "Hand: $cardList, total value: ${totalCardValue()}"
    }
}