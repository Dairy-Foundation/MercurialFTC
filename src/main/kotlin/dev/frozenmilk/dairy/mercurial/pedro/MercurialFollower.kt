package dev.frozenmilk.dairy.mercurial.pedro

import com.pedropathing.algorithm.Algorithm
import com.pedropathing.drivetrain.DrivePowers
import com.pedropathing.drivetrain.Drivetrain
import com.pedropathing.localization.Localizer
import com.pedropathing.localization.MotionState
import com.pedropathing.math.Pose
import com.pedropathing.paths.PathTracker
import dev.frozenmilk.dairy.mercurial.Mercurial
import dev.frozenmilk.dairy.mercurial.continuations.Continuation
import dev.frozenmilk.dairy.mercurial.processes.EventManager
import dev.frozenmilk.dairy.mercurial.processes.Spawnable
import kotlin.time.DurationUnit
import kotlin.time.TimeMark

class MercurialFollower(
    @get:JvmName("localizer")
    val localizer: Localizer,
    @get:JvmName("drivetrain")
    val drivetrain: Drivetrain,
    @get:JvmName("algorithm")
    val algorithm: Algorithm,
) {
    @get:JvmName("localizationEventManager")
    val localizationEventManager = EventManager<MotionState>()
    @get:JvmName("localizerFiber")
    val localizerFiber = Spawnable.Builder<Nothing>(object : Continuation {
        override fun eval() = run {
            localizer.update()
            localizationEventManager.notify(localizer.state())
            this
        }
    }).spawnLink()

    fun manual(powers: DrivePowers) = drivetrain.drive(powers, true)
    fun idle() = drivetrain.stop()

    fun hold(
        pose: Pose,
        useScaling: Boolean,
        motionState: MotionState,
        timeMark: TimeMark?,
    ): TimeMark {
        val powers = algorithm.calculateHold(
            drivetrain,
            pose,
            motionState,
            useScaling,
            timeMark?.elapsedNow()?.toDouble(DurationUnit.SECONDS) ?: 0.0,
        )
        drivetrain.drive(powers, false)
        return Mercurial.timeSource.markNow()
    }

    fun hold(
        pose: Pose,
        useScaling: Boolean,
        motionState: MotionState,
    ) = hold(
        pose,
        useScaling,
        motionState,
        null,
    )

    fun follow(
        pathTracker: PathTracker,
        motionState: MotionState,
        timeMark: TimeMark?,
    ): TimeMark? {
        if (pathTracker.done()) {
            pathTracker.release()
            return null
        }
        val powers = algorithm.calculatePath(
            drivetrain,
            pathTracker,
            motionState,
            timeMark?.elapsedNow()?.toDouble(DurationUnit.SECONDS) ?: 0.0,
        )
        drivetrain.drive(powers, false)
        return Mercurial.timeSource.markNow()
    }
}
