package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import dev.frozenmilk.dairy.mercurial.ftc.GamepadD;
import dev.frozenmilk.dairy.mercurial.processes.Channel;
import dev.frozenmilk.dairy.mercurial.processes.EventManager;
import dev.frozenmilk.dairy.mercurial.processes.EventManager.EventHandled;

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
    
    // we can implement EventManager.Handler
    EventManager.Handler<String> handler1 = new EventManager.Handler<String>() {
        @NonNull
        @Override
        public EventHandled<String> handleEvent(String event) {
            // we can do whatever we want with the event, like print it out,
            // put it in telemetry, or send it to another process
            println(event);
            
            // but remember to do it quickly, you shouldn't block the event management process
            // if you need to do something more complex, then you should spawn a fiber to do it for you
            // or send it to a different process
            
            boolean isOk = true;
            
            // by default, you should return EventHandled.ok()
            // which says that event handling went fine,
            // and to keep this handler for the next event!
            if (isOk) return EventHandled.ok();
            // if the event handler is finished for any reason, you can return EventHandled.remove()
            // which will remove the handler from the event manager
            else return EventHandled.remove();
        }
    };
    // lets look at some more features of handlers
    EventManager.Handler<String> handler2 = new EventManager.Handler<String>() {
        @Override
        @NotNull
        public EventHandled<String> handleEvent(String event) {
            // you can return SwapTo to replace this handler with another
            return new EventHandled.SwapTo<>(handler1);
        }
        
        // you can optionally override remove
        // remove is only called when removal is not triggered by handleEvent
        // for instance,
        // returning EventHandled.remove or EventHandled.SwapTo will not cause remove to be called
        // but using EventManager.removeHandler() will cause remove to be called
        @Override
        public void remove() {
        }
    };
    
    // simple handlers can be written as lambdas
    EventManager.Handler<String> lambda = event -> EventHandled.ok();
    
    // there are some common cases for event handling
    
    Channel<String> words = Channel.queue();
    Channel<Integer> numbers = Channel.queue();
    
    // forward to can forward directly to a channel
    // and will remove itself when the channel is closed
    EventManager.Handler<String> forwarding = EventManager.forwardTo(words);
    
    // forward to can also take a mapping function
    EventManager.Handler<String> forwarding2 = EventManager.forwardTo(numbers, String::length);
    
    // number events is a different event manager
    EventManager<Integer> numberEvents = new EventManager<>();
    
    // forwardTo also allows you to easily handle an event in a custom way,
    // making the handler depend on a fiberous (like a fiber, an event manager, or a state machine)
    EventManager.Handler<String> forwarding3 = EventManager.forwardTo(numberEvents, message -> {
        numberEvents.notify(message.length());
    });
    
    // you can use depending on to wrap and compose dependencies with other handlers
    // note that depending on will call a handler's remove method
    // if the depended upon channel closes or fiber exits
    EventManager.Handler<String> dependingOn = EventManager.dependingOn(numberEvents, forwarding);
    
    // in mercurial opmodes there is a gamepad daemon, which is an event manager
    // it only emits new events when new gamepad data comes in over the network, not every loop
    GamepadD gamepadD = new GamepadD();
    // it emits delta events, which represent a change in gamepad data
    EventManager.Handler<GamepadD.Delta> gamepadHandler = event -> {
        // you can check if the event is for gamepad 1 or 2
        if (event.id() == GamepadD.Id.One) {
            // the event allows you to check the current state of the buttons and sticks
            if (event.b()) println("b is pressed");
            
            // and also if they were just pressed or released
            if (event.xWasPressed()) println("x was pressed");
        }
        if (event.id() == GamepadD.Id.Two) {
            // same as above
        }
        return EventHandled.ok();
    };
    
    // gamepad d has helpers that auto filter to gamepad 1 or 2
    EventManager.Handler<GamepadD.Delta> gamepad1Handler = GamepadD.gamepad1Handler(
        event -> {
            if (event.xWasPressed()) println("x was pressed");
            return EventHandled.ok();
        }
    );
    
    // I recommend using the EventManager helpers along with the filters
    // to drive messages caused by driver and operator inputs
    
    {
        // finally, you must add handlers to event managers to start running them
        stringManager.addHandler(forwarding);
        // you can later remove the handler, which returns true if it succeeded
        boolean success = stringManager.removeHandler(forwarding);
        // swap handler is similar, and only works if the first handler was present
        boolean success2 = stringManager.swapHandler(forwarding, forwarding2);
    }
    
    {
        // similarly, you need to add gamepad handlers
        gamepadD.addHandler(gamepadHandler);
        gamepadD.addHandler(gamepad1Handler);
    }
    
    // event managers are well suited to acting as sensors on a robot
    static class BreakBeam extends EventManager<BreakBeam.Event> {
        enum Event {
            Broken,
            Unbroken
        }
        
        // on your actual robot, you would use a sensor
        private boolean sensorState() {
            return false;
        }
        
        private boolean previous;
        
        // you can override the event source function to run an event poll every loop,
        // rather than notify the event manager externally
        @Override
        protected void eventSource() {
            boolean current = sensorState();
            // note that we only notify on a change in state
            // but it is also ok to always notify with the current state
            // it depends on the way you want to model your system
            if (current && !previous) notify(Event.Unbroken);
            else if (!current && previous) notify(Event.Broken);
            previous = current;
        }
    }
    
    // I recommend using event handlers with the gamepad daemon instead of the regular gamepads
    // they make it much easier to work with channels and messages
    
    // next, move on to StateMachines.java to see how to model subsystems
    
    // ignore:
    private static void println(String str) {
        System.out.println(str);
    }
}
