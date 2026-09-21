package org.firstinspires.ftc.teamcode.examplerobot;

import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.examplerobot.pedro.Constants;

import dev.frozenmilk.dairy.mercurial.ftc.GamepadD;
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC;
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC.RegisterableProgram;
import dev.frozenmilk.dairy.mercurial.pedro.MercurialFollower;
import dev.frozenmilk.dairy.mercurial.processes.EventManager;
import dev.frozenmilk.dairy.mercurial.processes.EventManager.EventHandled;

@SuppressWarnings("unused")
public class Teleop {
    private static final Pose targetA = new Pose(0.0, 10.0);
    private static final Pose targetB = new Pose(100.0, 10.0);

    public static RegisterableProgram teleop(Pose pose) {
        return MercurialFTC.teleop(ctx -> {
            MercurialFollower pedro = Constants.create(ctx.hardwareMap());

            pedro.localizer().setPose(pose);

            Drivetrain drivetrain = new Drivetrain(pedro);
            Shooter shooter = new Shooter(ctx.hardwareMap(), pedro.localizationEventManager());

            EventManager.Handler<GamepadD.Delta> drivetrainInputHandler =
                GamepadD.gamepad1Handler(event -> {
                    drivetrain.sendPowers(
                        ManualDrive.fieldCentric(
                            -event.leftStickY(),
                            event.leftStickX(),
                            event.rightStickX(),
                            pedro.localizer().pose().heading()
                            )
                    );
                    if (!drivetrain.fiber().status().alive()) return EventHandled.remove();
                    else return EventHandled.ok();
                });

            // and this only gets gamepad2 events
            EventManager.Handler<GamepadD.Delta> shooterInputHandler =
                GamepadD.gamepad2Handler(event -> {
                    if (event.aWasPressed()) shooter.target(targetA);
                    else if (event.bWasPressed()) shooter.target(targetB);
                    else if (event.xWasPressed()) shooter.idle();

                    if (!shooter.fiber().status().alive()) return EventHandled.remove();
                    else return EventHandled.ok();
                });

            // wait for start
            ctx.waitForStart();

            // install handlers
            ctx.gamepadD().addHandler(shooterInputHandler);
            ctx.gamepadD().addHandler(drivetrainInputHandler);

            // yield remainder of opmode to the scheduler
            ctx.dropToScheduler();

            // return null, we won't transition to another opmode after teleop
            return null;
        });
    }

    // standard teleop
    public static final RegisterableProgram teleop = teleop(new Pose(0.0, 0.0));
}
