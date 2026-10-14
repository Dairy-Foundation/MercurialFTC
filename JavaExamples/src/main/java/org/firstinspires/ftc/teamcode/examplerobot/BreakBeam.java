package org.firstinspires.ftc.teamcode.examplerobot;

import com.qualcomm.robotcore.hardware.DigitalChannel;

import dev.frozenmilk.dairy.mercurial.ftc.Context;
import dev.frozenmilk.dairy.mercurial.processes.EventManager;

// this demonstrates how to use a standalone sensor
// I recommend against putting sensors in subsystems when possible
// as it allows them to be shared and composed more easily
// We extend EventManager
public class BreakBeam extends EventManager<BreakBeam.Event> {
    // and define our own event type
    public enum Event {
        Broken,
        Unbroken,
    }
    
    // the constructor sets up the related hardware
    private final DigitalChannel breakBeam;
    public BreakBeam(Context ctx) {
        breakBeam = ctx.hardwareMap().get(DigitalChannel.class, "breakBeam");
        breakBeam.setMode(DigitalChannel.Mode.INPUT);
    }
    
    private boolean previous = false;
    
    // and we override eventSource
    // which is called every loop, and allows us to notify the event manager
    @Override
    protected void eventSource() {
        // NOTE: breakBeam sensors return true when they are not broken
        boolean broken = !breakBeam.getState();
        if (broken && !previous) notify(Event.Broken);
        if (!broken && previous) notify(Event.Unbroken);
        previous = broken;
    }
}
