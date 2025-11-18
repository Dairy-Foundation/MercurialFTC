package org.firstinspires.ftc.teamcode.beginners;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.command;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.ifHuh;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.jumpScope;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.parallel;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.race;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.scope;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitSeconds;

import dev.frozenmilk.dairy.mercurial.continuations.channels.Channel;
import dev.frozenmilk.dairy.mercurial.continuations.channels.Channels;
import dev.frozenmilk.dairy.mercurial.continuations.registers.ValRegister;
import dev.frozenmilk.dairy.mercurial.continuations.registers.VarRegister;

public class Closures2 {
    {
        // we haven't yet looked at more advanced control flow techniques

        // the jump scope creates an escapable area
        scope(env -> {
            VarRegister<Integer> count = env.variable(() -> 0);
            return jumpScope(jumpHandle ->
                    loop(
                            ifHuh(
                                    () -> count.get() < 20,
                                    exec(() -> count.set(count.get() + 1))
                            ).elseHuh(
                                    // the jump function will jump us out of the jump scope
                                    jumpHandle.jump()
                            )
                    )
            );
        });

        // note this this is similar to our previous examples

        // jump is useful for 'failing'

        // at each step, we wait for 2 seconds
        // and if the long running process doesn't succeed in that time,
        // we skip all of them
        jumpScope(jumpHandle ->
                sequence(
                        race(
                                // long running process
                                loop(exec(() -> {})),
                                // wait 2 seconds, then jump out
                                sequence(
                                        waitSeconds(2.0),
                                        jumpHandle.jump()
                                )
                        ),
                        race(
                                loop(exec(() -> {})),
                                sequence(
                                        waitSeconds(2.0),
                                        jumpHandle.jump()
                                )
                        ),
                        race(
                                loop(exec(() -> {})),
                                sequence(
                                        waitSeconds(2.0),
                                        jumpHandle.jump()
                                )
                        )
                )
        );

        // this is a bit like an early return

        // commands:
        // if you feel particularly attached to commands, you can use them:
        sequence(
                command(),
                command(),
                command()
        );

        // the standard command is equivalent to noop

        command()
                .setInit(() -> {})
                // you can set each step
                .setFinished(() -> false)
                .setExecute(() -> {})
                // avoid setting the steps that you're going to leave empty / return true
                // as if you don't set them, then Mercurial will optimise the command
                .setEnd(() -> {});

        // in Mercurial, execute will not be run if finished returns true
        // this means if you don't setFinished, setExecute is pretty useless

        // channels:
        // channels are a useful tool for sending and receiving data across Fibers

        // tx stands for transmitter (you can send data into it)
        // rx stands for receiver (you can get data out of it)

        Channel<String> chan = Channels.single();

        // a single channel supports sending data a 'single' receiver
        // this means a message can only be received once,
        // even if multiple different fibers are listening at the same time

        // atm there is no support for a `multiple` channel
        // but there will be soon!
        // channels are high on the list of needing some more work

        // usually you make a single channel globally,
        // and then bind lots of different things to the tx/rx

        scope(env -> {
            ValRegister<String> message = env.value(() -> "wow!");
            // see how we can communicate across fibers running at the same time
            return parallel(
                    // sending a message
                    Channels.send(message, chan::tx),
                    // receiving a message
                    // recv is a bit like scope, but it gives you the register, and we can't make more
                    Channels.recv(chan::rx, receivedMessage ->
                            exec(() -> System.out.println(receivedMessage.get())))
            );
        });

        scope(env -> {
            // a oneshot channel is only useful for sending one message
            // so we need to make it and store it in a register
            ValRegister<Channel<String>> chan2 = env.value(Channels::oneshot);
            return parallel(
                    Channels.send(() -> "wow!", () -> chan2.get().tx()),
                    // attempting to send a second message will hang forever
                    // which isn't good
                    Channels.send(() -> "wow! again?", () -> chan2.get().tx()),
                    Channels.recv(() -> chan2.get().rx(), receivedMessage ->
                            exec(() -> System.out.println(receivedMessage.get())))
            );
        });

        // there are poll variations for send and recv as well
        // they allow you to try sending or receiving, and do something else if it can't be done
        Channels.sendPoll(
                () -> "polling...",
                chan::tx,
                exec(() -> System.out.println("failed!"))
        );

        // TODO: actors
    }
}
