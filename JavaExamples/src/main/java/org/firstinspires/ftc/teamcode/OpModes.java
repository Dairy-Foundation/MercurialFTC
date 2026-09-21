package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;

import dev.frozenmilk.dairy.mercurial.Mercurial;
import dev.frozenmilk.dairy.mercurial.ftc.Context;
import dev.frozenmilk.dairy.mercurial.ftc.GamepadD;
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC;
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC.RegisterableProgram;
import dev.frozenmilk.dairy.mercurial.ftc.State;
import dev.frozenmilk.dairy.mercurial.processes.Scheduler;
import kotlin.time.TimeSource;

@SuppressWarnings("unused")
public class OpModes {
    // Mercurial uses a custom system for opmodes called programs.
    // Programs that are assigned to public values in objects
    // or are top level (like these) will be found by Mercurial

    // if you don't need a program for a bit, you can make it private, or comment it out

    // teleop programs are just like `@TeleOp`
    public static RegisterableProgram myTeleop = MercurialFTC.teleop(ctx -> {
        // Programs automatically have Mercurial.initialiseThread() called
        // so you shouldn't call it, otherwise it will cause issues

        // inside the body of a program,
        // you can access members of the program's Context:
        // the scheduler
        Scheduler scheduler = ctx.scheduler();
        // the opmode's metadata
        OpModeMeta metadata = ctx.metadata();
        // the gamepad daemon
        GamepadD gamepadD = ctx.gamepadD();
        // gamepads 1 and 2
        Gamepad gamepad1 = ctx.gamepad1();
        Gamepad gamepad2 = ctx.gamepad2();
        // the HardwareMap
        HardwareMap hardwareMap = ctx.hardwareMap();
        // the Telemetry (NOTE: Mercurial turns off auto clear by default, this may change the way you use telemetry)
        Telemetry telemetry = ctx.telemetry();
        // the current state of the opmode (INIT, LOOP, or STOP)
        State state = ctx.state();

        // there are some helper methods for dealing with the state
        // is active will be true if the opmode is currently running (not stopped)
        boolean isActive = ctx.isActive();
        // in init will be true if the opmode is in init, but not yet looping
        boolean inInit = ctx.inInit();
        // in loop will be true if the opmode is in loop, and no longer in init
        boolean inLoop = ctx.inLoop();

        // you can use wait for start like in a linear opmode
        // this will run the scheduler for you
        ctx.waitForStart();

        // and when you're done setting up,
        // you can use drop to scheduler to run the scheduler for the rest of the opmode
        ctx.dropToScheduler();

        // at the end of each program, you need to return the next program to run
        // or null, if you don't want to run another program
        // most teleops won't need to run another program
        return null;
    });

    // it can be good to write a function to make your programs
    // that way, they can be adapted and composable
    private static RegisterableProgram myTeleop2(Pose pose) {
        return MercurialFTC.teleop(ctx -> null)
            // you can use modifier methods to change the metadata of the opmode
            .withGroup("Cool!")
            // if you don't set a name,
            // Mercurial will take it from the name of the field it was stored in
            .withName("My Radical Teleop!!");
    }


    // it is good to make a default version of dynamic opmodes
    // just in case
    public static RegisterableProgram myTeleop2 = myTeleop2(new Pose(0.0, 0.0, 0.0));

    // autonomous programs are just like `@Autonomous`
    public static RegisterableProgram myAuto = MercurialFTC.autonomous(ctx -> {
        Pose pose = new Pose(0.0, 0.0, 0.0);
        // this is a common pattern
        // at the end of an autonomous,
        // you pass data directly into the teleop
        // like the pose of the robot
        // and mercurial will instantly init into the next stage of the opmode
        // we say that these opmodes are "dynamic"
        // and as soon as you stop it, it will be removed from the driver station
        return myTeleop2(pose);
    });

    // utility programs are just like `@Utility`
    RegisterableProgram myUtil = MercurialFTC.utility(new MercurialFTC.Program() {
        // if you need to customise the settings for mercurial,
        // you can override this method in Program
        // like maybe you want to change the scheduler, or turn off retracing
        @NonNull
        @Override
        public Mercurial.Settings settings() {
            return new Mercurial.Settings(
                TimeSource.Monotonic.INSTANCE,
                false,
                new Scheduler.Standard()
            );
        }

        @Override
        public MercurialFTC.Program exec(@NonNull Context ctx) {
            return null;
        }
    });

    // next, look at EventManagers.kt for information on EventManagers
    // which allow you to publish and subscribe to events
}
