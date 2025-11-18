package org.firstinspires.ftc.teamcode.beginners

import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.continuations.Fiber
import dev.frozenmilk.dairy.mercurial.ftc.MercurialOpMode

// Mercurial 2.0 uses a special OpMode runner at the moment
// its possible to recreate the way it works in other OpModes
// but MercurialOpMode has some nice advantages

// MercurialOpMode needs a constructor that takes a Context, and only a Context
// This will be automatically provided by Sloth

// It is important to note that MercurialOpMode does not extend OpMode or LinearOpMode

class MyFirstMercurialOpMode(context: Context) : MercurialOpMode(context) {
    // in MercurialOpMode, its completely legal to access the hardwareMap immediately
    val motor = hardwareMap.get(DcMotorEx::class.java, "")

    // MercurialOpModes have no special methods to fill in
    // instead, they run until their constructor finishes
    // in kotlin, you can write your code in an `init` block
    init {
        // provided values:

        // the scheduler:
        // allows us to run Continuations
        // however, you probably won't need to interact with it directly
        scheduler

        // gamepads:
        // standard from the sdk
        gamepad1
        gamepad2

        // hardwareMap:
        // standard from the sdk
        hardwareMap

        // telemetry:
        // standard from the sdk
        telemetry

        // current state:
        // INIT, LOOP, STOP
        state

        // helpers:
        inInit // true if state == INIT
        inLoop // true if state == LOOP
        isActive // true if either of the above are true

        // allows us to schedule a Continuation
        schedule(exec {})
        schedule(
            sequence(
                exec {},
                exec {},
                exec {},
                exec {},
            )
        )
        // this one will run forever
        val fiber: Fiber = schedule(
            loop(
            exec {}))
        // so we can grab the fiber from it
        // and cancel it:
        Fiber.CANCEL(fiber)
        // so that it doesn't run forever

        // in addition to `schedule`
        // we have some helpers to set up loops that poll for events,
        // and if they become true, they run a Continuation for us

        // these are generally better to use than schedule,
        // unless you're scheduling an infinite loop,
        // or scheduling a one off to run immediately

        // every single loop that `gamepad1.a` returns true
        // this will start an infinite loop that sets the motor power to 1
        // however, if the infinite loop is already running
        // then it will cancel the infinite loop, and replace it with a new copy
        bindExec(
            // condition
            { gamepad1.a },
            // run
            loop(exec { motor.power = 1.0 }),
        )

        // bindSpawn is like bindExec, but will not cancel already running Fibers
        // every time `gamepad1.a` is pressed, this will wait 1 second, then turn on the motor
        bindSpawn(
            // we can use the rising edge function to add a filter to the condition:
            // this only runs once when we press `gamepad1.a`, not every loop that it is pressed
            risingEdge { gamepad1.a },
            // we haven't seen wait before, but it waits for the passed number in seconds
            sequence(
                wait(1.0),
                exec { motor.power = 1.0 },
            ),
        )

        // as long as gamepad1.a continues to return true,
        // the loop will continue to run
        // once it returns false,
        // the loop will be cancelled
        bindWhileTrue(
            { gamepad1.a },
            loop(exec { motor.power = 1.0 }),
        )

        // we have some common utility functions we have seen in LinearOpMode
        // wait for start will run the scheduler until start is pressed
        waitForStart()

        telemetry.addLine("started!")

        // drop to scheduler will give up the rest of the op mode runtime to the scheduler
        dropToScheduler()

        // forgetting to call this will cause your op mode to end early
        // code after it will be run only after the opmode finishes

        // now, on to `Registers`
    }
}