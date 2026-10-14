package org.firstinspires.ftc.teamcode.examplerobot

import dev.frozenmilk.dairy.mercurial.ftc.Context
import dev.frozenmilk.dairy.mercurial.processes.EventManager
import dev.frozenmilk.dairy.mercurial.processes.EventManager.EventHandled
import org.firstinspires.ftc.teamcode.examplerobot.pedro.Constants

// a robot class can be used to bundle together systems
// so that they are reusable across different opmodes
class Robot(ctx: Context) {
    // create the localizer
    val localizer = Constants.createLocalizer(ctx.hardwareMap)

    init {
        // Mercurial does not use telemetry the same way regular opmodes do
        // instead, you retain the telemetry item and update it over time
        val poseTelemetry = ctx.telemetry.addData("Localizer/Pose", "unknown")

        // send updates from the localizer to telemetry
        localizer.addHandler { event ->
            poseTelemetry.setValue(event.pose())
            EventHandled.Ok
        }
    }

    // create the drivetrain
    val drivetrain = Drivetrain(ctx)

    init {
        // send updates from the localizer to the drivetrain
        localizer.addHandler(
            EventManager.forwardTo(
                drivetrain,
                drivetrain::sendMotionState,
            )
        )
    }

    // create the shooter
    val shooter = Shooter(ctx)

    init {
        // send updates from the localizer to the shooter
        localizer.addHandler(
            EventManager.forwardTo(
                shooter,
                shooter::sendMotionState,
            )
        )
    }

    // create the intake
    val intake = Intake(ctx)

    // create the break beam
    val breakBeam = BreakBeam(ctx)

    init {
        // send updates from the break beam to control the shooter
        breakBeam.addHandler(EventManager.forwardTo(shooter) { event ->
            when (event) {
                // when the break beam is broken
                // we have stored 4 pollen in our robot
                // so we turn on the shooter,
                // to begin spinning it up for shooting them
                BreakBeam.Event.Broken -> shooter.fixed(1.0)

                // if its unbroken,
                // then we have shot all the pollen,
                // and we turn the shooter off again
                BreakBeam.Event.Unbroken -> shooter.idle()
            }
        })

        // send updates from the break beam to control the intake
        breakBeam.addHandler(
            EventManager.forwardTo(intake) { event ->
                when (event) {
                    // when the break beam is broken
                    // we have stored 4 pollen in our robot
                    // so we disable the intake,
                    // to prevent over possession
                    BreakBeam.Event.Broken -> intake.enabled = false

                    // if its unbroken,
                    // then we have shot all the pollen,
                    // and we re-enable the intake
                    BreakBeam.Event.Unbroken -> intake.enabled = true
                }
            })
    }

    // you can make several different robot classes for different purposes
    // i.e. one for testing each system independently, or for tuning just your shooter.
    // the way we structured our code means each system does not know about the others,
    // or even any sensors.
    // instead, we link systems and sensors together externally.
    // this keeps everything reusable and independent,
    // and makes it easy to recombine them in different ways,
    // substitute out data sources,
    // and to make changes to one system without needing to adjust others
}
