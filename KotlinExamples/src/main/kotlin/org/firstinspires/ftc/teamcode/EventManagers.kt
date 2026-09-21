package org.firstinspires.ftc.teamcode

import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.receive
import dev.frozenmilk.dairy.mercurial.ftc.GamepadD
import dev.frozenmilk.dairy.mercurial.processes.Channel
import dev.frozenmilk.dairy.mercurial.processes.EventManager
import dev.frozenmilk.dairy.mercurial.processes.EventManager.EventHandled

class EventManagers {
    // event managers manage an event type
    // you can add and remove event handlers as the manager runs
    // when the event manager receives an event, it gives it to all the event handlers
    // which can then do things like log the event
    // or transform and forward it to another channel

    // you can construct an event manager to start it
    // this one handles string events
    val stringManager = EventManager<String>()

    // this program receives messages over chan
    // and prints them out
    val chan = Channel.queue<String>()
    val program = loop(
        receive<Any?>()
            .receive(chan) { msg ->
                exec { println(msg()) }
            }
    ).spawnable().spawnLink()

    // we can implement EventManager.Handler
    val forwardingHandler = object : EventManager.Handler<String> {
        override fun handleEvent(event: String): EventHandled<String> {
            // we can do whatever we want with the event
            chan.send(event)
            // but remember to do it quickly, you shouldn't block the process
            // if you need to do something more complex, then you should spawn a fiber to do it for you
            // or send it to a different process

            // if the destination program finished for any reason
            // then we return EventHandled.Remove
            // to remove this event handler
            // otherwise we return ok, to keep the handler for the next event
            return if (program.status.alive) EventHandled.Ok
            else EventHandled.Remove
        }
    }

    init {
        // finally, we add the handler to the string manager to enable it
        stringManager.addHandler(forwardingHandler)
        // you can later remove the handler, which returns true if it succeeded
        val success: Boolean = stringManager.removeHandler(forwardingHandler)
        // swap handler is similar, and only works if the first handler was present
        val success2: Boolean = stringManager.swapHandler(forwardingHandler, forwardingHandler)
    }

    // lets look at some more features of handlers
    val handler = object : EventManager.Handler<String> {
        override fun handleEvent(event: String): EventHandled<String> {
            // you can return SwapTo to replace this handler with another
            return EventHandled.SwapTo(forwardingHandler)
        }

        // you can optionally override remove
        // this function is called when removeHandler or swapHandler are used to remove it
        override fun remove() {
        }
    }

    // in mercurial opmodes there is a gamepad daemon, which is an event manager
    // it only emits new events when new gamepad data comes in over the network, not every loop
    val gamepadD = GamepadD()

    // it emits delta events, which represent a change in gamepad data
    val gamepadHandler = object : EventManager.Handler<GamepadD.Delta> {
        override fun handleEvent(event: GamepadD.Delta): EventHandled<GamepadD.Delta> {
            // you can check if the event is for gamepad 1 or 2
            if (event.id === GamepadD.Id.One) {
                // the event allows you to check the current state of the buttons and sticks
                if (event.b) chan.send("b is pressed")

                // and also if they were just pressed or released
                if (event.xWasPressed) chan.send("x was pressed")
            }
            if (event.id === GamepadD.Id.Two) {
                // same as above
            }
            return EventHandled.Ok
        }
    }

    // gamepad d has helpers that auto filter to gamepad 1 or 2
    val gamepad1Handler = GamepadD.gamepad1Handler(object : EventManager.Handler<GamepadD.Delta> {
        override fun handleEvent(event: GamepadD.Delta): EventHandled<GamepadD.Delta> {
            if (event.xWasPressed) chan.send("x was pressed")
            return EventHandled.Ok
        }
    })

    init {
        // then you can add the handlers to the daemon
        gamepadD.addHandler(gamepadHandler)
        gamepadD.addHandler(gamepad1Handler)
    }

    // I recommend using event handlers with the gamepad daemon instead of the regular gamepads
    // they make it much easier to work with channels and messages

    // next, move on to StateMachines.kt to see how to model subsystems
}