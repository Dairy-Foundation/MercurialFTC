package org.firstinspires.ftc.teamcode;

import org.jetbrains.annotations.NotNull;

import dev.frozenmilk.dairy.mercurial.processes.Channel;
import dev.frozenmilk.dairy.mercurial.processes.StateMachine;

public class StateMachines {
    // state machines are a helpful way to model subsystems
    // StateMachine is an abstract class, so you can make your own state machine by extending it

    class MySubsystem extends StateMachine {
        // first, a state machine should set up all its data
        // this might be hardware devices
        // or global shared state (hopefully not!)

        // as part of the data, state machines should set up some channels
        // we will set up two channels

        // a word will come in over the word channel
        // we use single to make sure we only have the lastest word
        private final Channel<String> words = Channel.single();
        // switches are requests to switch modes
        // this approach to mode switching is fairly common, and easy to set up
        // but is not the only way to handle mode switching
        private final Channel<Mode> switches = Channel.queue();

        // state machines use modes
        // sometimes modes might be singletons
        // 'get' is a good example of a singleton mode
        private final Mode get = new Mode() {
            // eval runs and returns the next mode to run
            @Override
            @NotNull
            public Mode eval() {
                // let us check if there is word
                String word = words.poll();
                // if not, we see if we should switch
                // if no switch, return a and keep waiting for word
                if (word == null) {
                    Mode next = switches.poll();
                    if (next == null) return this;
                    else return next;
                } else {
                    // if we got word, let everyone know:
                    println("got word: " + word);
                    // then, check if we should switch, or if not, move to Store the word
                    Mode next = switches.poll();
                    if (next == null) return new Store(word);
                    else return next;
                }
            }
        };

        // sometimes a mode needs to have some extra data associated with it
        // an inner class is good for this, as it allows access to the shared data
        private class Store implements Mode {
            public final String word;

            private Store(String word) {
                this.word = word;
            }

            // modes may override an enter method
            // this is invoked when the mode is switched to
            // and the previous mode is supplied
            @Override
            public void enter(@NotNull Mode previousMode) {
                println("stored word: $word");
            }

            @Override
            @NotNull
            public Mode eval() {
                // we don't let go of the word until asked
                Mode nextMode = switches.poll();
                if (nextMode == null) return this;
                else return nextMode;
            }
        }

        // finally, we return the mode to start in
        // it will be entered too, with itself as the previous mode

        @Override
        @NotNull
        protected Mode init() {
            return get;
        }

        // after this, you can add your own public api:

        public void sendWord(String word) {
            words.send(word);
        }

        // bird is the word
        public void sendWord() {
            sendWord("bird");
        }

        public void dropWord() {
            // ask to switch to get
            switches.send(get);
        }

        public void storeWord(String word) {
            // ask to switch to store
            switches.send(new Store(word));
        }

        public String storedWord() {
            // you can access the current mode of the state machine
            Mode mode = mode();
            if (mode instanceof Store) return ((Store) mode).word;
                // after all, bird IS the word
            else return "bird";
        }
    }

    // hopefully state machines feel intuitive
    // you can see more practical and involved examples in the example robot package
    // as state machines are used for both the shooter and the drivetrain

    // ignore:
    private static void println(String str) {
        System.out.println(str);
    }
}
