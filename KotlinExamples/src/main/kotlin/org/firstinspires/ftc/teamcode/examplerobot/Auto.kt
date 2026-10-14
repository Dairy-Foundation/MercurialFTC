package org.firstinspires.ftc.teamcode.examplerobot

import com.pedropathing.api.Paths
import com.pedropathing.api.PoseFactory
import com.pedropathing.math.Pose
import com.pedropathing.paths.Path
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.expression
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.seconds
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitFor
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitUntil
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC.autonomous

private val p = PoseFactory.degrees()
private val start: Pose = p.of(24.0, 24.0, 0.0)
private val end: Pose = p.of(48.0, 48.0, 90.0)
private val path1: Path = Paths.line(start, end)
private val path2: Path = Paths.line(end, start)

@Suppress("unused")
val auto = autonomous {
    val r = Robot(this)
    r.localizer.setPose(start)

    val program = expression { _ ->
        // wait for the opmode to start
        !waitUntil(::inLoop)
        !r.drivetrain.followPathSync(path1, true)
        !(waitFor * seconds(10.0))
        !r.drivetrain.followPathSync(path2, true)
        noop
    }.spawnable()

    // spawn and link
    val fiber = program.spawnLink()

    // yield remainder of auto to the scheduler
    dropToScheduler()

    // switch straight into teleop
    teleop(r.localizer.pose())
}
