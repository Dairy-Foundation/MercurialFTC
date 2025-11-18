package org.firstinspires.ftc.teamcode.beginners

import dev.frozenmilk.dairy.mercurial.continuations.Continuations.command
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.ifHuh
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.jumpScope
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.parallel
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.race
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.scope
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.continuations.channels.Channels

class Closures2 {
    init {
        // we haven't yet looked at more advanced control flow techniques

        // the jump scope creates an escapable area
        scope {
            var count by variable { 0 }
            jumpScope {
                loop(
                    ifHuh(
                        { count < 20 },
                        exec { count++ },
                    ).elseHuh(
                        // the jump function will jump us out of the jump scope
                        jump(),
                    )
                )
            }
        }

        // note this this is similar to our previous examples

        // jump is useful for 'failing'

        // at each step, we wait for 2 seconds
        // and if the long running process doesn't succeed in that time,
        // we skip all of them
        jumpScope {
            sequence(
                race(
                    // long running process
                    loop(exec {}),
                    // wait 2 seconds, then jump out
                    sequence(
                        wait(2.0),
                        jump(),
                    )
                ),
                race(
                    // long running process
                    loop(exec {}),
                    // wait 2 seconds, then jump out
                    sequence(
                        wait(2.0),
                        jump(),
                    )
                ),
                race(
                    // long running process
                    loop(exec {}),
                    // wait 2 seconds, then jump out
                    sequence(
                        wait(2.0),
                        jump(),
                    )
                ),
            )
        }

        // this is a bit like an early return

        // commands:
        // if you feel particularly attached to commands, you can use them:
        sequence(
            command(),
            command(),
            command(),
        )

        // the standard command is equivalent to noop

        command()
            // you can set each step
            .setInit {}
            .setFinished { false }
            .setExecute {}
            // avoid setting the steps that you're going to leave empty / return true
            // as if you don't set them, then Mercurial will optimise the command
            .setEnd {}

        // in Mercurial, execute will not be run if finished returns true
        // this means if you don't setFinished, setExecute is pretty useless

        // channels:
        // channels are a useful tool for sending and receiving data across Fibers

        // tx stands for transmitter (you can send data into it)
        // rx stands for receiver (you can get data out of it)

        val (tx, rx) = Channels.single<String>()

        // a single channel supports sending data a 'single' receiver
        // this means a message can only be received once,
        // even if multiple different fibers are listening at the same time

        // atm there is no support for a `multiple` channel
        // but there will be soon!
        // channels are high on the list of needing some more work

        // usually you make a single channel globally,
        // and then bind lots of different things to the tx/rx

        scope {
            val message by value { "wow!" }
            // see how we can communicate across fibers running at the same time
            parallel(
                // sending a message
                Channels.send({ message }, { tx }),
                // receiving a message
                // recv is a bit like scope, but it gives you the register, and we can't make more
                Channels.recv({ rx }) { messageRegister ->
                    val message by messageRegister
                    exec { println(message) }
                },
            )
        }

        scope {
            // a oneshot channel is only useful for sending one message
            // so we need to make it and store it in a register
            val chan by value { Channels.oneshot<String>() }
            parallel(
                Channels.send({ "wow!" }, { chan.tx }),
                // attempting to send a second message will hang forever
                // which isn't good
                Channels.send({ "wow! again?" }, { chan.tx }),
                Channels.recv({ chan.rx }) { messageRegister ->
                    val message by messageRegister
                    exec { println(message) }
                },
            )
        }

        // there are poll variations for send and recv as well
        // they allow you to try sending or receiving, and do something else if it can't be done
        Channels.sendPoll({ "polling..." }, { tx }, exec { println("failed!") })

        // TODO: actors
    }
}