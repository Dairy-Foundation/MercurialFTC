package org.firstinspires.ftc.teamcode

import dev.frozenmilk.dairy.mercurial.processes.Channel
import dev.frozenmilk.dairy.mercurial.processes.StateMachine
import dev.frozenmilk.dairy.mercurial.processes.SwitchedStateMachine
import java.util.function.Consumer
import java.util.function.UnaryOperator

@Suppress("unused")
object StateMachines {
    // state machines are a helpful way to model subsystems
    // StateMachine is an abstract class, so you can make your own state machine by extending it

    internal class MySubsystem : StateMachine() {
        // first, a state machine should set up all its data
        // this might be hardware devices
        // or global shared state

        // as part of the data, state machines should set up some channels
        // we will set up one channel

        // a word will come in over the word channel
        // we use single to make sure we only have the lastest word
        // we will use linkedSingleChannel to link the channel to this fiber
        // if the state machine stops, the channel will close
        private val words: Channel<String> = linkedSingleChannel()

        // state machines use modes
        // sometimes modes might be singletons
        // 'get' is a good example of a singleton mode
        private val get: Mode = object : Mode {
            // eval runs and returns the next mode to run
            override fun eval(): Mode {
                // let us check if there is word
                val word = words.poll()

                if (word != null) {
                    // if we got word, let everyone know:
                    println("got word: $word")
                    // and store the word
                    // returning a Mode will switch to it
                    return Store(word)
                }

                // if theres no word, we'll stay in this mode
                return this
            }
        }

        // sometimes a mode needs to have some extra data associated with it
        // an inner class is good for this, as it allows access to the shared data
        private inner class Store(val word: String) : Mode {
            // modes may override an enter method
            // this is invoked when the mode is switched to
            // and the previous mode is supplied
            // enter is only invoked when the mode changes,
            override fun enter(previousMode: Mode) {
                println("stored word: $word")
            }

            override fun eval(): Mode {
                // once we've stored a word, never forget it!
                return this
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

        fun sendWord() {
            // bird is the word
            words.send("bird")
        }

        fun storedWord(): String {
            // you can access the current mode of the state machine
            val mode = mode
            return if (mode is Store) mode.word
            else "bird"
        }

        // instead of the classes api we used above, we could have used the functional api
        // mode constructs a mode with 'eval'
        private val get2 = mode { self: Mode ->
            // let us check if there is word
            val word = words.poll()

            // if not, we see if we should switch
            // if no switch, return a and keep waiting for word
            if (word != null) {
                // if we got word, let everyone know:
                println("got word: $word")
                // and store the word
                store2(word)
            }
            else self
        }

        private fun store2(word: String): Mode {
            // entrance mode only has enter filled in
            // and always returns itself
            return entranceMode { from: Mode ->
                println("stored word: $word")
            }
        }

        // the mode function can also take an enter function
        private val demo2 = mode(
            // enter
            { from: Mode -> },
            // eval
            { self: Mode -> self },
        )
    }

    // often we want the ability to instruct our state machine to switch modes
    // while you could set up the logic by hand,
    // switched state machine does it all for you
    internal class MySubsystem2 : SwitchedStateMachine() {
        // NOTE: SwitchedStateMachine Modes are not compatible with StateMachine Modes

        // in switched state machines modes have an extra function
        private val demo: Mode = object : Mode {
            // like before we have enter
            override fun enter(previousMode: Mode) {
            }

            // and eval
            override fun eval(): Mode {
                return this
            }

            // and now we have exit, which is called when a mode switch is triggered by the builtin switching system
            // note that exit will not be called when returning a different mode from eval
            override fun exit(nextMode: Mode) {
            }
        }

        // just like StateMachine, we have mode and entrance mode helpers
        private val a = mode { self: Mode -> self }

        // but the egress mode helper also exists to set up exit-only modes
        private val b = egressMode { to: Mode -> }

        override fun init(): Mode {
            return demo
        }

        fun a() {
            // you can use switchTo to ask the state machine to swap modes for you
            // which it will do after the current mode finishes processing
            switchTo(a)
        }

        fun b() {
            // switchToNext will put the provided mode at the front of the queue
            // you probably shouldn't need this unless you're doing something complex
            switchToNext(b)
        }
    }

    // all the other state machine apis are the same!

    // lets quickly review the flow:

    // current mode -> a
    // request switch to b
    // eval a -> returns a
    // exit a (to b)
    // enter b (from a)
    // eval b -> ...

    // current mode -> a
    // request switch to b
    // eval a -> returns c
    // enter c (from a)
    // exit c (to b)
    // enter b (from c)
    // eval b -> ...

    // I recommend using switched state machines for most cases

    // hopefully state machines feel intuitive
    // you can see more practical and involved examples in the example robot package
    // as switched state machines are used for both the shooter and the drivetrain
}
