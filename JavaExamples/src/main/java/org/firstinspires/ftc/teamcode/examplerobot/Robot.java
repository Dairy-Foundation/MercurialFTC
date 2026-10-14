package org.firstinspires.ftc.teamcode.examplerobot;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.examplerobot.pedro.Constants;

import dev.frozenmilk.dairy.mercurial.ftc.Context;
import dev.frozenmilk.dairy.mercurial.pedro.MercurialLocalizer;
import dev.frozenmilk.dairy.mercurial.processes.EventManager;
import dev.frozenmilk.dairy.mercurial.processes.EventManager.EventHandled;

// a robot class can be used to bundle together systems
// so that they are reusable across different opmodes
public class Robot {
    public final MercurialLocalizer localizer;
    public final Drivetrain drivetrain;
    public final Shooter shooter;
    public final Intake intake;
    public final BreakBeam breakBeam;
    
    public Robot(Context ctx) {
        // create the localizer
        localizer = Constants.createLocalizer(ctx.hardwareMap());
        
        // Mercurial does not use telemetry the same way regular opmodes do
        // instead, you retain the telemetry item and update it over time
        Telemetry.Item poseTelemetry = ctx.telemetry().addData("Localizer/Pose", "unknown");
        // send updates from the localizer to telemetry
        localizer.addHandler(event -> {
            poseTelemetry.setValue(event.pose());
            return EventHandled.ok();
        });
        
        // create the drivetrain
        drivetrain = new Drivetrain(ctx);
        // send updates from the localizer to the drivetrain
        localizer.addHandler(
            EventManager.forwardTo(
                drivetrain,
                drivetrain::sendMotionState
            )
        );
        
        // create the shooter
        shooter = new Shooter(ctx);
        // send updates from the localizer to the shooter
        localizer.addHandler(
            EventManager.forwardTo(
                shooter,
                shooter::sendMotionState
            )
        );
        
        // create the intake
        intake = new Intake(ctx);
        
        // create the break beam
        breakBeam = new BreakBeam(ctx);
        
        // send updates from the break beam to control the shooter
        breakBeam.addHandler(EventManager.forwardTo(shooter, event -> {
            switch (event) {
                // when the break beam is broken
                // we have stored 4 pollen in our robot
                // so we turn on the shooter,
                // to begin spinning it up for shooting them
                case Broken:
                    shooter.fixed(1);
                    break;
                
                // if its unbroken,
                // then we have shot all the pollen,
                // and we turn the shooter off again
                case Unbroken:
                    shooter.idle();
                    break;
            }
        }));
        
        // send updates from the break beam to control the intake
        breakBeam.addHandler(EventManager.forwardTo(intake, event -> {
            switch (event) {
                // when the break beam is broken
                // we have stored 4 pollen in our robot
                // so we disable the intake,
                // to prevent over possession
                case Broken:
                    intake.enable(false);
                    break;
                
                // if its unbroken,
                // then we have shot all the pollen,
                // and we re-enable the intake
                case Unbroken:
                    intake.enable(true);
                    break;
            }
        }));
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
