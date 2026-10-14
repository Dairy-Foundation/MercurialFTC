package org.firstinspires.ftc.teamcode.examplerobot

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import dev.frozenmilk.dairy.mercurial.ftc.Context
import dev.frozenmilk.dairy.mercurial.processes.SwitchedStateMachine

// this is a very simple intake subsystem
class Intake(ctx: Context) : SwitchedStateMachine() {
    private val motor: DcMotorEx = ctx.hardwareMap.get(DcMotorEx::class.java, "intake").apply {
        zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        direction = DcMotorSimple.Direction.FORWARD
    }

    private val on = mode { self ->
        // on needs to check if the intake is enabled
        // if the intake isn't enabled, wait for it to become enabled
        if (enabled) motor.power = 1.0
        else motor.power = 0.0
        self
    }

    // off is always off
    private val off = entranceMode { _ -> motor.power = 0.0 }

    var enabled = true

    override fun init(): Mode {
        return off
    }

    // public api
    fun switchOn() {
        switchTo(on)
    }

    fun switchOff() {
        switchTo(off)
    }
}
