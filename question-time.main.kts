import khoury.CapturedResult
import khoury.captureResults
import khoury.EnabledTest
import khoury.runEnabledTests
import khoury.reactConsole
import khoury.fileReadAsList
import khoury.input
import khoury.testSame
import khoury.isAnInteger

//Step 1: Questions
data class Question(
    val questionString: String,
    val answerString: String,)

//question takes in a string for the question and string for the answer
val fileQuestions = readQuestionBank(0, "question-bank1.txt")
val question1 = Question("What is 2+2?", "4")
val question2 = Question("What is 1*1?", "1")
val question3 = Question("What is 2^3?", "8")

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Step 2: Question Banks
//creating a data class for question bank which contains a name and a list of questions
data class QuestionBank(
    val nameOfBank: String,
    val setsOfQuestions: List<Question>,
)
//question bank has name (used in the last step and referring to the list)

//creating a mathbank list with math questions
val mathBank =
    QuestionBank(
        "Math Questions",
        listOf( 
            question1,
            question2,
            question3, ),
    )

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Step 3: Auto-Generated question Banks

/*function cubes takes a count (assuming it is positive) and produces 
question bank of that many questions testing the users’ knowledge on perfect cubes*/
fun cubes(count: Int): QuestionBank = QuestionBank(
    "Cubes",
    List(count) {num -> Question("What is ${num + 1} cubed?", ((num + 1) * (num + 1) * (num + 1)).toString())}//starting from 1 so test cases ensure it runs properly (error from 0)
)
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Step 4: Files

//4.1: making question into a string by separating the question and answer (| is used as a separator)
fun questionToString(question: Question): String = question.questionString + "|" + question.answerString


//4.2: converting the string to a question by separating at instance |
fun stringToQuestion(questionAnswer: String): Question {
    val parts = questionAnswer.split("|")
    val separateQuestion: Question
    if (parts.size == 2) {
        separateQuestion = Question(parts[0], parts[1])//creating separate question with question as the first part and its answer as the second
    } else {
        separateQuestion = Question("Error", "Invalid")//returns error if no split or if more/less than 2 parts
    }

    return separateQuestion
}

//4.3: produces corresponding sequence of cards found in the file using sequence, returns empty sequence if file is N/A. 
fun readQuestionBank(
    currentLine: Int,
    questionFile: String,): List<Question> {
    val readFile = fileReadAsList(questionFile)

    if (currentLine >= readFile.size) {
        return listOf()
    }

    val fileList =
        List(readFile.size) { index ->
            if (index <= readFile.size) {
                val parts = readFile[index].split("|")//splitting each line to make it into a question
                if (parts.size == 2) {
                    Question(parts[0], parts[1])
                } else {
                    Question("Error", "Invalid")
                }
            } else {
                Question("Error", "Invalid")
            }
        }.filterIsInstance<Question>()//checks if each element is a question using filter (Source: kotlinlang.org)

    return fileList//returning list of questions from text file
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Step 5: Self-reporting on a single question

//creating a data class QuestionState has the phrase to help the react console know which part the user is observing
data class QuestionState(
    val questionBank: Question,
    val phrase: String,
)

//5.1: determining if the supplied string starts with the letter “y” or “Y”
//input with y means correct
fun isCorrect(input: String): Boolean = input.lowercase().startsWith("y")


//defining the state
val studyQuestionInit = QuestionState(question1, "question")


//5.2: using react console
fun studyQuestion() {
    reactConsole(
        studyQuestionInit,
        ::questionStateToText,
        ::questionNextState,
        ::questionTerminateState,
    )
}

//returning differnet strings based on what the user is observing and updating the state
fun questionStateToText(state: QuestionState): String {
    if (state.phrase == "question") {
        return state.questionBank.questionString
    } else if (state.phrase == "answer") {
        return state.questionBank.answerString
    } else if (state.phrase == "correct") {
        return "Great Job!"
    } else {
        return "Retry"
    }
}

 //phasing to the next state by using the phrase to go to next step
fun questionNextState(
    state: QuestionState,
    userInput: String,): QuestionState {
    val newQuestionBank = state.questionBank

    if (state.phrase == "question") {
        return QuestionState(newQuestionBank, "answer")
    } else if (state.phrase == "answer") {
        if (isCorrect(userInput)) {
            return QuestionState(newQuestionBank, "correct")
        } else {
            return QuestionState(newQuestionBank, "retry")
        }
    } else {
        return state
    }
}

//terminating function studyQuestion function if correct or needs to retry
fun questionTerminateState(state: QuestionState): Boolean {
    return state.phrase == "correct" || state.phrase == "retry"
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Step 6: Going over Multiple Questions

//6.1: designing data type QuestionBankState to keep track of question, observation, and self reports
//question bank state has a questionList variable to be used for different banks (not hardcoded to one)
data class QuestionBankState(
    val questionList: QuestionBank,
    val questionNum: Int,
    val observing: String,
    val correct: Int,
)


val studyBankInit = QuestionBankState(mathBank, 0, "question", 0)

//6.2: goes over all the questions in a supplied sequence and returns number that were (self-reported) correct
//analyzing the question bank with react console
fun studyQuestionBank(userSet: QuestionBankState) {
    reactConsole(
        userSet,
        ::bankStateToText,
        ::bankNextState,
        ::bankTerminateState,
    )
}

//showing text on the current state
fun bankStateToText(state: QuestionBankState): String {
    return if (state.questionNum < state.questionList.setsOfQuestions.size) {
        when (state.observing) {
            "question" -> state.questionList.setsOfQuestions[state.questionNum].questionString
            "answer" -> state.questionList.setsOfQuestions[state.questionNum].answerString + " - " + state.correct + " correct so far"
            else -> "Invalid"
        }
    } else {
        "No more Questions. You got ${state.correct} questions correct"
    }
    //returns string based off what user is looking at
}

//goes to next state based off what the user was previously observing
fun bankNextState(
    state: QuestionBankState,
    userInput: String,): QuestionBankState {
    if (state.questionNum >= state.questionList.setsOfQuestions.size) {
        return QuestionBankState(state.questionList, state.questionNum, "done", state.correct)
    }

    if (state.observing == "question") {
        return QuestionBankState(state.questionList, state.questionNum, "answer", state.correct)
    } else if (state.observing == "answer") {
        val newQuestionNum = state.questionNum + 1
        val newCorrect = if (isCorrect(userInput)) state.correct + 1 else state.correct
        return QuestionBankState(state.questionList, newQuestionNum, "question", newCorrect)
    }
    return state

}

//terminate state when the user completes all the questions
fun bankTerminateState(state: QuestionBankState): Boolean {
    // Terminate if all questions have been answered and the final message has been displayed once
    return state.questionNum >= state.questionList.setsOfQuestions.size && state.observing == "done"
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Step 7: Choosing a Bank

//creating multiple banks for the function to choose one bank from a bank list
val bank1 = mathBank
val bank2 = cubes(5)
val bank3 = QuestionBank("General Questions", readQuestionBank(1, "question-bank1.txt"))
val bankList = listOf(bank1, bank2, bank3)

//function chooseBank takes a list of QuestionBanks and produces a corresponding numbered menu 
fun chooseBank(listBanks: List<QuestionBank>): QuestionBank {
    println("Welcome to Question Time! You can choose from ${listBanks.size} Question banks:")//intro

    listBanks.forEachIndexed { index, bank ->
        println("${index + 1}. ${bank.nameOfBank}")//called function gets both the index of the current element, as well as the element itself, to produce a numbered menu
    }
    println("Enter your choice:")//prompting user for input (number)
    val questionInput = input()

    return if (isAnInteger(questionInput)) {//returning bank corresponding to number entered
        val choice = questionInput.toInt()
        if (choice in 1..listBanks.size) {
            listBanks[choice - 1]
        } else {
            println("Error out of bounds. Try again")//Error if out of bounds
            chooseBank(listBanks)//displaying the menu until the user enters a valid number
        }
    } else {
        println("Error not an option. Enter a number in range")//Error if not a number
        chooseBank(listBanks)//displaying the menu until the user enters a valid number.
    }
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Step 8: Putting All Together

fun play() {
    val selectedBank = chooseBank(bankList)//selecting one amongst a list of question banks
    val initialState = QuestionBankState(selectedBank, 0, "question", 0)
    studyQuestionBank(initialState)
}

//Defining main function with the play function inside of it to run everything
fun main() {
    play()
}

//Running enabled tests using the runEnabledTests function
runEnabledTests(this)

//Running main function to run the program
main()

//testing all the functions
@EnabledTest
fun testQuestions() {
    //checking if cubes are working correctly from 1
    testSame(
        cubes(2),
        QuestionBank(
            "Cubes",
            listOf(
                Question("What is 1 cubed?", "1"),//testing for 1 cubed (1)
                Question("What is 2 cubed?", "8")//testing for 2 cubed (8)
            )
        ),
        "Test cubes"
    )

    testSame(
        questionToString(question1),
        "What is 2+2?|4",
        "Test questionToString"
    )

    //checking if question and answer matching is working (correct)
    testSame(
        stringToQuestion("What is the day today?|Wednesday"),
        Question("What is the day today?", "Wednesday"),
        "Test stringToQuestion"
    )

    testSame(
        isCorrect("Yes"),
        true,
        "Test isCorrect - Yes"
    )

    testSame(
        isCorrect("No"),
        false,
        "Test isCorrect - No"
    )

    //checking self report for studyQuestion with right/wrong answers
    testSame(
        captureResults(
            ::studyQuestion,
            "",
            "y"
        ),
        CapturedResult(
            Unit,
            "What is 2+2?",
            "4",
            "Great Job!"
        ),
        "Test studyQuestion - Correct"
    )

    testSame(
        captureResults(
            ::studyQuestion,
            "",
            ""
        ),
        CapturedResult(
            Unit,
            "What is 2+2?",
            "4",
            "Retry"
        ),
        "Test studyQuestion - Incorrect"
    )

    //reflects correct question sequence in `mathBank`
    testSame(
        captureResults(
            { studyQuestionBank(studyBankInit) },
            "",  //first question
            "y", //correct answer 
            "",  //move to second question
            "n"  //incorrect answer for the second question
        ),
        CapturedResult(
            Unit,
            "What is 2+2?",
            "4 - 0 correct so far",
            "What is 1*1?",
            "1 - 1 correct so far",
            "What is 2^3?",
            "8 - 1 correct so far",
            "No more Questions. You got 1 questions correct"
        ),
        "Test studyQuestionBank"
    )
}




@EnabledTest
fun testReadQuestionBank() {
    //list must exactly match the contents of text file (question-bank1.txt)
    val expectedQuestions = listOf(
        Question("What is J. Cole's top song on 2014 Forest Hills Drive?", "No Role Modelz"),
        Question("What is the capital of Germany?", "Berlin"),
        Question("What is the largest continent?", "Asia"),
        Question("Which country has the most population?", "China"),
        Question("What is the smallest country in the world?", "Vatican City"),
        Question("What is the longest river in the world?", "Nile"),
        Question("What is the highest mountain in the world?", "Mount Everest"),
        Question("What is Tyler the Creator's new album?", "CHROMAKOPIA"),
        Question("Which desert is the largest?", "Sahara"),
        Question("Which is the deepest ocean?", "Pacific Ocean")
    )

    val result = readQuestionBank(0, "question-bank1.txt")

    testSame(
        result,
        expectedQuestions,
        "Test readQuestionBank"
    )
}

//testing function ChooseBank with valid input
@EnabledTest
fun testChooseBank() {
    val questionBanks = listOf(
        QuestionBank("Math Questions", listOf(question1, question2, question3)),
        cubes(5),
        QuestionBank("General Questions", readQuestionBank(1, "question-bank1.txt"))
    )

    val result = captureResults({ chooseBank(questionBanks) }, "1")

    testSame(
        result.returnedValue,
        questionBanks[0], //Math Questions bank as expected
        "Test chooseBank with input '1'"
    )
}