package org.firstinspires.ftc.teamcode

import dev.frozenmilk.dairy.mercurial.ftc.GamepadD
import dev.frozenmilk.dairy.mercurial.ftc.GamepadD.Companion.gamepad1Handler
import dev.frozenmilk.dairy.mercurial.processes.Channel
import dev.frozenmilk.dairy.mercurial.processes.EventManager
import dev.frozenmilk.dairy.mercurial.processes.EventManager.EventHandled

@Suppress("unused")
class EventManagers {
    // event managers manage an event type
    // you can add and remove event handlers as the manager runs
    // when the event manager receives an event, it gives it to all the event handlers
    // which can then do things like log the event
    // or transform and forward it to another channel
    // you can construct an event manager to start it
    // this one handles string events
    val stringManager = EventManager<String>()

    // we can implement EventManager.Handler
    val handler1 = object : EventManager.Handler<String> {
        override fun handleEvent(event: String): EventHandled<String> {
            // we can do whatever we want with the event, like print it out,
            // put it in telemetry, or send it to another process
            println(event)

            // but remember to do it quickly, you shouldn't block the event management process
            // if you need to do something more complex, then you should spawn a fiber to do it for you
            // or send it to a different process

            val isOk = true
            // by default, you should return EventHandled.Ok
            // which says that event handling went fine,
            // and to keep this handler for the next event!
            if (isOk) return EventHandled.Ok
            // if the event handler is finished for any reason, you can return EventHandled.Remove
            // which will remove the handler from the event manager
            else return EventHandled.Remove
        }
    }

    // lets look at some more features of handlers
    val handler2 = object : EventManager.Handler<String> {
        override fun handleEvent(event: String): EventHandled<String> {
            // you can return SwapTo to replace this handler with another
            return EventHandled.SwapTo(handler1)
        }

        // you can optionally override remove
        // remove is only called when removal is not triggered by handleEvent
        // for instance,
        // returning EventHandled.remove or EventHandled.SwapTo will not cause remove to be called
        // but using EventManager.removeHandler() will cause remove to be called
        override fun remove() {
        }
    }

    // simple handlers can be written as lambdas
    val lambda = EventManager.Handler { event: String -> EventHandled.Ok }

    // there are some common cases for event handling
    val words = Channel.queue<String>()
    val numbers = Channel.queue<Int>()

    // forward to can forward directly to a channel
    // and will remove itself when the channel is closed
    val forwarding = EventManager.forwardTo(words)

    // forward to can also take a mapping function
    val forwarding2: EventManager.Handler<String> =
        EventManager.forwardTo(numbers) { message -> message.length }

    // number events is a different event manager
    val numberEvents = EventManager<Int>()

    // forwardTo also allows you to easily consume an event in a custom way,
    // making the handler depend on a fiberous (like a fiber, an event manager, or a state machine)
    val forwarding3: EventManager.Handler<String> =
        EventManager.forwardTo(numberEvents) { message: String ->
            numberEvents.notify(message.length)
        }

    // you can use depending on to wrap and compose dependencies with other handlers
    // note that depending on will call a handler's remove method
    // if the depended upon channel closes or fiber exits
    val dependingOn: EventManager.Handler<String> =
        EventManager.dependingOn(numberEvents, forwarding)

    // in mercurial opmodes there is a gamepad daemon, which is an event manager
    // it only emits new events when new gamepad data comes in over the network, not every loop
    val gamepadD: GamepadD = GamepadD()

    // it emits delta events, which represent a change in gamepad data
    val gamepadHandler: EventManager.Handler<GamepadD.Delta> =
        EventManager.Handler { event ->
            // you can check if the event is for gamepad 1 or 2
            if (event.id == GamepadD.Id.One) {
                // the event allows you to check the current state of the buttons and sticks
                if (event.b) println("b is pressed")


                // and also if they were just pressed or released
                if (event.xWasPressed) println("x was pressed")
            }
            if (event.id == GamepadD.Id.Two) {
                // same as above
            }
            EventHandled.Ok
        }

    // gamepad d has helpers that auto filter to gamepad 1 or 2
    val gamepad1Handler: EventManager.Handler<GamepadD.Delta> = gamepad1Handler { event ->
        if (event.xWasPressed) println("x was pressed")
        EventHandled.Ok
    }

    // I recommend using the EventManager helpers along with the filters
    // to drive messages caused by driver and operator inputs
    init {
        // finally, you must add handlers to event managers to start running them
        stringManager.addHandler(handler1)
        // you can later remove the handler, which returns true if it succeeded
        val success = stringManager.removeHandler(handler1)
        // swap handler is similar, and only works if the first handler was present
        val success2 = stringManager.swapHandler(handler1, handler1)
    }

    init {
        // similarly, you need to add gamepad handlers
        gamepadD.addHandler(gamepadHandler)
        gamepadD.addHandler(gamepad1Handler)
    }

    // event managers are well suited to acting as sensors on a robot
    class BreakBeam : EventManager<BreakBeam.Event>() {
        enum class Event {
            Broken,
            Unbroken
        }

        // on your actual robot, you would use a sensor
        private fun sensorState(): Boolean {
            return false
        }

        private var previous = false

        // you can override the event source function to run an event poll every loop,
        // rather than notify the event manager externally
        override fun eventSource() {
            val current = sensorState()
            // note that we only notify on a change in state
            // but it is also ok to always notify with the current state
            // it depends on the way you want to model your system
            if (current && !previous) notify(Event.Unbroken)
            else if (!current && previous) notify(Event.Broken)
            previous = current
        }
    }

    // I recommend using event handlers with the gamepad daemon instead of the regular gamepads
    // they make it much easier to work with channels and messages
    // next, move on to StateMachines.kt to see how to model subsystems
}
