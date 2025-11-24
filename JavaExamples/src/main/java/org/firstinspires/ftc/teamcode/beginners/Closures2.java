package org.firstinspires.ftc.teamcode.beginners;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.async;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.command;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.ifHuh;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.jumpScope;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.match;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.parallel;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.race;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.scope;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitSeconds;

import dev.frozenmilk.dairy.mercurial.continuations.Actors;
import dev.frozenmilk.dairy.mercurial.continuations.Closure;
import dev.frozenmilk.dairy.mercurial.continuations.channels.Channel;
import dev.frozenmilk.dairy.mercurial.continuations.channels.Channels;
import dev.frozenmilk.dairy.mercurial.continuations.registers.ValRegister;
import dev.frozenmilk.dairy.mercurial.continuations.registers.VarRegister;

public class Closures2 {
    static {
        // we haven't yet looked at more advanced control flow techniques

        // the jump scope creates an escapable area
        scope(env -> {
            VarRegister<Integer> count = env.variable(() -> 0);
            return jumpScope(jumpHandle ->
                    loop(
                            ifHuh(
                                    () -> count.get() < 20,
                                    exec(() -> count.map(x -> x + 1))
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
                                loop(exec(() -> {
                                })),
                                // wait 2 seconds, then jump out
                                sequence(
                                        waitSeconds(2.0),
                                        jumpHandle.jump()
                                )
                        ),
                        race(
                                loop(exec(() -> {
                                })),
                                sequence(
                                        waitSeconds(2.0),
                                        jumpHandle.jump()
                                )
                        ),
                        race(
                                loop(exec(() -> {
                                })),
                                sequence(
                                        waitSeconds(2.0),
                                        jumpHandle.jump()
                                )
                        )
                )
        );

        // this is a bit like an early return

        // another useful tool is async
        async(
                asyncHandle -> sequence(
                        // calling detach will cause the continuation in this block
                        // to be moved to the current scheduler, instead of being tied to the running process
                        asyncHandle.detach(),
                        loop(exec(() -> {
                        }))
                ),
                // this block will be run after detach is called,
                // await() will cause this block to wait for the detached
                // Fiber to finish
                // cancel() will cancel it
                awaitHandle -> sequence(
                        waitSeconds(10.0),
                        ifHuh(
                                () -> true,
                                awaitHandle.await()
                        ).elseHuh(
                                awaitHandle.cancel()
                        )
                )
        );

        // async / await is harder to work with than the traditional structured
        // concurrency we've shown so far

        // its important to note that AFTER detach() occurs, the process will no longer have access
        // to any of the registers available before that
        // for this reason, its encouraged to 're-scope' everything:

        scope(env -> {
            // outer count
            VarRegister<Integer> count = env.variable(() -> 0);
            Closure increment = exec(() -> count.map(x -> x + 1));
            return sequence(
                    // increase it 3 times
                    increment,
                    increment,
                    increment,
                    // run an async block
                    async(
                            asyncHandle -> scope(env2 -> {
                                // we need to re-scope count, as the outer block will finish
                                // before the inner block
                                VarRegister<Integer> count2 = env.variable(count);
                                return sequence(
                                        asyncHandle.detach(),
                                        waitSeconds(10.0),
                                        exec(() -> System.out.println(count2.get()))
                                );
                                // this might not always be true,
                                // but we need to imagine that it could occur
                                // and the way that registers work is structured to keep us safe
                                // from that
                            }),
                            // note that we also have no obligation to await, or cancel
                            awaitHandle -> noop()
                    )
            );
        });

        // commands:
        // if you feel particularly attached to commands, you can use them:
        sequence(
                command(),
                command(),
                command()
        );

        // the standard command is equivalent to noop

        command()
                .setInit(() -> {
                })
                // you can set each step
                .setFinished(() -> false)
                .setExecute(() -> {
                })
                // avoid setting the steps that you're going to leave empty / return true
                // as if you don't set them, then Mercurial will optimise the command
                .setEnd(() -> {
                });

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

        // actors are like finite state machines
        // but more powerful

        // as you can imagine,
        // it's pretty easy to make a finite state machine with continuations

        // actors have a few advantages over traditional switch-statement:
        // 1. actors can have 'states' that are continuations themselves
        // 2. each state can use all the things we like about continuations:
        //   - registers
        //   - async
        //   - parallel processes
        //   - channels
        // 3. actors expose a channel over which other processes can send them a message,
        //    to change their state.

        // actors are recommended for use in creating separate systems that interact with each other
        // I strongly recommend using actors for writing your subsystems

        // an actor has two types associated with it:
        // 1. STATE: the current state of the actor
        // 2. MESSAGE: the type of all messages that the actor can receive

        // simple cases might have STATE and MESSAGE be the same
        // as the simplest message you can handle is 'heres your new state'
        // but more complex cases might have some separation
        // we'll do that here

        // we'll exit the init block in order to write the types and actor itself

        // this actor is modeled after a simple lift subsystem
    }

    private enum State {
        GoFor2Seconds,
        Stop,
    }

    // this actor can turn on a motor for two seconds
    // then goes back to Stop
    // its a very simple example
    Actors.Actor<State, State> actor = Actors.actor(
            // the initial state
            () -> State.Stop,
            (state, message) ->
                    // for this example, we'll ignore the previous state
                    // but more complex examples might handle the previous state
                    // switch to the new state
                    message,
            state ->
                    match(state)
                            .branch(State.GoFor2Seconds,
                                    sequence(
                                            exec(() -> {
                                                // motor.setPower(1);
                                                // ...
                                            }),
                                            // wait 2 seconds
                                            waitSeconds(2.0),
                                            // go to Stop
                                            exec(() -> state.set(State.Stop))
                                    ))
                            .branch(State.Stop,
                                    exec(() -> {
                                        // motor.setPower(0);
                                        // ...
                                    })
                            )
                            .assertExhaustive()
    );

    // we can write other closures that send a message to the actor
    Closure go = Channels.send(() -> State.GoFor2Seconds, actor::tx);
    // and control it externally
}
