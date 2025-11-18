package org.firstinspires.ftc.teamcode.beginners;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitUntil;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.jetbrains.annotations.NotNull;

import dev.frozenmilk.dairy.mercurial.ftc.MercurialOpMode;

@TeleOp
public class MyFirstMercurialTeleOp extends MercurialOpMode {

    private double throttle = 1.0;

    public MyFirstMercurialTeleOp(@NotNull Context context) {
        super(context);

        DcMotorEx fl = hardwareMap().get(DcMotorEx.class, "fl");
        DcMotorEx bl = hardwareMap().get(DcMotorEx.class, "bl");
        DcMotorEx br = hardwareMap().get(DcMotorEx.class, "br");
        DcMotorEx fr = hardwareMap().get(DcMotorEx.class, "fr");

        br.setDirection(DcMotorSimple.Direction.REVERSE);
        fr.setDirection(DcMotorSimple.Direction.REVERSE);

        // POV drive
        schedule(
                sequence(
                        // wait can also take a boolean supplier,
                        // we'll start this process now,
                        // but it will wait until we press play to actually start running
                        waitUntil(this::inLoop),
                        exec(() -> {
                            double drive = -gamepad1().left_stick_y;
                            double turn = gamepad1().right_stick_x;

                            fl.setPower((drive + turn) * throttle);
                            bl.setPower((drive + turn) * throttle);
                            br.setPower((drive - turn) * throttle);
                            fr.setPower((drive - turn) * throttle);
                        })
                )
        );

        // throttle controls
        bindSpawn(
                risingEdge(() -> gamepad1().right_bumper),
                exec(() -> throttle = 0.5)
        );

        bindSpawn(
                // inverting the condition will convert our rising edge detector to a falling edge detector!
                risingEdge(() -> !gamepad1().right_bumper),
                exec(() -> throttle = 1.0)
        );

        // TODO: i'd love to increase the size of this example

        dropToScheduler();
    }
}
