package org.firstinspires.ftc.teamcode.examplerobot

import com.pedropathing.localization.MotionState
import com.pedropathing.math.Vector2D
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.frozenmilk.dairy.mercurial.ftc.Context
import dev.frozenmilk.dairy.mercurial.processes.Channel
import dev.frozenmilk.dairy.mercurial.processes.SwitchedStateMachine
import kotlin.math.hypot

class Shooter(ctx: Context) : SwitchedStateMachine() {
    // the channel we will receive motion states into
    private val motionStates: Channel<MotionState> = linkedSingleChannel()

    // motor
    private val motor: DcMotorEx = ctx.hardwareMap.get(DcMotorEx::class.java, "shooter").apply {
        mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
    }

    // idle
    private val idle = entranceMode { _ ->
        // when you enter the idle mode, turn off the motor
        motor.power = 0.0
    }

    // a fixed power
    private fun fixedMode(power: Double) =
        entranceMode { _ -> motor.power = power }

    // targeting a pose
    private fun targetMode(target: Vector2D) =
        mode { self ->
            // we poll the motion states,
            // so that we only do work when we get a new motion state
            val motionState = motionStates.poll()
            if (motionState != null) {
                // calculate the power from the position of the robot and the target position
                motor.power = 0.1 + 0.001 * hypot(
                    target.x() - motionState.pose().x(),
                    target.y() - motionState.pose().y()
                )
            }
            self
        }

    override fun init(): Mode {
        // we init into the idle mode
        return idle
    }

    // public api:
    fun sendMotionState(motionState: MotionState) {
        motionStates.send(motionState)
    }

    fun target(point: Vector2D) {
        switchTo(targetMode(point))
    }

    fun idle() {
        switchTo(idle)
    }

    fun fixed(power: Double) {
        switchTo(fixedMode(power))
    }
}
