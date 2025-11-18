package org.firstinspires.ftc.teamcode.beginners

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.MercurialOpMode

// we can still use `@TeleOp` like usual
@TeleOp
class MyFirstMercurialTeleOp(context: Context) : MercurialOpMode(context) {

    init {
        val fl = hardwareMap.get(DcMotorEx::class.java, "fl")
        val bl = hardwareMap.get(DcMotorEx::class.java, "bl")
        val br = hardwareMap.get(DcMotorEx::class.java, "br")
        val fr = hardwareMap.get(DcMotorEx::class.java, "fr")

        var throttle = 1.0

        br.direction = DcMotorSimple.Direction.REVERSE
        fr.direction = DcMotorSimple.Direction.REVERSE

        // POV drive
        schedule(
            sequence(
                // wait can also take a boolean supplier,
                // we'll start this process now,
                // but it will wait until we press play to actually start running
                wait { inLoop },
                exec {
                    val drive = -gamepad1.left_stick_y.toDouble()
                    val turn = gamepad1.right_stick_x.toDouble()

                    // a simple POV drive
                    fl.power = (drive + turn) * throttle
                    bl.power = (drive + turn) * throttle
                    br.power = (drive - turn) * throttle
                    fr.power = (drive - turn) * throttle
                }
            )
        )

        // throttle controls
        bindSpawn(
            risingEdge { gamepad1.right_bumper },
            exec { throttle = 0.5 }
        )

        bindSpawn(
            // inverting the condition will convert our rising edge detector to a falling edge detector!
            risingEdge { !gamepad1.right_bumper },
            exec { throttle = 1.0 }
        )

        // TODO: i'd love to increase the size of this example

        dropToScheduler()
    }
}