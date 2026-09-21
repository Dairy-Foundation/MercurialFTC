package org.firstinspires.ftc.teamcode;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.*;

import dev.frozenmilk.dairy.mercurial.Mercurial;
import dev.frozenmilk.dairy.mercurial.continuations.Continuation.Builder;
import dev.frozenmilk.dairy.mercurial.continuations.Continuations;
import dev.frozenmilk.dairy.mercurial.continuations.Expression;
import dev.frozenmilk.dairy.mercurial.continuations.Parameter;
import dev.frozenmilk.dairy.mercurial.continuations.Receive;
import dev.frozenmilk.dairy.mercurial.continuations.Return;
import dev.frozenmilk.dairy.mercurial.environments.Reference;
import dev.frozenmilk.dairy.mercurial.processes.Channel;
import dev.frozenmilk.dairy.mercurial.processes.ExitReason;
import dev.frozenmilk.dairy.mercurial.processes.Fiber;
import dev.frozenmilk.dairy.mercurial.processes.Messages;
import dev.frozenmilk.dairy.mercurial.processes.ProcessStatus;
import dev.frozenmilk.dairy.mercurial.processes.Scheduler;
import dev.frozenmilk.dairy.mercurial.processes.Spawnable;
import dev.frozenmilk.dairy.mercurial.processes.Timer;
import dev.frozenmilk.util.collections.WBT;
import kotlin.Unit;
import kotlin.time.TimeSource;

@SuppressWarnings("unused")
public class ConcurrentProgramming {
    // setup and the scheduler
    {
        // before we actually run any Mercurial programs,
        // we need to take a quick look at setup and the scheduler

        // When using mercurial for ftc, this step will be done for you
        // if you don't initialise the thread,
        // then trying to run programs as we will do shortly will crash
        Mercurial.initialiseThread();
        // you can pass a settings object to change some global settings
        // these are the default settings
        Mercurial.initialiseThread(
            new Mercurial.Settings(
                // the time source used by all of Mercurial
                TimeSource.Monotonic.INSTANCE,
                // controls if retracing of exceptions is enabled
                true,
                // the scheduler
                new Scheduler.Standard()
            )
        );

        // you can access these values from Mercurial
        Scheduler scheduler = Mercurial.scheduler();

        // if you need to run the scheduler yourself, you can poll it
        scheduler.poll();

        // poll duration is the previous duration of polling
        double _a = scheduler.getPollDurationSeconds();

        // average fiber duration is a weighted moving average fiber duration
        // more on fibers shortly
        double _b = scheduler.getAverageFiberDurationSeconds();

        // you will learn more about working with the scheduler in opmodes later
    }

    // channels
    {
        // a channel is a place to send and receive messages
        // we use them to communicate between different processes in Mercurial

        // a single channel can hold a single message at once
        // sending a new message to it will overwrite the old message
        // single channels are good if you only want the most up-to-date message
        // like if you use the channel to receive sensor data, or drive inputs
        Channel<String> word = Channel.single();

        // queue channels can hold an infinite list of messages
        // queues are good when you don't want to miss an event
        Channel<String> words = Channel.queue();

        // channels can be polled
        // they will return null if empty
        // otherwise they will return a message from the channel
        // and remove that message from the channel
        String aWord = word.poll();

        // you can also send a message to the back of the channel
        word.send("Hello");

        // generally you should only be sending messages to the back of the channel
        // sometimes you need to put a message at the front
        word.prepend("World");

        // once again, for a single channel, sending two messages in a row will erase the first one
        // but not so for a queue

        // we will use channels more later
        // now, lets look at ways to start running top level programs
    }

    // spawnables, fibers, spawning, linking, monitoring
    {
        // you can convert any builder into a spawnable
        // this compiles it, and allows you to start a
        // new process running the program as many times as you wish
        Spawnable<Boolean> tenThenTrue = Continuations.<Boolean>expression((scope, _self) -> {
            scope.run(waitFor.bind(seconds(10)));
            return value.b(() -> true);
        }).spawnable();

        // you can take a spawnable and .spawn() it
        // which creates, schedules, and returns a fiber running the program
        // each fiber runs independently, and concurrently
        Fiber<Boolean> fiber = tenThenTrue.spawn();
        // remember a fiber is a running program,
        // each time you spawn the same program will be a new, different fiber

        // fibers will exit when they finish running the program
        // normally, they exit with ExitReason.normally
        // you can force a fiber to exit like this:
        fiber.exit(ExitReason.interrupt);
        // there are a few built in exit reasons, but you can add your own, by extending ExitReason

        // normally is for when a fiber finishes running
        // be careful about manually exiting normally,
        // as other fibers may expect the fiber to have stored a return value
        // and will see the normal exit reason as the program having finished
        // and then the expected value won't be there for them
        ExitReason _a = ExitReason.normally;

        // interrupt is a good generic abnormal reason
        ExitReason _b = ExitReason.interrupt;

        // kill is a special exit reason
        // it forces a fiber to die, preventing the fiber from ignoring the exit request
        // we will see more of this in a second
        ExitReason _c = ExitReason.kill;

        // kill is converted to killed,
        // allowing linked fibers to know that the fiber was killed with kill
        // but not also force killing them
        // more on linking shortly
        ExitReason _d = ExitReason.killed;

        // when a fiber throws an exception, the fiber will exit exceptionally.
        // exceptionally is treated specially,
        // and the exception will be re thrown if it causes the root fiber to exit
        ExitReason _e = new ExitReason.Exceptionally(new RuntimeException());

        // the root fiber is the fiber running the scheduler
        // its active by default
        // and if it exits, then the whole opmode will exit

        // fibers can be linked
        fiber.link();
        // this links the currently running fiber (the root at the moment) to fiber
        // if either of the linked fibers exits abnormally, it will cause the other fiber to exit
        // you should generally link fibers

        // spawnLink spawns and links immediately,
        Fiber<Boolean> _f = tenThenTrue.spawnLink();

        // fibers can be monitored as well
        Channel<Messages.Down> downChannel = Channel.queue();
        fiber.monitor(downChannel);
        // when the fiber exits, a message (Messages.Down) will be sent to the down channel
        // Messages.Down records the fiber that exited, and the reason why

        // you can also spawnMonitor, which spawns and setups up the monitor at the same time
        Fiber<Boolean> _g = tenThenTrue.spawnMonitor(downChannel);

        // you can unlink
        fiber.unlink();
        // or demonitor
        fiber.demonitor(downChannel);
        // to remove a link or monitor

        // you can check the current status of a fiber
        ProcessStatus status = fiber.status();
        // the status is either ProcessStatus.Alive
        // or an exit reason
        if (status == ProcessStatus.alive) {

        } else if (status instanceof ExitReason) {

        }
        // status.alive will be true if the fiber has not yet exited
        // and is ProcessStatus.Alive
        // you can use this to check if a fiber is still running
        if (status.alive()) {

        }

        // when a fiber finishes running
        // you can get the return value
        // this will throw an exception if the fiber hasn't exited normally
        boolean cond = fiber.returnValue();

        // instead, you can use return result
        Fiber.Result<Boolean> condResult = fiber.returnResult();
        // the result will be Ok with the value if the fiber finished normally
        // or will be the current status value
        boolean result;
        if (condResult instanceof Fiber.Result.Ok)
            result = ((Fiber.Result.Ok<Boolean>) condResult).getValue();
        else result = false;

        // at any point you can check the current fiber
        // you can never know the return type of the current fiber though
        // as for it to be the current fiber, it must be alive
        Fiber<?> current = Fiber.current();

        // you can use Fiber.exitChannel to set a channel to 'trap' exits
        // this sets it for the current Fiber
        // you cannot set it for other fibers
        Channel<Messages.Exit> exitChannel = Channel.queue();
        Fiber.exitChannel(exitChannel);
        // when Fiber.exit is called
        // it will be converted to Messages.Exit
        // and sent to the exit channel
        // only the special exit reason 'kill' can bypass this,
        // and the current fiber can always exit itself without sending a message

        // poll duration is the time it took for the fiber to be run last cycle
        double seconds = fiber.getPollDurationSeconds();

        // functions are a bit stranger to spawn
        // spawnable doesn't change anything about it
        // but does guarantee that the function has been compiled
        Parameter.O<String, Parameter.O<String, Return<Object>>> print = Continuations.<String, String, Object>function((scope, _self, a, b) ->
            exec(() -> println(a.get() + ", " + b.get()))
        ).spawnable();

        // unlike for builders, you use chained .spawn() calls to set each argument
        // and at the end you can spawn / spawnLink / spawnMonitor
        Fiber<?> fiber2 = print.spawn("Hello").spawn("World").spawn();
        Fiber<?> _h = print.spawn("Hello").spawn("World").spawnLink();
        Fiber<?> _i = print.spawn("Hello").spawn("World").spawnMonitor(downChannel);

        // mixed syntax like this is not allowed
        Fiber<?> _j = print.bind(() -> "Hello").spawn("World").spawn();

        // its also not allowed to not complete the call in one go
        Parameter.O<String, Return<Object>> _k = print.spawn("Hello");

        // these could all lead to undefined behaviour
    }

    // spawning closures
    {
        // spawnable is only suitable for top level builders and functions
        // if you want to spawn a closure correctly,
        // then you'll need a slightly different set of tools

        Builder<?> _a = expression((scope, _self) -> {
            Reference.I x = scope.i(() -> 0);

            Builder<Integer> program = expression((innerScope, __self) -> {
                // x is still available here, even though it runs in parallel
                innerScope.run(waitFor.bind(seconds(10)));
                innerScope.runExec(() -> println(x.get()));
                innerScope.runExec(() -> x.set(5));
                return value.i(x);
            });

            // the spawn builder will spawn the given builder,
            // correctly capturing the current environment
            scope.run(spawn(program));

            // you can use o to store the spawned fiber, and work with it like normal
            Reference.O<Fiber<Integer>> fiber1 = scope.o(spawn(program));

            // spawn.link behaves like we've seen before
            scope.run(spawn.link(program));

            // spawn.monitor also behaves as we've seen before
            Reference.O<Channel<Messages.Down>> downChannel = scope.o(() ->
                Channel.queue()
            );
            scope.run(spawn.monitor(downChannel, program));

            // you can also spawn several fibers at once
            // this returns a set of fibers
            // this also works with spawn.link
            // and spawn.monitor
            // we will see some uses for groups of fibers shortly
            Reference.O<WBT.Tree<Fiber<Integer>>> fibers = scope.o(
                spawn(
                    program,
                    program,
                    program,
                    program,
                    program
                )
            );

            return noop;
        });

        // next, lets look at meta functions to help await and exit spawned fibers
    }

    // awaiting, exiting
    {
        Spawnable<Boolean> tenThenTrue = Continuations.<Boolean>expression((scope, _self) -> {
            scope.run(waitFor.bind(seconds(10)));
            return value.b(() -> true);
        }).spawnable();

        Builder<?> _a = expression((scope, _self) -> {
            Reference.I x = scope.i(() -> 0);

            Builder<Integer> program = expression((innerScope, __self) -> {
                // x is still available here, even though it runs in parallel
                innerScope.run(waitFor.bind(seconds(10)));
                innerScope.runExec(() -> println(x.get()));
                innerScope.runExec(() -> x.set(5));
                return value.i(x);
            });

            // you can use awaiting and interruptions with either method of spawning a fiber
            Reference.O<Fiber<Boolean>> fiber1 = scope.o(() -> tenThenTrue.spawnLink());
            Reference.O<Fiber<Integer>> fiber2 = scope.o(spawn(program));

            // there are meta functions and functions provided
            // to help you with waiting for return values from fibers

            // await.exit will not return anything, and just waits for the fiber to exit
            scope.run(await.exit.bind(fiber1));

            // await.status will return the exit reason of the fiber
            Reference.O<ExitReason> status = scope.o(await.status.bind(fiber2));

            // await.result will return the result of the fiber
            // either ok with the returned value
            // or error with the exit reason
            // this is very similar to fiber.returnResult
            // note that you need to tell result the type that will be returned
            Reference.O<Fiber.Result<Boolean>> result = scope.o(await.<Boolean>result().bind(fiber1));

            // await.expect will throw an exception if the awaited fiber finished abnormally
            Reference.O<Boolean> value = scope.o(await.<Boolean>expect().bind(fiber1));

            // it is best to prefer await if you won't look at the returned value / status
            // as it will perform a bit better

            // now we will look at awaiting groups
            Reference.O<WBT.Tree<Fiber<Integer>>> fibers = scope.o(
                spawn.link(
                    program,
                    program,
                    program,
                    program,
                    program
                )
            );

            // await.all will wait for the whole group to exit
            scope.run(await.all.bind(fibers));
            // await.all will wait for any fiber in the group to exit
            scope.run(await.any.bind(fibers));

            // exit.one can be used to exit a fiber
            scope.run(exit.one.bind(fiber1).bind(() -> ExitReason.interrupt));
            // exit.all can be used to exit a group
            scope.run(exit.all.bind(fibers).bind(() -> ExitReason.interrupt));

            // interrupt and kill will exit with the builtin exit reasons
            scope.run(interrupt.one.bind(fiber2));
            scope.run(interrupt.all.bind(fibers));
            scope.run(kill.one.bind(fiber2));
            scope.run(kill.all.bind(fibers));

            // there are some prebuilt meta functions for common spawn-await situations
            // these are based on the parallel tooling that most command bases use

            // parallel spawns, links, and awaits all fibers
            scope.run(parallel(
                program,
                program,
                program,
                program,
                program,
                program
            ));
            // race spawns and links all fibers,
            // then waits for any fiber to finish
            // then unlinks and cancels all remaining fibers
            scope.run(race(
                program,
                program,
                program,
                program
            ));

            // deadline spawns and links all fibers
            // then and waits for the first fiber (the deadline) to finish
            // then unlinks and cancels all other fibers
            scope.run(deadline(
                program,
                program,
                program,
                program
            ));

            return noop;
        });

        // now we have seen all of Mercurial's async / await api features
        // lets take a look at sending and receiving messages with channels
    }

    // messaging and receive
    {
        // let's look at some example programs that send and receive messages to each other
        // this ping and pong program will run two fibers that will message back and forth 3 times

        // we will first make the channels
        // there is no important information for these channels
        // so we will send Unit, which is a bit like kotlin's void
        Channel<Unit> pingChannel = Channel.queue();
        Channel<Unit> pongChannel = Channel.queue();

        Fiber<?> ping = expression((scope, _self) ->
            // receive is a lot like match with cases
            // we will look more at its details shortly

            // this will receive three pings
            // and respond to pong each time
            repeat(
                3,
                receive()
                    .receive(pingChannel, exec(() -> {
                            // print ping
                            println("ping!");
                            // send back to pong
                            pongChannel.send(Unit.INSTANCE);
                        })
                    )
            )
        ).spawnable().spawnLink();

        Fiber<?> pong = expression((scope, _self) ->
            // this will receive three pongs
            // and respond to ping each time
            repeat(
                3,
                receive()
                    .receive(pongChannel, exec(() -> {
                            // print pong
                            println("pong!");
                            // send back to ping
                            pingChannel.send(Unit.INSTANCE);
                        })
                    )
            )
        ).spawnable().spawnLink();

        // now the two programs will run and message back and forth
        // we just need to send the first message to start it!
        pingChannel.send(Unit.INSTANCE);

        Channel<String> words = Channel.queue();
        Channel<Integer> numbers = Channel.queue();

        // this receive will wait until a message comes in on the words channel
        // and then will return it
        Receive<String> program1 = Continuations.<String>receive()
            .receive(words, (Expression<?> scope, Reference.O<String> word) -> value.o(word));

        // let's look at some more complex receives

        // receive is very similar to match with cases
        Builder<?> program2 = receive()
            // receive specifies what to do if there is a message in the given channel
            .receive(words, exec(() ->
                println("got a word!")
            ))
            // you can also use a supplier, for working with references
            .receive(() -> numbers, exec(() ->
                println("got a number!")
            ))
            // you can add guards to determine if you do want to receive the message or not
            .receive(() -> numbers, i -> i > 5, exec(() ->
                println("got a number larger than 5!")
            ))
            // you can also pass a function to store the message
            .receive(() -> numbers, function((scope, _self, i) -> exec(() ->
                    println("got ${i()}!")
                )
            ))
            // and receive has a helper form that makes the function for you
            .receive(
                () -> numbers,
                (Expression<?> scope, Reference.O<Integer> i) -> exec(() -> println("got " + i.get() + "!"))
            )
            // and of course you can combine all if it together!
            .receive(
                numbers,
                i -> i > 5,
                (Expression<?> scope, Reference.O<Integer> i) ->
                    exec(() ->
                        println("got " + i.get() + "!")
                    )
            )
            // if you think there might not be any messages in any of the channels
            // you can use or with a guard
            .or(() -> false, exec(() ->
                println("nothing!")
            ))
            // an or without a guard terminates the whole receive builder
            // as this guarantees that this builder will be reached,
            // and nothing after it could be
            .or(exec(() ->
                println("nothing!")
            ));

        Channel<Messages.Down> downs = Channel.queue();

        Receive<?> program3 = receive()
            .receive(
                downs,
                (Expression<?> scope, Reference.O<Messages.Down> down) ->
                    exec(() ->
                        println("fiber " + down.get().getFrom() + " downed because " + down.get().getReason())
                    )
            )
            // if you don't specify a default or,
            // then receive will keep trying its conditions over and over forever
            // to help work around this, you can specify a timeout with after.
            // after uses the same time helper functions we saw earlier
            .after(seconds(10), exec(() ->
                println("timed out")
            ));

        // you can only have one after
        // specifying another one will remove the current one
    }

    // timers
    {
        // you can use Timer to spawn and interact with a timer process
        Channel<Timer<String>> timeouts = Channel.queue();

        // Timer.start starts a timer, and gives you a handle to it
        Timer<String> timer = Timer.start(
            // the duration
            10,
            // the destination
            timeouts,
            // the message
            "Hello, world"
        );

        // after 10 seconds the timer will send itself to the destination
        // to let you know its finished

        // you can edit the message after you have start the timer
        timer.setMessage("Goodbye, world");

        // and you can check how long is left!
        double remaining = timer.getRemainingSeconds();

        // you can also cancel it early
        timer.cancel();

        Channel<String> messages = Channel.queue();

        // Timer.sendAfter is like start,
        // but will only send the message, not the whole timer
        Timer<String> _a = Timer.sendAfter(
            10,
            messages,
            "Hello, world"
        );
    }

    // next, look at OpModes.kt for information on OpModes,
    // Mercurial takes a very different approach to OpModes in comparison to the base SDK

    // ignore:
    private static void println(String str) {
        System.out.println(str);
    }

    private static void println(int i) {
        System.out.println(i);
    }
}
