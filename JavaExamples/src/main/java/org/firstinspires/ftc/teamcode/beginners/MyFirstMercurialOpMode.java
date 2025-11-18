package org.firstinspires.ftc.teamcode.beginners;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitSeconds;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.jetbrains.annotations.NotNull;

import dev.frozenmilk.dairy.mercurial.continuations.Fiber;
import dev.frozenmilk.dairy.mercurial.ftc.MercurialOpMode;

// Mercurial 2.0 uses a special OpMode runner at the moment
// its possible to recreate the way it works in other OpModes
// but MercurialOpMode has some nice advantages

// It is important to note that MercurialOpMode does not extend OpMode or LinearOpMode

public class MyFirstMercurialOpMode extends MercurialOpMode {
    // in MercurialOpMode, its completely legal to access the hardwareMap immediately
    private final DcMotorEx motor = hardwareMap().get(DcMotorEx.class, "");

    // MercurialOpMode needs a constructor that takes a Context, and only a Context
    // This will be automatically provided by Sloth
    public MyFirstMercurialOpMode(@NotNull Context context) {
        super(context);

        // MercurialOpModes have no special methods to fill in
        // instead, they run until their constructor finishes
        // in java, you can write your code in a constructor or an init block
        // we'll do the constructor, but an init block is demonstrated below

        // provided values:

        // the scheduler:
        // allows us to run Continuations
        // however, you probably won't need to interact with it directly
        scheduler();

        // gamepads:
        // standard from the sdk
        gamepad1();
        gamepad2();

        // hardwareMap:
        // standard from the sdk
        hardwareMap();

        // telemetry:
        // standard from the sdk
        telemetry();

        // current state:
        // INIT, LOOP, STOP
        state();

        // helpers:
        inInit(); // true if state == INIT
        inLoop(); // true if state == LOOP
        isActive(); // true if either of the above are true

        // allows us to schedule a Continuation
        schedule(exec(() -> {}));
        schedule(
            sequence(
                exec(() -> {}),
                exec(() -> {}),
                exec(() -> {}),
                exec(() -> {})
            )
        );
        // this one will run forever
        Fiber fiber = schedule(loop(exec(() -> {})));
        // so we can grab the fiber from it
        // and cancel it:
        Fiber.CANCEL(fiber);
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
                () -> gamepad1().a,
                // run
                loop(exec(() -> motor.setPower(1)))
        );

        // bindSpawn is like bindExec, but will not cancel already running Fibers
        // every time `gamepad1.a` is pressed, this will wait 1 second, then turn on the motor
        bindSpawn(
                // we can use the rising edge function to add a filter to the condition:
                // this only runs once when we press `gamepad1.a`, not every loop that it is pressed
                risingEdge(() -> gamepad1().a),
                // we haven't seen wait before, but it waits for the passed number in seconds
                sequence(
                        waitSeconds(1),
                        exec(() -> motor.setPower(1))
                )
        );

        // as long as gamepad1.a continues to return true,
        // the loop will continue to run
        // once it returns false,
        // the loop will be cancelled
        bindWhileTrue(
                () -> gamepad1().a,
                loop(exec(() -> motor.setPower(1)))
        );

        // we have some common utility functions we have seen in LinearOpMode
        // wait for start will run the scheduler until start is pressed
        waitForStart();

        telemetry().addLine("started!");

        // drop to scheduler will give up the rest of the op mode runtime to the scheduler
        dropToScheduler();

        // forgetting to call this will cause your op mode to end early
        // code after it will be run only after the opmode finishes

        // now, on to `Registers`

    }

    // this is an init block:
    {
        schedule(exec(() -> {}));
    }
}
