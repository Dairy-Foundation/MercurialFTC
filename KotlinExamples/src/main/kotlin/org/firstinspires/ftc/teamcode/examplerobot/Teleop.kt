package org.firstinspires.ftc.teamcode.examplerobot

import com.pedropathing.math.Pose
import com.pedropathing.math.Vector2D
import dev.frozenmilk.dairy.mercurial.ftc.GamepadD
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC
import dev.frozenmilk.dairy.mercurial.processes.EventManager

private val targetA = Vector2D.cartesian(0.0, 10.0)
private val targetB = Vector2D.cartesian(100.0, 10.0)

fun teleop(pose: Pose): MercurialFTC.RegisterableProgram = MercurialFTC.teleop {
    val r = Robot(this)
    r.localizer.setPose(pose)

    val drivetrainInputHandler =
        // filter for gamepad 1
        GamepadD.gamepad1Handler(
            // send driver inputs to drivetrain
            EventManager.forwardTo(r.drivetrain) { event ->
                r.drivetrain.sendPowers(
                    -event.leftStickY,
                    event.leftStickX,
                    event.rightStickX
                )
            }
        )

    val shooterInputHandler =
        // filter for gamepad 2
        GamepadD.gamepad2Handler(
            // consume driver inputs to control the shooter
            EventManager.forwardTo(r.shooter) { event ->
                if (event.aWasPressed) r.shooter.target(targetA)
                else if (event.bWasPressed) r.shooter.target(targetB)
                else if (event.xWasPressed) r.shooter.idle()
            }
        )

    val intakeInputHandler =
        // filter for gamepad 2
        GamepadD.gamepad2Handler(
            // consume driver inputs to control the intake
            EventManager.forwardTo(r.intake) { event ->
                if (event.rightTriggerPassedOver(0.1)) r.intake.switchOn()
                else if (event.rightTriggerPassedUnder(0.1)) r.intake.switchOff()
            }
        )


    // wait for start
    waitForStart()

    // install handlers
    gamepadD.addHandler(drivetrainInputHandler)
    gamepadD.addHandler(shooterInputHandler)
    gamepadD.addHandler(intakeInputHandler)

    // yield remainder of opmode to the scheduler
    dropToScheduler()

    // return null, we won't transition to another opmode after teleop
    null
}

// standard teleop
@Suppress("unused")
val teleop = teleop(Pose(72.0, 72.0))
