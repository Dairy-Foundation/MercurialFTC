package org.firstinspires.ftc.teamcode.examplerobot

import com.qualcomm.robotcore.hardware.DigitalChannel
import dev.frozenmilk.dairy.mercurial.ftc.Context
import dev.frozenmilk.dairy.mercurial.processes.EventManager

// this demonstrates how to use a standalone sensor
// I recommend against putting sensors in subsystems when possible
// as it allows them to be shared and composed more easily
// We extend EventManager
class BreakBeam(ctx: Context) : EventManager<BreakBeam.Event>() {
    // and define our own event type
    enum class Event {
        Broken,
        Unbroken,
    }

    // the constructor sets up the related hardware
    private val breakBeam: DigitalChannel = ctx.hardwareMap.get(DigitalChannel::class.java, "breakBeam").apply {
        mode = DigitalChannel.Mode.INPUT
    }
    private var previous = false

    // and we override eventSource
    // which is called every loop, and allows us to notify the event manager
    override fun eventSource() {
        // NOTE: breakBeam sensors return true when they are not broken
        val broken = !breakBeam.state
        if (broken && !previous) notify(Event.Broken)
        if (!broken && previous) notify(Event.Unbroken)
        previous = broken
    }
}
