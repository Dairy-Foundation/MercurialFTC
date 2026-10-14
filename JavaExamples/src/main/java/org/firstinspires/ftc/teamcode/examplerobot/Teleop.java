package org.firstinspires.ftc.teamcode.examplerobot;

import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector2D;

import dev.frozenmilk.dairy.mercurial.ftc.GamepadD;
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC;
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC.RegisterableProgram;
import dev.frozenmilk.dairy.mercurial.processes.EventManager;

@SuppressWarnings("unused")
public class Teleop {
    private static final Vector2D targetA = Vector2D.cartesian(0.0, 10.0);
    private static final Vector2D targetB = Vector2D.cartesian(100.0, 10.0);
    
    // standard teleop
    public static final RegisterableProgram teleop = teleop(new Pose(72.0, 72.0));
    
    public static RegisterableProgram teleop(Pose pose) {
        return MercurialFTC.teleop(ctx -> {
            Robot r = new Robot(ctx);
            r.localizer.setPose(pose);
            
            EventManager.Handler<GamepadD.Delta> drivetrainInputHandler =
                // filter for gamepad 1
                GamepadD.gamepad1Handler(
                    // send driver inputs to drivetrain
                    EventManager.forwardTo(r.drivetrain, event ->
                        r.drivetrain.sendPowers(
                            -event.leftStickY(),
                            event.leftStickX(),
                            event.rightStickX()
                        )
                    )
                );
            
            EventManager.Handler<GamepadD.Delta> shooterInputHandler =
                // filter for gamepad 2
                GamepadD.gamepad2Handler(
                    // consume driver inputs to control the shooter
                    EventManager.forwardTo(r.shooter, event -> {
                        if (event.aWasPressed()) r.shooter.target(targetA);
                        else if (event.bWasPressed()) r.shooter.target(targetB);
                        else if (event.xWasPressed()) r.shooter.idle();
                    })
                );
            
            EventManager.Handler<GamepadD.Delta> intakeInputHandler =
                // filter for gamepad 2
                GamepadD.gamepad2Handler(
                    // consume driver inputs to control the intake
                    EventManager.forwardTo(r.intake, event -> {
                        if (event.rightTriggerPassedOver(0.1)) r.intake.switchOn();
                        else if (event.rightTriggerPassedUnder(0.1)) r.intake.switchOff();
                    })
                );
            
            // wait for start
            ctx.waitForStart();
            
            // install handlers
            ctx.gamepadD().addHandler(drivetrainInputHandler);
            ctx.gamepadD().addHandler(shooterInputHandler);
            ctx.gamepadD().addHandler(intakeInputHandler);
            
            // yield remainder of opmode to the scheduler
            ctx.dropToScheduler();
            
            // return null, we won't transition to another opmode after teleop
            return null;
        });
    }
}
