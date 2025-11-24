package org.firstinspires.ftc.teamcode.intermediate;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec;
import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop;

import dev.frozenmilk.dairy.mercurial.ftc.Mercurial;

@SuppressWarnings("unused")
public class AutoPipelines {
    // Mercurial allows you to define a common program function
    // here, we take a message to put in telemetry
    // but more realistically, you could pass the pose of your robot
    // and any other initialising information
    // to the teleop
    private static Mercurial.Program teleop(String message) {
        return ctx -> {
            ctx.schedule(loop(
                    exec(() -> {
                        ctx.telemetry().addData("message", message);
                        ctx.telemetry().update();
                    })
            ));
            ctx.dropToScheduler();
        };
    }

    // we can register the teleop like normal
    // and provide sensible defaults
    public static Mercurial.RegisterableProgram teleop = Mercurial.teleop(teleop("no message"));

    // and we can use 'pipelineAutonomous'
    // in order to produce a pipeline from auto to teleop
    public static Mercurial.RegisterableProgram autonomous = Mercurial.pipelineAutonomous(ctx -> {
        ctx.dropToScheduler();
        return teleop("from-auto");
    });

    // when this autonomous finishes,
    // a temporary teleop will appear and be instantly initialised
    // once this teleop is stopped, it will be removed
    // this teleop will be named `autonomous |> teleop (System)`
    // which is the name of the auto, followed by ` |> teleop (System)`
    // `|>` is the 'pipeline' operator in some languages

    // its important to remember that in order to do this legally this season,
    // none of your robot may move in init
    // so make sure that you keep that in mind if you use this feature
    // one way to ensure that this doesn't happen, is to wait for start to be pressed
    // before running any code

    // this is a very safe way to customise and configure your teleop
}
