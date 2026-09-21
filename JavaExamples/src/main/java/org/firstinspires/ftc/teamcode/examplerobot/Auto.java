package org.firstinspires.ftc.teamcode.examplerobot;

import static com.pedropathing.api.Paths.*;

import static org.firstinspires.ftc.teamcode.examplerobot.Teleop.teleop;

import static dev.frozenmilk.dairy.mercurial.continuations.Continuations.*;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.examplerobot.pedro.Constants;

import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC;
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC.RegisterableProgram;
import dev.frozenmilk.dairy.mercurial.pedro.MercurialFollower;
import dev.frozenmilk.dairy.mercurial.processes.Fiber;
import dev.frozenmilk.dairy.mercurial.processes.Spawnable;

@SuppressWarnings("unused")
public class Auto {
    private static final PoseFactory p = PoseFactory.degrees();
    private static final Pose start = p.of(24.0, 24.0, 0.0);
    private static final Pose end = p.of(48.0, 48.0, 90.0);
    private static final Path path1 = line(start, end);
    private static final Path path2 = line(end, start);

    public static final RegisterableProgram auto = MercurialFTC.autonomous(ctx -> {
        MercurialFollower pedro = Constants.create(ctx.hardwareMap());

        pedro.localizer().setPose(start);

        Drivetrain drivetrain = new Drivetrain(pedro);
        Shooter shooter = new Shooter(ctx.hardwareMap(), pedro.localizationEventManager());

        Spawnable<?> program = expression((scope, _self) -> {
            // wait for the opmode to start
            scope.run(waitUntil(ctx::isActive));
            scope.runExec(() -> shooter.target(end));
            scope.run(waitFor.bind(seconds(10)));
            scope.runExec(shooter::idle);
            scope.run(drivetrain.followSync(path2, true));
            return noop;
        }).spawnable();

        // spawn and link
        Fiber<?> fiber = program.spawnLink();

        // yield remainder of auto to the scheduler
        ctx.dropToScheduler();

        // return into teleop
        // this will auto transition you into teleop
        // with the correctly set pose
        return teleop(pedro.localizer().pose());
    });
}
