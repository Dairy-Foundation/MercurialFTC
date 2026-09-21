package org.firstinspires.ftc.teamcode.examplerobot

import com.pedropathing.api.Paths.line
import com.pedropathing.api.PoseFactory
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.expression
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.seconds
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitFor
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.waitUntil
import dev.frozenmilk.dairy.mercurial.ftc.MercurialFTC
import org.firstinspires.ftc.teamcode.examplerobot.pedro.Constants


private val p = PoseFactory.degrees()
private val start = p.of(24.0, 24.0, 0.0)
private val end = p.of(48.0, 48.0, 90.0)
private val path1 = line(start, end)
private val path2 = line(end, start)

@Suppress("unused")
val auto = MercurialFTC.autonomous {
    val pedro = Constants.create(hardwareMap)

    pedro.localizer.setPose(start)

    val drivetrain = Drivetrain(pedro)
    val shooter = Shooter(hardwareMap, pedro.localizationEventManager)

    val program = expression {
        // wait for the opmode to start
        !waitUntil(::isActive)
        !drivetrain.followSync(path1, true)
        !exec { shooter.target(end) }
        !(waitFor * seconds { 10.0 })
        !exec { shooter.idle() }
        !drivetrain.followSync(path2, true)
        noop
    }.spawnable()

    // spawn and link
    val fiber = program.spawnLink()

    // yield remainder of auto to the scheduler
    dropToScheduler()

    // return into teleop
    // this will auto transition you into teleop
    // with the correctly set pose
    teleop(pedro.localizer.pose())
}