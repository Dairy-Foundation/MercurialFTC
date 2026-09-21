@file:Suppress("unused")

package org.firstinspires.ftc.teamcode

import com.pedropathing.math.Pose
import dev.frozenmilk.dairy.mercurial.Mercurial
import dev.frozenmilk.dairy.mercurial.ftc.Context
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC

// Mercurial uses a custom system for opmodes called programs.
// Programs that are assigned to public values in objects
// or are top level (like these) will be found by Mercurial

// if you don't need a program for a bit, you can make it private, or comment it out

// teleop programs are just like `@TeleOp`
val myTeleop = MercurialFTC.teleop {
    // Programs automatically have Mercurial.initialiseThread() called
    // so you shouldn't call it, otherwise it will cause issues

    // inside the body of a program,
    // you can access members of the program's Context:
    // the scheduler
    val _ = scheduler
    // the opmode's metadata
    val _ = metadata
    // the gamepad daemon
    val _ = gamepadD
    // gamepads 1 and 2
    val _ = gamepad1
    val _ = gamepad2
    // the HardwareMap
    val _ = hardwareMap
    // the Telemetry (NOTE: Mercurial turns off auto clear by default, this may change the way you use telemetry)
    val _ = telemetry
    // the current state of the opmode (INIT, LOOP, or STOP)
    val _ = state

    // there are some helper methods for dealing with the state
    // is active will be true if the opmode is currently running (not stopped)
    val _ = isActive
    // in init will be true if the opmode is in init, but not yet looping
    val _ = inInit
    // in loop will be true if the opmode is in loop, and no longer in init
    val _ = inLoop

    // you can use wait for start like in a linear opmode
    // this will run the scheduler for you
    waitForStart()

    // and when you're done setting up,
    // you can use drop to scheduler to run the scheduler for the rest of the opmode
    dropToScheduler()

    // at the end of each program, you need to return the next program to run
    // or null, if you don't want to run another program
    // most teleops won't need to run another program
    null
}

// it can be good to write a function to make your programs
// that way, they can be adapted and composable
fun myTeleop2(pose: Pose) =
    MercurialFTC.teleop {
        null
    }
        // you can use modifier methods to change the metadata of the opmode
        .withGroup("Cool!")
        // if you don't set a name,
        // Mercurial will take it from the name of the field it was stored in
        .withName("My Radical Teleop!!")

// it is good to make a default version of dynamic opmodes
// just in case
val myTeleop2 = myTeleop2(Pose(0.0, 0.0, 0.0))

// autonomous programs are just like `@Autonomous`
val myAuto = MercurialFTC.autonomous {
    val pose = Pose(0.0, 0.0, 0.0)
    // this is a common pattern
    // at the end of an autonomous,
    // you pass data directly into the teleop
    // like the pose of the robot
    // and mercurial will instantly init into the next stage of the opmode
    // we say that these opmodes are "dynamic"
    // and as soon as you stop it, it will be removed from the driver station
    myTeleop2(pose)
}

// utility programs are just like `@Utility`
val myUtil = MercurialFTC.utility(object : MercurialFTC.Program {
    // if you need to customise the settings for mercurial,
    // you can override this method in Program
    // like maybe you want to change the scheduler, or turn off retracing
    override fun settings() = Mercurial.Settings(stackTraces = false)

    override fun Context.exec(): MercurialFTC.Program? {
        return null
    }
})

// next, look at EventManagers.kt for information on EventManagers
// which allow you to publish and subscribe to events