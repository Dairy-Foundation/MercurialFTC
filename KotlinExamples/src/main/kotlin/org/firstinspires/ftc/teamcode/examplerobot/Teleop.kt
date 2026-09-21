package org.firstinspires.ftc.teamcode.examplerobot

import com.pedropathing.follower.ManualDrive
import com.pedropathing.math.Pose
import dev.frozenmilk.dairy.mercurial.ftc.GamepadD
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC
import dev.frozenmilk.dairy.mercurial.processes.EventManager
import dev.frozenmilk.dairy.mercurial.processes.EventManager.EventHandled
import org.firstinspires.ftc.teamcode.examplerobot.pedro.Constants

private val targetA = Pose(0.0, 10.0)
private val targetB = Pose(100.0, 10.0)

fun teleop(pose: Pose): MercurialFTC.RegisterableProgram = MercurialFTC.teleop {
    val pedro = Constants.create(hardwareMap)

    pedro.localizer.setPose(pose)

    val drivetrain = Drivetrain(pedro)
    val shooter = Shooter(hardwareMap, pedro.localizationEventManager)

    val drivetrainInputHandler =
        GamepadD.gamepad1Handler(object : EventManager.Handler<GamepadD.Delta> {
            override fun handleEvent(event: GamepadD.Delta): EventHandled<GamepadD.Delta> {
                drivetrain.sendPowers(
                    ManualDrive.fieldCentric(
                        -event.leftStickY,
                        event.leftStickX,
                        event.rightStickX,
                        pedro.localizer.pose().heading(),
                    )
                )
                return if (!drivetrain.fiber.status.alive) EventHandled.Remove
                else EventHandled.Ok
            }
        })

    // and this only gets gamepad2 events
    val shooterInputHandler =
        GamepadD.gamepad2Handler(object : EventManager.Handler<GamepadD.Delta> {
            override fun handleEvent(event: GamepadD.Delta): EventHandled<GamepadD.Delta> {
                if (event.aWasPressed) shooter.target(targetA)
                else if (event.bWasPressed) shooter.target(targetB)
                else if (event.xWasPressed) shooter.idle()

                return if (!shooter.fiber.status.alive) EventHandled.Remove
                else EventHandled.Ok
            }
        })

    // wait for start
    waitForStart()

    // install handlers
    gamepadD.addHandler(shooterInputHandler)
    gamepadD.addHandler(drivetrainInputHandler)

    // yield remainder of opmode to the scheduler
    dropToScheduler()

    // return null, we won't transition to another opmode after teleop
    null
}

// standard teleop
@Suppress("unused")
val teleop = teleop(Pose(0.0, 0.0))