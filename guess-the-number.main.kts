import khoury.reactConsole
import khoury.input


data class MyState(val UserInput: Int, val guesses: Int )

val initialState: Int = MyState(0,0)

fun stateToText(currentState: MyState): String {
    if (currentState.guesses == 0) {
        return "Guess a number"
    }
    else if (currentState.userInput >7) {
        return "Too high"
    }
    else if (currentState.userInput < 7) {
        return "Too low"
    }
    else {
        return "You found it"
    }
}

fun transitionState(currentState: MyState, userInput: String): MyState {
    //Ignore currentState, don't need it
    return MyState(userInput.toInt(), currentState.guesses + 1)
}


fun terminate(currentState: MyState): Boolean {
    return (currentState.userInput == 7)
}

reactConsole(
    initialState,
    ::stateToText,
    ::transitionState,
    ::terminate,
)