package org.firstinspires.ftc.teamcode

import dev.frozenmilk.dairy.mercurial.processes.Channel
import dev.frozenmilk.dairy.mercurial.processes.StateMachine

class StateMachines {
    // state machines are a helpful way to model subsystems
    // StateMachine is an abstract class, so you can make your own state machine by extending it

    class MySubsystem : StateMachine() {
        // first, a state machine should set up all its data
        // this might be hardware devices
        // or global shared state (hopefully not!)

        // as part of the data, state machines should set up some channels
        // we will set up two channels

        // a word will come in over the word channel
        // we use single to make sure we only have the lastest word
        private val words = Channel.single<String>()
        // switches are requests to switch modes
        // this approach to mode switching is fairly common, and easy to set up
        // but is not the only way to handle mode switching
        private val switches = Channel.queue<Mode>()

        // state machines use modes
        // sometimes modes might be singletons
        // 'get' is a good example of a singleton mode
        private val get: Mode = object : Mode {
            // eval runs and returns the next mode to run
            override fun eval(): Mode {
                // let us check if there is word
                val word = words.poll()
                // if not, we see if we should switch
                // if no switch, return a and keep waiting for word
                if (word === null) return switches.poll() ?: this
                else {
                    // if we got word, let everyone know:
                    println("got word: $word")
                    // then, check if we should switch, or if not, move to Store the word
                    return switches.poll() ?: Store(word)
                }
            }
        }

        // sometimes a mode needs to have some extra data associated with it
        // an inner class is good for this, as it allows access to the shared data
        private inner class Store(val word: String) : Mode {
            // modes may override an enter method
            // this is invoked when the mode is switched to
            // and the previous mode is supplied
            override fun enter(previousMode: Mode) {
                println("stored word: $word")
            }
            override fun eval(): Mode {
                // we don't let go of the word until asked
                return switches.poll() ?: this
            }
        }

        // finally, we return the mode to start in
        // it will be entered too, with itself as the previous mode
        override fun init(): Mode {
            return get
        }

        // after this, you can add your own public api:

        fun sendWord(word: String) {
            words.send(word)
        }

        // bird is the word
        fun sendWord() = sendWord("bird")

        fun dropWord() {
            // ask to switch to get
            switches.send(get)
        }

        fun storeWord(word: String) {
            // ask to switch to store
            switches.send(Store(word))
        }

        fun storedWord(): String {
            // you can access the current mode of the state machine
            val mode = mode
            return if (mode is Store) mode.word
            // after all, bird IS the word
            else "bird"
        }
    }

    // hopefully state machines feel intuitive
    // you can see more practical and involved examples in the example robot package
    // as state machines are used for both the shooter and the drivetrain
}
