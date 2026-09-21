package org.firstinspires.ftc.teamcode;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.*;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import dev.frozenmilk.dairy.mercurial.continuations.Expression;
import dev.frozenmilk.dairy.mercurial.environments.Reference;
import dev.frozenmilk.dairy.mercurial.ftc.GamepadD;
import dev.frozenmilk.dairy.mercurial.processes.Channel;
import dev.frozenmilk.dairy.mercurial.processes.EventManager;
import dev.frozenmilk.dairy.mercurial.processes.EventManager.EventHandled;
import dev.frozenmilk.dairy.mercurial.processes.Fiber;

@SuppressWarnings("unused")
public class EventManagers {
    // event managers manage an event type
    // you can add and remove event handlers as the manager runs
    // when the event manager receives an event, it gives it to all the event handlers
    // which can then do things like log the event
    // or transform and forward it to another channel

    // you can construct an event manager to start it
    // this one handles string events
    EventManager<String> stringManager = new EventManager<>();

    // this program receives messages over chan
    // and prints them out
    Channel<String> chan = Channel.queue();
    Fiber<?> program = loop(
        receive()
            .receive(chan, (Expression<?> scope, Reference.O<String> msg) ->
                exec(() -> println(msg.get()))
            )
    ).spawnable().spawnLink();

    // we can implement EventManager.Handler
    EventManager.Handler<String> forwardingHandler = new EventManager.Handler<String>() {
        @NonNull
        @Override
        public EventHandled<String> handleEvent(String event) {
            // we can do whatever we want with the event
            chan.send(event);
            // but remember to do it quickly, you shouldn't block the process
            // if you need to do something more complex, then you should spawn a fiber to do it for you
            // or send it to a different process

            // if the destination program finished for any reason
            // then we return EventHandled.remove
            // to remove this event handler
            // otherwise we return ok, to keep the handler for the next event
            if (program.status().alive()) return EventHandled.ok();
            else return EventHandled.remove();
        }
    };

    {
        // finally, we add the handler to the string manager to enable it
        stringManager.addHandler(forwardingHandler);
        // you can later remove the handler, which returns true if it succeeded
        boolean success = stringManager.removeHandler(forwardingHandler);
        // swap handler is similar, and only works if the first handler was present
        boolean success2 = stringManager.swapHandler(forwardingHandler, forwardingHandler);
    }

    // lets look at some more features of handlers
    EventManager.Handler<String> handler = new EventManager.Handler<String>() {
        @Override
        @NotNull
        public EventHandled<String> handleEvent(String event) {
            // you can return SwapTo to replace this handler with another
            return new EventHandled.SwapTo<>(forwardingHandler);
        }

        // you can optionally override remove
        // this function is called when removeHandler or swapHandler are used to remove it
        @Override
        public void remove() {
        }
    };

    // in mercurial opmodes there is a gamepad daemon, which is an event manager
    // it only emits new events when new gamepad data comes in over the network, not every loop
    GamepadD gamepadD = new GamepadD();

    // it emits delta events, which represent a change in gamepad data
    EventManager.Handler<GamepadD.Delta>gamepadHandler = new EventManager.Handler<GamepadD.Delta>() {
        @Override
        @NotNull
        public EventHandled<GamepadD.Delta> handleEvent(GamepadD.Delta event) {
            // you can check if the event is for gamepad 1 or 2
            if (event.id() == GamepadD.Id.One) {
                // the event allows you to check the current state of the buttons and sticks
                if (event.b()) chan.send("b is pressed");

                // and also if they were just pressed or released
                if (event.xWasPressed()) chan.send("x was pressed");
            }
            if (event.id() == GamepadD.Id.Two) {
                // same as above
            }
            return EventHandled.ok();
        }
    };

    // gamepad d has helpers that auto filter to gamepad 1 or 2
    EventManager.Handler<GamepadD.Delta> gamepad1Handler = GamepadD.gamepad1Handler(
        event -> {
            if (event.xWasPressed()) chan.send("x was pressed");
            return EventHandled.ok();
        }
    );

    {
        // then you can add the handlers to the daemon
        gamepadD.addHandler(gamepadHandler);
        gamepadD.addHandler(gamepad1Handler);
    }

    // I recommend using event handlers with the gamepad daemon instead of the regular gamepads
    // they make it much easier to work with channels and messages

    // next, move on to StateMachines.kt to see how to model subsystems

    // ignore:
    private static void println(String str) {
        System.out.println(str);
    }
}
