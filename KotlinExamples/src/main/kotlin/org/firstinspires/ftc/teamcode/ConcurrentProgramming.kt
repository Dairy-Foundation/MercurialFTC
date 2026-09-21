package org.firstinspires.ftc.teamcode

import dev.frozenmilk.dairy.mercurial.Mercurial
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.await
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.duration
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exit
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.expression
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.function
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.interrupt
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.kill
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.parallel
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.race
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.receive
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.repeat
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.spawn
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.value
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitFor
import dev.frozenmilk.dairy.mercurial.environments.Reference
import dev.frozenmilk.dairy.mercurial.processes.Channel
import dev.frozenmilk.dairy.mercurial.processes.Channel.Companion.tryPoll
import dev.frozenmilk.dairy.mercurial.processes.ExitReason
import dev.frozenmilk.dairy.mercurial.processes.Fiber
import dev.frozenmilk.dairy.mercurial.processes.Messages
import dev.frozenmilk.dairy.mercurial.processes.ProcessStatus
import dev.frozenmilk.dairy.mercurial.processes.Scheduler
import dev.frozenmilk.dairy.mercurial.processes.Spawnable
import dev.frozenmilk.dairy.mercurial.processes.Timer
import dev.frozenmilk.util.collections.WBT
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

class ConcurrentProgramming {
    // setup and the scheduler
    init {
        // before we actually run any Mercurial programs,
        // we need to take a quick look at setup and the scheduler

        // When using mercurial for ftc, this step will be done for you
        // if you don't initialise the thread,
        // then trying to run programs as we will do shortly will crash
        Mercurial.initialiseThread()
        // you can pass a settings object to change some global settings
        // these are the default settings
        Mercurial.initialiseThread(Mercurial.Settings(
            // the time source used by all of Mercurial
            timeSource = TimeSource.Monotonic,
            // controls if retracing of exceptions is enabled
            stackTraces = true,
            // the scheduler
            scheduler = Scheduler.Standard(),
        ))

        // you can access these values from Mercurial
        val scheduler = Mercurial.scheduler

        // if you need to run the scheduler yourself, you can poll it
        scheduler.poll()

        // poll duration is the previous duration of polling
        val _ = scheduler.pollDuration

        // average fiber duration is a weighted moving average fiber duration
        // more on fibers shortly
        val _ = scheduler.averageFiberDuration

        // you will learn more about working with the scheduler in opmodes later
    }

    // channels
    init {
        // a channel is a place to send and receive messages
        // we use them to communicate between different processes in Mercurial

        // a single channel can hold a single message at once
        // sending a new message to it will overwrite the old message
        // single channels are good if you only want the most up-to-date message
        // like if you use the channel to receive sensor data, or drive inputs
        val word = Channel.single<String>()

        // queue channels can hold an infinite list of messages
        // queues are good when you don't want to miss an event
        val words = Channel.queue<String>()

        // channels can be polled
        // they will return null if empty
        // otherwise they will return a message from the channel
        // and remove that message from the channel
        val aWord: String? = word.poll()

        // you can also send a message to the back of the channel
        word.send("Hello")

        // generally you should only be sending messages to the back of the channel
        // sometimes you need to put a message at the front
        word.prepend("World")

        // once again, for a single channel, sending two messages in a row will erase the first one
        // but not so for a queue

        // the tryPoll function will only run the given function
        // if there is a message in the channel
        // this function can help you to receive a message from the channel in a simple manner
        word.tryPoll { word: String ->
            println(word)
        }

        // we will use channels more later
        // now, lets look at ways to start running top level programs
    }

    // spawnables, fibers, spawning, linking, monitoring
    init {
        // you can convert any builder into a spawnable
        // this compiles it, and allows you to start a
        // new process running the program as many times as you wish
        val tenThenTrue: Spawnable<Boolean> = expression {
            !(waitFor * duration(10.seconds))
            value.b { true }
        }.spawnable()

        // you can take a spawnable and .spawn() it
        // which creates, schedules, and returns a fiber running the program
        // each fiber runs independently, and concurrently
        val fiber: Fiber<Boolean> = tenThenTrue.spawn()
        // remember a fiber is a running program,
        // each time you spawn the same program will be a new, different fiber

        // fibers will exit when they finish running the program
        // normally, they exit with ExitReason.Normally
        // you can force a fiber to exit like this:
        fiber.exit(ExitReason.Interrupt)
        // there are a few built in exit reasons, but you can add your own, by extending ExitReason

        // normally is for when a fiber finishes running
        // be careful about manually exiting normally,
        // as other fibers may expect the fiber to have stored a return value
        // and will see the normal exit reason as the program having finished
        // and then the expected value won't be there for them
        val _ = ExitReason.Normally

        // interrupt is a good generic abnormal reason
        val _ = ExitReason.Interrupt

        // kill is a special exit reason
        // it forces a fiber to die, preventing the fiber from ignoring the exit request
        // we will see more of this in a second
        val _ = ExitReason.Kill

        // kill is converted to killed,
        // allowing linked fibers to know that the fiber was killed with kill
        // but not also force killing them
        // more on linking shortly
        val _ = ExitReason.Killed

        // when a fiber throws an exception, the fiber will exit exceptionally.
        // exceptionally is treated specially,
        // and the exception will be re thrown if it causes the root fiber to exit
        val _ = ExitReason.Exceptionally(RuntimeException())

        // the root fiber is the fiber running the scheduler
        // its active by default
        // and if it exits, then the whole opmode will exit

        // fibers can be linked
        fiber.link()
        // this links the currently running fiber (the root at the moment) to fiber
        // if either of the linked fibers exits abnormally, it will cause the other fiber to exit
        // you should generally link fibers

        // spawnLink spawns and links immediately,
        val _ = tenThenTrue.spawnLink()

        // fibers can be monitored as well
        val downChannel = Channel.queue<Messages.Down>()
        fiber.monitor(downChannel)
        // when the fiber exits, a message (Messages.Down) will be sent to the down channel
        // Messages.Down records the fiber that exited, and the reason why

        // you can also spawnMonitor, which spawns and setups up the monitor at the same time
        val _ = tenThenTrue.spawnMonitor(downChannel)

        // you can unlink
        fiber.unlink()
        // or demonitor
        fiber.demonitor(downChannel)
        // to remove a link or monitor

        // you can check the current status of a fiber
        val status = fiber.status
        // the status is either ProcessStatus.Alive
        // or an exit reason
        when (status) {
            is ProcessStatus.Alive -> {}
            is ExitReason -> {}
        }
        // status.alive will be true if the fiber has not yet exited
        // and is ProcessStatus.Alive
        // you can use this to check if a fiber is still running
        if (status.alive) {

        }

        // when a fiber finishes running
        // you can get the return value
        // this will throw an exception if the fiber hasn't exited normally
        val cond: Boolean = fiber.returnValue

        // instead, you can use return result
        val condResult = fiber.returnResult
        // the result will be Ok with the value if the fiber finished normally
        // or will be the current status value
        val cond2 = when (condResult) {
            is Fiber.Result.Ok -> condResult.value
            is ProcessStatus -> false
        }

        // at any point you can check the current fiber
        // you can never know the return type of the current fiber though
        // as for it to be the current fiber, it must be alive
        val current: Fiber<*> = Fiber.current

        // you can use Fiber.exitChannel to set a channel to 'trap' exits
        // this sets it for the current Fiber
        // you cannot set it for other fibers
        val exitChannel = Channel.queue<Messages.Exit>()
        Fiber.exitChannel(exitChannel)
        // when Fiber.exit is called
        // it will be converted to Messages.Exit
        // and sent to the exit channel
        // only the special exit reason 'kill' can bypass this,
        // and the current fiber can always exit itself without sending a message

        // pollDuration is the time it took for the fiber to be run last cycle
        val _ = fiber.pollDuration

        // functions are a bit stranger to spawn
        // spawnable doesn't change anything about it
        // but does guarantee that the function has been compiled
        val print = function<String, String, Any?> { _, a, b ->
            exec { println(a() + ", " + b()) }
        }.spawnable()

        // unlike for builders, you use chained invocation to set each argument
        // and at the end you can spawn / spawnLink / spawnMonitor
        val fiber2 = print("Hello")("World").spawn()
        val _ = print("Hello")("World").spawnLink()
        val _ = print("Hello")("World").spawnMonitor(downChannel)

        // mixed syntax like this is not allowed
        val _ = (print * { "Hello" })("World").spawn()

        // its also not allowed to not complete the call in one go
        val _ = print("Hello")

        // these behaviours could all lead to undefined behaviour
    }

    // spawning closures
    init {
        // spawnable is only suitable for top level builders and functions
        // if you want to spawn a closure correctly,
        // then you'll need a slightly different set of tools

        val _ = expression {
            val x = i { 0 }

            val program = expression {
                // x is still available here, even though it runs in parallel
                !(waitFor * duration { 10.seconds })
                !exec { println(x()) }
                !exec { x(5) }
                value.i(x)
            }

            // the spawn builder will spawn the given builder,
            // correctly capturing the current environment
            !spawn(program)

            // you can use o to store the spawned fiber, and work with it like normal
            val fiber1 = o(spawn(program))

            // spawn.link behaves like we've seen before
            !spawn.link(program)

            // spawn.monitor also behaves as we've seen before
            val downChannel = o { Channel.queue<Messages.Down>() }
            !spawn.monitor(downChannel, program)

            // you can also spawn several fibers at once
            // this returns a set of fibers
            // this also works with spawn.link
            // and spawn.monitor
            // we will see some uses for groups of fibers shortly
            val fibers = o<WBT.Tree<Fiber<*>>?>(
                spawn(
                    program,
                    program,
                    program,
                    program,
                    program,
                )
            )

            noop
        }

        // next, lets look at meta functions to help await and exit spawned fibers
    }

    // awaiting, exiting
    init {
        val tenThenTrue: Spawnable<Boolean> = expression {
            !(waitFor * duration(10.seconds))
            value.b { true }
        }.spawnable()

        val _ = expression {
            val x = i { 0 }

            val program = expression {
                // x is still available here, even though it runs in parallel
                !(waitFor * duration { 10.seconds })
                !exec { println(x()) }
                !exec { x(5) }
                value.i(x)
            }

            // you can use awaiting and interruptions with either method of spawning a fiber
            val fiber1: Reference.O<Fiber<Boolean>> = o { tenThenTrue.spawnLink() }
            val fiber2: Reference.O<Fiber<Int>> = o(spawn(program))

            // there are meta functions and functions provided
            // to help you with waiting for return values from fibers

            // await will not return anything, and just waits for the fiber to exit
            !(await * fiber1)

            // await.status will return the exit reason of the fiber
            val status: Reference.O<ExitReason> = o(await.status * fiber2)

            // await.result will return the result of the fiber
            // either ok with the returned value
            // or error with the exit reason
            // this is very similar to fiber.returnResult
            // note that you need to tell result the type that will be returned
            val result: Reference.O<Fiber.Result<Boolean>> = o(await.result<Boolean>() * fiber1)

            // await.expect will throw an exception if the awaited fiber finished abnormally
            val value: Reference.O<Boolean> = o(await.expect<Boolean>() * fiber1)

            // it is best to prefer await if you won't look at the returned value / status
            // as it will perform a bit better

            // now we will look at awaiting groups
            val fibers: Reference.O<WBT.Tree<Fiber<*>>?> = o(
                spawn.link(
                    program,
                    program,
                    program,
                    program,
                    program,
                )
            )

            // await.all will wait for the whole group to exit
            !(await.all * fibers)
            // await.all will wait for any fiber in the group to exit
            !(await.any * fibers)

            // exit can be used to exit a fiber
            !(exit * fiber1 * { ExitReason.Interrupt })
            // exit.all can be used to exit a group
            !(exit.all * fibers * { ExitReason.Interrupt })

            // interrupt and kill will exit with the builtin exit reasons
            !(interrupt * fiber2)
            !(interrupt.all * fibers)
            !(kill * fiber2)
            !(kill.all * fibers)

            // there are some prebuilt meta functions for common spawn-await situations
            // these are based on the parallel tooling that most command bases use

            // parallel spawns, links, and awaits all fibers
            !parallel(
                program,
                program,
                program,
                program,
                program,
                program,
            )
            // race spawns and links all fibers,
            // then waits for any fiber to finish
            // then unlinks and cancels all remaining fibers
            !race(
                program,
                program,
                program,
                program,
            )

            // deadline spawns and links all fibers
            // then and waits for the first fiber (the deadline) to finish
            // then unlinks and cancels all other fibers
            !deadline(
                program,
                program,
                program,
                program,
            )

            noop
        }

        // now we have seen all of Mercurial's async / await api features
        // lets take a look at sending and receiving messages with channels
    }

    // messaging and receive
    init {
        // let's look at some example programs that send and receive messages to each other
        // this ping and pong program will run two fibers that will message back and forth 3 times

        // we will first make the channels
        // there is no important information for these channels
        // so we will send Unit
        val pingChannel = Channel.queue<Unit>()
        val pongChannel = Channel.queue<Unit>()

        val ping: Fiber<Any?> = expression {
            // receive is a lot like match with cases
            // we will look more at its details shortly

            // this will receive three pings
            // and respond to pong each time
            repeat(
                3,
                receive<Any?>()
                    .receive(pingChannel, exec {
                        // print ping
                        println("ping!")
                        // send back to pong
                        pongChannel.send(Unit)
                    })
            )
        }.spawnable().spawnLink()

        val pong: Fiber<Any?> = expression {
            // this will receive three pongs
            // and respond to ping each time
            repeat(
                3,
                receive<Any?>()
                    .receive(pongChannel, exec {
                        // print pong
                        println("pong!")
                        // send back to ping
                        pingChannel.send(Unit)
                    })
            )
        }.spawnable().spawnLink()

        // now the two programs will run and message back and forth
        // we just need to send the first message to start it!
        pingChannel.send(Unit)

        val words = Channel.queue<String>()
        val numbers = Channel.queue<Int>()

        // this receive will wait until a message comes in on the words channel
        // and then will return it
        val program1 = receive<String>()
            .receive(words) { word -> value.o(word) }

        // let's look at some more complex receives

        // receive is very similar to match with cases
        val program2 = receive<Any?>()
            // receive specifies what to do if there is a message in the given channel
            .receive(words, exec { println("got a word!") })
            // you can also use a supplier, for working with references
            .receive({ numbers }, exec { println("got a number!") })
            // you can add guards to determine if you do want to receive the message or not
            .receive({ numbers }, { i -> i > 5 }, exec { println("got a number larger than 5!") })
            // you can also pass a function to store the message
            .receive({ numbers }, function { _, i ->
                exec { println("got ${i()}!") }
            })
            // and receive has a helper form that makes the function for you
            .receive({ numbers }, { i -> exec { println("got ${i()}!") } })
            // and of course you can combine all if it together!
            .receive(numbers, { i -> i > 5 }, { i -> exec { println("got ${i()}!") } })
            // if you think there might not be any messages in any of the channels
            // you can use or with a guard
            .or({ false }, exec { println("nothing!") })
            // an or without a guard terminates the whole receive builder
            // as this guarantees that this builder will be reached,
            // and nothing after it could be
            .or(exec { println("nothing!") })

        val downs = Channel.queue<Messages.Down>()

        val program3 = receive<Any?>()
            .receive(downs) { down ->
                exec { println("fiber ${down().from} downed because ${down().reason}") }
            }
            // if you don't specify a default or,
            // then receive will keep trying its conditions over and over forever
            // to help work around this, you can specify a timeout with after.
            // after uses the same time helper functions we saw earlier
            .after(duration { 10.seconds }, exec { println("timed out") })
            // you can only have one after
            // specifying another one will remove the current one
    }

    // timers
    init {
        // you can use Timer to spawn and interact with a timer process
        val timeouts = Channel.queue<Timer<String>>()

        // Timer.start starts a timer, and gives you a handle to it
        val timer: Timer<String> = Timer.start(
            // the duration
            10.seconds,
            // the destination
            timeouts,
            // the message
            "Hello, world",
        )

        // after 10 seconds the timer will send itself to the destination
        // to let you know its finished

        // you can edit the message after you have start the timer
        timer.message = "Goodbye, world"

        // and you can check how long is left!
        val remaining = timer.remaining.toDouble(DurationUnit.SECONDS)

        // you can also cancel it early
        timer.cancel()

        val messages = Channel.queue<String>()

        // Timer.sendAfter is like start,
        // but will only send the message, not the whole timer
        val _ = Timer.sendAfter(
            10.seconds,
            messages,
            "Hello, world",
        )
    }

    // next, look at OpModes.kt for information on OpModes,
    // Mercurial takes a very different approach to OpModes in comparison to the base SDK
}
